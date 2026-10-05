package com.colossalfurnaces.blockentity;

import com.colossalfurnaces.block.ColossalFurnaceControllerBlock;
import com.colossalfurnaces.block.ColossalFurnaceInterfaceBlock;
import com.colossalfurnaces.block.ColossalFurnaceWallBlock;
import com.colossalfurnaces.block.ColossalSmokerControllerBlock;
import com.colossalfurnaces.config.ColossalFurnacesConfig;
import com.colossalfurnaces.menu.ColossalFurnaceMenu;
import com.colossalfurnaces.registry.ModBlockEntities;
import com.colossalfurnaces.registry.ModParticles;
import com.colossalfurnaces.util.ColossalFurnaceStructure;
import com.colossalfurnaces.util.MultiblockValidationResult;
import com.colossalfurnaces.util.LegacyItemHandlerAdapter;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ColossalFurnaceControllerBlockEntity extends BlockEntity implements MenuProvider {
    private static final int COOKING_PROGRESS_DECAY = 2;
    public static final int MAX_PARALLELISM = 25;
    public static final int MAX_INPUT_SLOTS = 9;
    public static final int MAX_OUTPUT_SLOTS = 9;
    public static final int MAX_FUEL_SLOTS = 3;
    private static final int LIT_GRACE_TICKS = 4;

    private final InputHandler inputHandler = new InputHandler(MAX_INPUT_SLOTS);
    private final FuelHandler fuelHandler = new FuelHandler(MAX_FUEL_SLOTS);
    private final OutputHandler outputHandler = new OutputHandler(MAX_OUTPUT_SLOTS);
    private final Object2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed = new Object2IntOpenHashMap<>();
    private final List<LaneState> laneStates = new ArrayList<>();
    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ColossalFurnaceControllerBlockEntity.this.formed ? 1 : 0;
                case 1 -> ColossalFurnaceControllerBlockEntity.this.outerSize;
                case 2 -> ColossalFurnaceControllerBlockEntity.this.getProcessingParallelism();
                case 3 -> ColossalFurnaceControllerBlockEntity.this.inventoryParallelism;
                case 4 -> ColossalFurnaceControllerBlockEntity.this.heatBuffer;
                case 5 -> ColossalFurnaceControllerBlockEntity.this.getActiveLaneCount();
                case 6 -> ColossalFurnaceControllerBlockEntity.this.isMenuLit() ? 1 : 0;
                case 7 -> ColossalFurnaceControllerBlockEntity.this.getMenuLitProgress();
                case 8 -> ColossalFurnaceControllerBlockEntity.this.getMenuBurnProgress();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index < 0) {
                return;
            }
        }

        @Override
        public int getCount() {
            return 9;
        }
    };
    private final IItemHandlerModifiable automationHandler = new AutomationItemHandler(this);
    private final ResourceHandler<ItemResource> automationTransferHandler = new LegacyItemHandlerAdapter(this.automationHandler);
    private boolean formed;
    private BlockPos minPos;
    private BlockPos maxPos;
    private int outerSize;
    private int inventoryParallelism = 1;
    private int heatBuffer;
    private int currentFuelBurnRemaining;
    private int currentFuelBurnTotal;
    private int litGraceTicks;
    private boolean consumedHeatThisTick;
    private Boolean lastAppliedShellLightState;
    private int inputPage;
    private int fuelPage;
    private int outputPage;
    private final Set<BlockPos> linkedInterfaces = new HashSet<>();

    public ColossalFurnaceControllerBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.COLOSSAL_FURNACE_CORE.get(), pos, state);
    }

    protected ColossalFurnaceControllerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
        this.minPos = pos;
        this.maxPos = pos;
        for (int index = 0; index < MAX_PARALLELISM; index++) {
            this.laneStates.add(new LaneState());
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ColossalFurnaceControllerBlockEntity blockEntity) {
        blockEntity.consumedHeatThisTick = false;
        if (!blockEntity.formed) {
            blockEntity.litGraceTicks = 0;
            blockEntity.updateLitState();
            return;
        }

        boolean changed = blockEntity.processSmeltingWork();
        if (!blockEntity.consumedHeatThisTick) {
            changed |= blockEntity.drainIdleFuel();
        }
        blockEntity.tickLitGracePeriod();

        if (changed) {
            blockEntity.clampPages();
            blockEntity.setChanged();
            blockEntity.sync();
        }
        blockEntity.updateLitState();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ColossalFurnaceControllerBlockEntity blockEntity) {
        if (!blockEntity.formed || !state.hasProperty(ColossalFurnaceControllerBlock.LIT) || !state.getValue(ColossalFurnaceControllerBlock.LIT)) {
            return;
        }
        blockEntity.spawnWorkingParticles();
    }

    public void openMenu(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || this.level == null || !this.formed) {
            return;
        }

        serverPlayer.openMenu(this, buffer -> buffer.writeBlockPos(this.worldPosition));
    }

    public boolean revalidateStructure(@Nullable Player player, boolean suppressErrors) {
        if (this.level == null || this.level.isClientSide() || this.isRemoved()
                || !this.level.getBlockState(this.worldPosition).is(this.getBlockState().getBlock())) {
            return false;
        }

        MultiblockValidationResult validationResult = this.validateStructure(this.level, this.worldPosition);
        if (validationResult.valid() && validationResult.controllerPos().equals(this.worldPosition)) {
            this.applyStructure(validationResult);
            return true;
        }

        if (this.formed) {
            this.clearStructureState();
        }

        if (!suppressErrors && player != null && ColossalFurnacesConfig.showStructureErrorMessages && validationResult.error() != null) {
            player.sendOverlayMessage(Component.translatable(validationResult.error().translationKey()));
        }
        return false;
    }

    private void applyStructure(MultiblockValidationResult validationResult) {
        if (!this.linkedInterfaces.equals(validationResult.interfacePositions())) {
            this.unlinkInterfaces();
        }
        this.formed = true;
        this.minPos = validationResult.minPos();
        this.maxPos = validationResult.maxPos();
        this.outerSize = validationResult.outerSize();
        this.inventoryParallelism = validationResult.parallelism();
        this.linkedInterfaces.clear();
        this.linkedInterfaces.addAll(validationResult.interfacePositions());
        this.setShellFormedState(validationResult.shellPositions(), true);
        this.linkInterfaces();
        this.clampPages();
        this.setChanged();
        this.sync();
        this.updateLitState();
    }

    private void clearStructureState() {
        this.returnLaneInputsToStorage();
        this.setShellFormedState(this.collectCurrentShellPositions(), false);
        this.unlinkInterfaces();
        this.formed = false;
        this.outerSize = 0;
        this.minPos = this.worldPosition;
        this.maxPos = this.worldPosition;
        this.linkedInterfaces.clear();
        this.litGraceTicks = 0;
        this.clampPages();
        this.setChanged();
        this.sync();
        this.updateLitState();
    }

    private void linkInterfaces() {
        if (this.level == null) {
            return;
        }

        for (BlockPos interfacePos : this.linkedInterfaces) {
            if (this.level.getBlockEntity(interfacePos) instanceof ColossalFurnaceInterfaceBlockEntity interfaceBlockEntity) {
                interfaceBlockEntity.setController(this.worldPosition, true);
            }
        }
    }

    private void unlinkInterfaces() {
        if (this.level == null) {
            return;
        }

        for (BlockPos interfacePos : this.linkedInterfaces) {
            if (this.level.getBlockEntity(interfacePos) instanceof ColossalFurnaceInterfaceBlockEntity interfaceBlockEntity) {
                interfaceBlockEntity.setController(null, false);
            }
            BlockState state = this.level.getBlockState(interfacePos);
            if (state.hasProperty(ColossalFurnaceInterfaceBlock.LIT) && state.getValue(ColossalFurnaceInterfaceBlock.LIT)) {
                this.level.setBlock(interfacePos, state.setValue(ColossalFurnaceInterfaceBlock.LIT, false), 3);
            }
        }
    }

    private Set<BlockPos> collectCurrentShellPositions() {
        Set<BlockPos> shellPositions = new HashSet<>();
        if (this.level == null) {
            return shellPositions;
        }

        for (BlockPos pos : BlockPos.betweenClosed(this.minPos, this.maxPos)) {
            BlockPos immutablePos = pos.immutable();
            if (this.isShellBlock(this.level.getBlockState(immutablePos))) {
                shellPositions.add(immutablePos);
            }
        }
        return shellPositions;
    }

    private void setShellFormedState(Set<BlockPos> shellPositions, boolean formedState) {
        if (this.level == null) {
            return;
        }

        // Apply the final light state directly so unchanged revalidation never extinguishes the shell.
        Set<BlockPos> lightSources = formedState && (this.heatBuffer > 0 || this.litGraceTicks > 0)
                ? this.getFrontLightSourcePositions() : Set.of();
        for (BlockPos shellPos : shellPositions) {
            BlockState state = this.level.getBlockState(shellPos);
            if (state.getBlock() instanceof ColossalFurnaceWallBlock && state.hasProperty(ColossalFurnaceWallBlock.FORMED)) {
                this.level.setBlock(shellPos, state.setValue(ColossalFurnaceWallBlock.FORMED, formedState)
                        .setValue(ColossalFurnaceWallBlock.LIT, lightSources.contains(shellPos)), 3);
            } else if (state.getBlock() instanceof ColossalFurnaceInterfaceBlock && state.hasProperty(ColossalFurnaceInterfaceBlock.FORMED)) {
                this.level.setBlock(shellPos, state.setValue(ColossalFurnaceInterfaceBlock.FORMED, formedState)
                        .setValue(ColossalFurnaceInterfaceBlock.LIT, lightSources.contains(shellPos)), 3);
            } else if (state.getBlock() instanceof ColossalFurnaceControllerBlock && state.hasProperty(ColossalFurnaceControllerBlock.FORMED)) {
                this.level.setBlock(shellPos, state.setValue(ColossalFurnaceControllerBlock.FORMED, formedState), 3);
            } else if (state.getBlock() instanceof ColossalSmokerControllerBlock && state.hasProperty(ColossalFurnaceControllerBlock.FORMED)) {
                this.level.setBlock(shellPos, state.setValue(ColossalFurnaceControllerBlock.FORMED, formedState), 3);
            }
        }
        this.lastAppliedShellLightState = null;
    }

    private void returnLaneInputsToStorage() {
        for (LaneState laneState : this.laneStates) {
            if (laneState.active()) {
                if (laneState.inputSlot < 0) {
                    this.insertIntoHandler(this.inputHandler, laneState.input.copy(), this.getInputSlotCount());
                }
                laneState.clear();
            }
        }
    }

    private boolean assignIdleLanes() {
        boolean changed = false;
        LaneState laneState = this.getPrimaryLane();
        if (laneState.active()) {
            return false;
        }
        if (this.heatBuffer <= 0 && !this.hasFuelAvailable()) {
            return false;
        }

        for (int slot = 0; slot < this.getInputSlotCount(); slot++) {
            ItemStack stack = this.inputHandler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>> recipe = this.getRecipeFor(stack);
            if (recipe.isEmpty()) {
                continue;
            }

            AbstractCookingRecipe cookingRecipe = recipe.get().value();
            ItemStack output = cookingRecipe.assemble(new SingleRecipeInput(stack.copyWithCount(1)));
            if (output.isEmpty() || !this.canAcceptOutput(output)) {
                continue;
            }

            laneState.input = stack.copyWithCount(1);
            laneState.inputSlot = slot;
            laneState.output = output.copy();
            laneState.recipeId = recipe.get().id();
            laneState.progress = 0;
            laneState.cookTimeTotal = Math.max(1, cookingRecipe.cookingTime());
            changed = true;
            break;
        }
        return changed;
    }

    private boolean processSmeltingWork() {
        LaneState laneState = this.getPrimaryLane();
        boolean changed = false;
        if (laneState.active() && !this.hasMatchingLaneInput(laneState)) {
            if (laneState.inputSlot < 0) {
                this.insertIntoHandler(this.inputHandler, laneState.input.copy(), this.getInputSlotCount());
            }
            laneState.clear();
            changed = true;
        }
        changed |= this.assignIdleLanes();
        if (!laneState.active()) {
            return changed;
        }
        if (!this.canAcceptOutput(laneState.output)) {
            return changed;
        }

        int workBudget = this.getProcessingParallelism();
        int carriedWork = 0;

        while (workBudget > 0 || carriedWork > 0) {
            if (!laneState.active()) {
                changed |= this.assignIdleLanes();
                if (!laneState.active()) {
                    break;
                }
                if (!this.canAcceptOutput(laneState.output)) {
                    break;
                }
            }

            if (carriedWork == 0) {
                if (this.heatBuffer <= 0 && !this.consumeOneFuel()) {
                    changed |= this.decayCookingProgress(laneState);
                    break;
                }

                int workChunk = workBudget;
                if (this.currentFuelBurnRemaining > 0) {
                    workChunk = Math.min(workChunk, this.currentFuelBurnRemaining);
                }
                workChunk = Math.min(workChunk, this.heatBuffer);
                if (workChunk <= 0) {
                    if (!this.consumeOneFuel()) {
                        break;
                    }
                    continue;
                }

                this.heatBuffer -= workChunk;
                this.consumedHeatThisTick = true;
                this.currentFuelBurnRemaining = Math.max(0, this.currentFuelBurnRemaining - workChunk);
                workBudget -= workChunk;
                carriedWork = workChunk;
                changed = true;
            }

            int needed = laneState.cookTimeTotal - laneState.progress;
            int applied = Math.min(carriedWork, needed);
            laneState.progress += applied;
            carriedWork -= applied;

            if (laneState.progress >= laneState.cookTimeTotal) {
                ItemStack consumedInput = this.inputHandler.extractItem(laneState.inputSlot, 1, false);
                if (!ItemStack.matches(consumedInput, laneState.input)) {
                    if (!consumedInput.isEmpty()) {
                        this.insertIntoHandler(this.inputHandler, consumedInput, this.getInputSlotCount());
                    }
                    laneState.clear();
                    changed = true;
                    continue;
                }
                ItemStack remainder = this.insertOutput(laneState.output.copy());
                if (!remainder.isEmpty()) {
                    this.insertIntoHandler(this.inputHandler, consumedInput, this.getInputSlotCount());
                    laneState.progress = laneState.cookTimeTotal;
                    break;
                }
                this.recordRecipeUsed(laneState.recipeId);
                laneState.clear();
                changed = true;
            }
        }

        return changed;
    }

    private boolean decayCookingProgress(LaneState laneState) {
        int previousProgress = laneState.progress;
        laneState.progress = Math.max(0, laneState.progress - COOKING_PROGRESS_DECAY);
        return laneState.progress != previousProgress;
    }

    private boolean hasMatchingLaneInput(LaneState laneState) {
        if (laneState.inputSlot < 0 || laneState.inputSlot >= this.getInputSlotCount()) {
            return false;
        }
        ItemStack sourceStack = this.inputHandler.getStackInSlot(laneState.inputSlot);
        return !sourceStack.isEmpty()
                && ItemStack.matches(sourceStack.copyWithCount(1), laneState.input);
    }

    protected Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>> getRecipeFor(ItemStack stack) {
        if (!(this.level instanceof ServerLevel serverLevel) || stack.isEmpty()) {
            return Optional.empty();
        }
        SingleRecipeInput input = new SingleRecipeInput(stack.copyWithCount(1));
        return serverLevel.recipeAccess().getRecipeFor(this.getRecipeType(), input, serverLevel);
    }

    public boolean isSmeltable(ItemStack stack) {
        return this.getRecipeFor(stack).isPresent();
    }

    public boolean isFuel(ItemStack stack) {
        return this.level != null && stack.getBurnTime(this.getRecipeType(), this.level.fuelValues()) > 0;
    }

    private boolean hasHeatDemand() {
        if (this.getPrimaryLane().active()) {
            return true;
        }

        for (int slot = 0; slot < this.getInputSlotCount(); slot++) {
            if (this.isSmeltable(this.inputHandler.getStackInSlot(slot))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFuelAvailable() {
        for (int slot = 0; slot < this.getFuelSlotCount(); slot++) {
            if (this.isFuel(this.fuelHandler.getStackInSlot(slot))) {
                return true;
            }
        }
        return false;
    }

    private boolean consumeOneFuel() {
        for (int slot = 0; slot < this.getFuelSlotCount(); slot++) {
            ItemStack fuelStack = this.fuelHandler.getStackInSlot(slot);
            int burnTime = this.level == null ? 0 : fuelStack.getBurnTime(this.getRecipeType(), this.level.fuelValues());
            if (fuelStack.isEmpty() || burnTime <= 0) {
                continue;
            }

            ItemStack stackCopy = fuelStack.copy();
            stackCopy.shrink(1);
            ItemStackTemplate remainder = fuelStack.getCraftingRemainder();
            ItemStack containerItem = remainder == null ? ItemStack.EMPTY : remainder.create();
            if (stackCopy.isEmpty()) {
                this.fuelHandler.setStackInSlot(slot, containerItem);
            } else {
                this.fuelHandler.setStackInSlot(slot, stackCopy);
            }
            this.heatBuffer += burnTime;
            this.currentFuelBurnRemaining = burnTime;
            this.currentFuelBurnTotal = burnTime;
            return true;
        }
        return false;
    }

    private boolean drainIdleFuel() {
        if (this.currentFuelBurnRemaining <= 0 || this.heatBuffer <= 0) {
            return false;
        }

        this.currentFuelBurnRemaining--;
        this.heatBuffer--;
        this.consumedHeatThisTick = true;
        return true;
    }

    protected void spawnWorkingParticles() {
        if (this.level == null) {
            return;
        }

        int size = Math.max(2, this.outerSize);
        if (this.level.getRandom().nextFloat() < this.getSmokeParticleChance(size)) {
            this.spawnFrontParticle(false);
        }
        if (this.level.getRandom().nextFloat() < this.getFlameParticleChance(size)) {
            this.spawnFrontParticle(true);
        }
    }

    protected float getSmokeParticleChance(int size) {
        return Mth.clamp(0.18F + (size - 2) * 0.04F, 0.18F, 0.30F);
    }

    private float getFlameParticleChance(int size) {
        return Mth.clamp(0.10F + (size - 2) * 0.03F, 0.10F, 0.19F);
    }

    protected void spawnFrontSmokeParticle(double normalizedHeight) {
        if (this.level == null) {
            return;
        }

        double width = this.maxPos.getX() - this.minPos.getX() + 1.0D;
        double height = this.maxPos.getY() - this.minPos.getY() + 1.0D;
        double depth = this.maxPos.getZ() - this.minPos.getZ() + 1.0D;
        double x = this.minPos.getX() + width * 0.5D;
        double y = this.minPos.getY() + this.level.getRandom().nextDouble() * normalizedHeight * height;
        double z = this.minPos.getZ() + depth * 0.5D;
        double sideways = this.level.getRandom().nextDouble() * 0.6D - 0.3D;
        Direction front = this.getStructureFacing();
        // Vanilla blast furnace: smoke across the middle 60% of the front, with no supplied velocity.
        if (front.getAxis() == Direction.Axis.X) {
            x += front.getStepX() * (width * 0.5D + ModParticles.frontOffsetForSize(this.outerSize));
            z += sideways * depth;
        } else {
            x += sideways * width;
            z += front.getStepZ() * (depth * 0.5D + ModParticles.frontOffsetForSize(this.outerSize));
        }
        this.level.addParticle(ModParticles.smokeForSize(this.outerSize), x, y, z, 0.0D, 0.0D, 0.0D);
    }

    private void spawnFrontParticle(boolean flame) {
        if (this.level == null) {
            return;
        }

        double uMin = flame ? 0.25D : 0.1875D;
        double uMax = flame ? 0.75D : 0.8125D;
        double vMin = 0.125D;
        double vMax = flame ? 0.375D : 0.4375D;
        double u = Mth.lerp(this.level.getRandom().nextDouble(), uMin, uMax);
        double v = Mth.lerp(this.level.getRandom().nextDouble(), vMin, vMax);
        double minX = this.minPos.getX();
        double minY = this.minPos.getY();
        double minZ = this.minPos.getZ();
        double maxX = this.maxPos.getX() + 1.0D;
        double maxZ = this.maxPos.getZ() + 1.0D;
        double maxY = this.maxPos.getY() + 1.0D;
        double epsilon = ModParticles.frontOffsetForSize(this.outerSize);
        double x;
        double y;
        double z;
        double vx;
        double vy = Mth.lerp(this.level.getRandom().nextDouble(), 0.02D, 0.05D);
        double vz;
        Direction front = this.getStructureFacing();

        switch (front) {
            case SOUTH -> {
                x = Mth.lerp(u, minX, maxX);
                y = Mth.lerp(v, minY, maxY);
                z = maxZ + epsilon;
                vx = Mth.lerp(this.level.getRandom().nextDouble(), -0.01D, 0.01D);
                vz = Mth.lerp(this.level.getRandom().nextDouble(), 0.01D, 0.03D);
            }
            case WEST -> {
                x = minX - epsilon;
                y = Mth.lerp(v, minY, maxY);
                z = Mth.lerp(u, minZ, maxZ);
                vx = Mth.lerp(this.level.getRandom().nextDouble(), -0.03D, -0.01D);
                vz = Mth.lerp(this.level.getRandom().nextDouble(), -0.01D, 0.01D);
            }
            case EAST -> {
                x = maxX + epsilon;
                y = Mth.lerp(v, minY, maxY);
                z = Mth.lerp(u, minZ, maxZ);
                vx = Mth.lerp(this.level.getRandom().nextDouble(), 0.01D, 0.03D);
                vz = Mth.lerp(this.level.getRandom().nextDouble(), -0.01D, 0.01D);
            }
            default -> {
                x = Mth.lerp(u, minX, maxX);
                y = Mth.lerp(v, minY, maxY);
                z = minZ - epsilon;
                vx = Mth.lerp(this.level.getRandom().nextDouble(), -0.01D, 0.01D);
                vz = Mth.lerp(this.level.getRandom().nextDouble(), -0.03D, -0.01D);
            }
        }

        if (flame) {
            // Vanilla furnace flames receive no supplied velocity; smoke still rises independently.
            this.level.addParticle(ModParticles.flameForSize(this.outerSize), x, y, z, 0.0D, 0.0D, 0.0D);
        } else {
            this.level.addParticle(ModParticles.smokeForSize(this.outerSize), x, y, z, vx, vy, vz);
        }
    }

    private boolean canAcceptOutput(ItemStack stack) {
        return this.simulateInsert(this.outputHandler, stack.copy(), this.getOutputSlotCount()).isEmpty();
    }

    private ItemStack insertOutput(ItemStack stack) {
        return this.insertIntoHandler(this.outputHandler, stack, this.getOutputSlotCount());
    }

    private ItemStack insertIntoHandler(ItemStackHandler handler, ItemStack stack, int slotCount) {
        ItemStack remainder = stack;
        for (int slot = 0; slot < slotCount && !remainder.isEmpty(); slot++) {
            remainder = handler.insertItem(slot, remainder, false);
        }
        return remainder;
    }

    private ItemStack simulateInsert(ItemStackHandler handler, ItemStack stack, int slotCount) {
        ItemStack remainder = stack;
        for (int slot = 0; slot < slotCount && !remainder.isEmpty(); slot++) {
            remainder = handler.insertItem(slot, remainder, true);
        }
        return remainder;
    }

    public boolean isTrackingPosition(BlockPos changedPos) {
        return this.formed && changedPos.getX() >= this.minPos.getX() && changedPos.getX() <= this.maxPos.getX()
                && changedPos.getY() >= this.minPos.getY() && changedPos.getY() <= this.maxPos.getY()
                && changedPos.getZ() >= this.minPos.getZ() && changedPos.getZ() <= this.maxPos.getZ();
    }

    public void dropAllContents() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        this.returnLaneInputsToStorage();
        this.dropHandlerContents(this.inputHandler, this.getInputSlotCount());
        this.dropHandlerContents(this.fuelHandler, this.getFuelSlotCount());
        this.dropHandlerContents(this.outputHandler, this.getOutputSlotCount());
        if (this.level instanceof ServerLevel serverLevel) {
            this.collectUsedRecipesAndPopExperience(serverLevel, Vec3.atCenterOf(this.worldPosition));
        }
        this.clearStructureState();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        this.dropAllContents();
        super.preRemoveSideEffects(pos, state);
    }

    private void recordRecipeUsed(@Nullable ResourceKey<Recipe<?>> recipeId) {
        if (recipeId != null) {
            this.recipesUsed.addTo(recipeId, 1);
        }
    }

    public void awardUsedRecipesAndPopExperience(ServerPlayer player) {
        List<RecipeHolder<?>> recipes = this.collectUsedRecipesAndPopExperience(player.level(), player.position());
        player.awardRecipes(recipes);

        List<ItemStack> inventoryStacks = this.getInventoryStacksForRecipeTriggers();
        for (RecipeHolder<?> recipe : recipes) {
            player.triggerRecipeCrafted(recipe, inventoryStacks);
        }
    }

    public List<RecipeHolder<?>> collectUsedRecipesAndPopExperience(ServerLevel level, Vec3 position) {
        List<RecipeHolder<?>> recipes = new ArrayList<>();
        for (Object2IntMap.Entry<ResourceKey<Recipe<?>>> entry : this.recipesUsed.object2IntEntrySet()) {
            level.recipeAccess().byKey(entry.getKey()).ifPresent(recipe -> {
                if (recipe.value() instanceof AbstractCookingRecipe cookingRecipe) {
                    recipes.add(recipe);
                    createExperience(level, position, entry.getIntValue(), cookingRecipe.experience());
                }
            });
        }
        this.recipesUsed.clear();
        this.setChanged();
        return recipes;
    }

    private List<ItemStack> getInventoryStacksForRecipeTriggers() {
        List<ItemStack> stacks = new ArrayList<>(this.getInputSlotCount() + this.getFuelSlotCount() + this.getOutputSlotCount());
        this.addHandlerStacks(stacks, this.inputHandler, this.getInputSlotCount());
        this.addHandlerStacks(stacks, this.fuelHandler, this.getFuelSlotCount());
        this.addHandlerStacks(stacks, this.outputHandler, this.getOutputSlotCount());
        return stacks;
    }

    private void addHandlerStacks(List<ItemStack> stacks, ItemStackHandler handler, int slotCount) {
        for (int slot = 0; slot < slotCount; slot++) {
            stacks.add(handler.getStackInSlot(slot));
        }
    }

    private static void createExperience(ServerLevel level, Vec3 position, int recipeCount, float experience) {
        int amount = Mth.floor(recipeCount * experience);
        float remainder = Mth.frac(recipeCount * experience);
        if (remainder != 0.0F && Math.random() < remainder) {
            amount++;
        }
        ExperienceOrb.award(level, position, amount);
    }

    private void dropHandlerContents(ItemStackHandler handler, int slotCount) {
        if (this.level == null) {
            return;
        }

        for (int slot = 0; slot < slotCount; slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), stack.copy());
                handler.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    public int getComparatorOutput() {
        int filledSlots = 0;
        int totalSlots = this.getInputSlotCount() + this.getFuelSlotCount() + this.getOutputSlotCount();
        for (int slot = 0; slot < this.getInputSlotCount(); slot++) {
            if (!this.inputHandler.getStackInSlot(slot).isEmpty()) {
                filledSlots++;
            }
        }
        for (int slot = 0; slot < this.getFuelSlotCount(); slot++) {
            if (!this.fuelHandler.getStackInSlot(slot).isEmpty()) {
                filledSlots++;
            }
        }
        for (int slot = 0; slot < this.getOutputSlotCount(); slot++) {
            if (!this.outputHandler.getStackInSlot(slot).isEmpty()) {
                filledSlots++;
            }
        }
        return totalSlots == 0 ? 0 : Mth.floor((filledSlots / (float) totalSlots) * 14.0F) + (filledSlots > 0 ? 1 : 0);
    }

    public boolean isFormed() {
        return this.formed;
    }

    public int getOuterSize() {
        return this.outerSize;
    }

    public int getProcessingParallelism() {
        return this.formed ? Math.max(1, this.outerSize * this.outerSize) : 0;
    }

    protected MultiblockValidationResult validateStructure(Level level, BlockPos origin) {
        return ColossalFurnaceStructure.validate(level, origin);
    }

    protected boolean isShellBlock(BlockState state) {
        return ColossalFurnaceStructure.isShell(state);
    }

    protected RecipeType<? extends AbstractCookingRecipe> getRecipeType() {
        return RecipeType.SMELTING;
    }

    public int getStoredParallelism() {
        return this.inventoryParallelism;
    }

    public int getInputSlotCount() {
        return MAX_INPUT_SLOTS;
    }

    public int getFuelSlotCount() {
        return MAX_FUEL_SLOTS;
    }

    public int getOutputSlotCount() {
        return MAX_OUTPUT_SLOTS;
    }

    public int getActiveLaneCount() {
        return this.getPrimaryLane().active() ? 1 : 0;
    }

    public int getHeatBuffer() {
        return this.heatBuffer;
    }

    public boolean isMenuLit() {
        return this.formed && this.heatBuffer > 0;
    }

    public int getMenuLitProgress() {
        if (!this.isMenuLit() || this.currentFuelBurnRemaining <= 0 || this.currentFuelBurnTotal <= 0) {
            return 0;
        }
        return Mth.clamp((this.currentFuelBurnRemaining * 13) / this.currentFuelBurnTotal, 0, 13);
    }

    public int getMenuBurnProgress() {
        LaneState laneState = this.getPrimaryLane();
        if (laneState.cookTimeTotal <= 0) {
            return 0;
        }
        return Mth.clamp((laneState.progress * 24) / laneState.cookTimeTotal, 0, 24);
    }

    public BlockPos getMinPos() {
        return this.minPos;
    }

    public BlockPos getMaxPos() {
        return this.maxPos;
    }

    public Set<BlockPos> getLinkedInterfacePositions() {
        return Set.copyOf(this.linkedInterfaces);
    }

    public int getInputPageCount() {
        return 1;
    }

    public int getFuelPageCount() {
        return 1;
    }

    public int getOutputPageCount() {
        return 1;
    }

    public int getInputPage() {
        return this.inputPage;
    }

    public int getFuelPage() {
        return this.fuelPage;
    }

    public int getOutputPage() {
        return this.outputPage;
    }

    public void shiftInputPage(int delta) {
        this.inputPage = 0;
    }

    public void shiftFuelPage(int delta) {
        this.fuelPage = 0;
    }

    public void shiftOutputPage(int delta) {
        this.outputPage = 0;
    }

    private void clampPages() {
        this.inputPage = 0;
        this.fuelPage = 0;
        this.outputPage = 0;
    }

    public IItemHandlerModifiable getInputHandler() {
        return this.inputHandler;
    }

    public IItemHandlerModifiable getFuelHandler() {
        return this.fuelHandler;
    }

    public IItemHandlerModifiable getOutputHandler() {
        return this.outputHandler;
    }

    public ContainerData getMenuData() {
        return this.menuData;
    }

    public Direction getStructureFacing() {
        return this.getBlockState().hasProperty(ColossalFurnaceControllerBlock.FACING)
                ? this.getBlockState().getValue(ColossalFurnaceControllerBlock.FACING)
                : Direction.NORTH;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.colossalfurnaces.colossal_furnace");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (!this.formed) {
            return null;
        }
        return new ColossalFurnaceMenu(containerId, inventory, this, this.menuData);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.readChild("Input", this.inputHandler);
        input.readChild("Fuel", this.fuelHandler);
        input.readChild("Output", this.outputHandler);
        this.formed = input.getBooleanOr("Formed", false);
        this.outerSize = input.getIntOr("OuterSize", 0);
        this.inventoryParallelism = Mth.clamp(input.getIntOr("InventoryParallelism", 1), 1, MAX_PARALLELISM);
        this.heatBuffer = Math.max(0, input.getIntOr("HeatBuffer", 0));
        this.currentFuelBurnRemaining = Math.max(0, input.getIntOr("CurrentFuelBurnRemaining", this.heatBuffer));
        this.currentFuelBurnTotal = Math.max(0, input.getIntOr("CurrentFuelBurnTotal", this.currentFuelBurnRemaining));
        this.inputPage = Math.max(0, input.getIntOr("InputPage", 0));
        this.fuelPage = Math.max(0, input.getIntOr("FuelPage", 0));
        this.outputPage = Math.max(0, input.getIntOr("OutputPage", 0));
        this.minPos = input.read("MinPos", BlockPos.CODEC).orElse(this.worldPosition);
        this.maxPos = input.read("MaxPos", BlockPos.CODEC).orElse(this.worldPosition);
        this.linkedInterfaces.clear();
        input.listOrEmpty("Interfaces", BlockPos.CODEC).forEach(this.linkedInterfaces::add);

        for (int index = 0; index < this.laneStates.size(); index++) {
            this.laneStates.get(index).clear();
        }
        int laneIndex = 0;
        for (ValueInput laneInput : input.childrenListOrEmpty("Lanes")) {
            if (laneIndex >= this.laneStates.size()) break;
            this.laneStates.get(laneIndex++).load(laneInput);
        }
        this.recipesUsed.clear();
        for (ValueInput recipeInput : input.childrenListOrEmpty("RecipesUsed")) {
            recipeInput.read("RecipeId", Recipe.KEY_CODEC).ifPresent(key ->
                    this.recipesUsed.put(key, recipeInput.getIntOr("Count", 0)));
        }
        this.clampPages();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putChild("Input", this.inputHandler);
        output.putChild("Fuel", this.fuelHandler);
        output.putChild("Output", this.outputHandler);
        output.putBoolean("Formed", this.formed);
        output.putInt("OuterSize", this.outerSize);
        output.putInt("InventoryParallelism", this.inventoryParallelism);
        output.putInt("HeatBuffer", this.heatBuffer);
        output.putInt("CurrentFuelBurnRemaining", this.currentFuelBurnRemaining);
        output.putInt("CurrentFuelBurnTotal", this.currentFuelBurnTotal);
        output.putInt("InputPage", this.inputPage);
        output.putInt("FuelPage", this.fuelPage);
        output.putInt("OutputPage", this.outputPage);
        output.store("MinPos", BlockPos.CODEC, this.minPos);
        output.store("MaxPos", BlockPos.CODEC, this.maxPos);

        ValueOutput.TypedOutputList<BlockPos> interfaceList = output.list("Interfaces", BlockPos.CODEC);
        for (BlockPos interfacePos : this.linkedInterfaces) {
            interfaceList.add(interfacePos);
        }

        ValueOutput.ValueOutputList laneList = output.childrenList("Lanes");
        for (LaneState laneState : this.laneStates) {
            laneState.save(laneList.addChild());
        }
        ValueOutput.ValueOutputList recipeList = output.childrenList("RecipesUsed");
        this.recipesUsed.forEach((recipeId, count) -> {
            ValueOutput recipeOutput = recipeList.addChild();
            recipeOutput.store("RecipeId", Recipe.KEY_CODEC, recipeId);
            recipeOutput.putInt("Count", count);
        });
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public IItemHandler getAutomationHandler() {
        return this.automationHandler;
    }

    public ResourceHandler<ItemResource> getAutomationTransferHandler() {
        return this.automationTransferHandler;
    }

    public int getStoredRecipeUseCount() {
        return this.recipesUsed.values().intStream().sum();
    }

    public int getPrimaryLaneProgress() {
        return this.getPrimaryLane().progress;
    }

    private void updateLitState() {
        if (this.level == null || this.isRemoved()) {
            return;
        }

        // Removal callbacks see the new world state while this entity still caches the old Core state.
        BlockState state = this.level.getBlockState(this.worldPosition);
        if (!state.is(this.getBlockState().getBlock())) {
            return;
        }
        boolean shouldBeLit = this.formed && (this.heatBuffer > 0 || this.litGraceTicks > 0);
        if (state.hasProperty(ColossalFurnaceControllerBlock.LIT) && state.getValue(ColossalFurnaceControllerBlock.LIT) != shouldBeLit) {
            this.level.setBlock(this.worldPosition, state.setValue(ColossalFurnaceControllerBlock.LIT, shouldBeLit), 3);
        }
        if (this.lastAppliedShellLightState == null || this.lastAppliedShellLightState != shouldBeLit) {
            this.updateShellLightSources(shouldBeLit);
            this.lastAppliedShellLightState = shouldBeLit;
        }
    }

    private void updateShellLightSources(boolean active) {
        if (this.level == null || !this.formed) {
            return;
        }

        Set<BlockPos> lightSources = active ? this.getFrontLightSourcePositions() : Set.of();
        for (BlockPos shellPos : this.collectCurrentShellPositions()) {
            BlockState state = this.level.getBlockState(shellPos);
            BooleanProperty lightProperty = null;
            if (state.getBlock() instanceof ColossalFurnaceWallBlock) {
                lightProperty = ColossalFurnaceWallBlock.LIT;
            } else if (state.getBlock() instanceof ColossalFurnaceInterfaceBlock) {
                lightProperty = ColossalFurnaceInterfaceBlock.LIT;
            }
            if (lightProperty == null) {
                continue;
            }

            boolean shouldEmit = lightSources.contains(shellPos);
            if (state.getValue(lightProperty) != shouldEmit) {
                this.level.setBlock(shellPos, state.setValue(lightProperty, shouldEmit), 3);
            }
        }
    }

    private Set<BlockPos> getFrontLightSourcePositions() {
        Direction front = this.getStructureFacing();
        List<BlockPos> candidates = new ArrayList<>();
        int centerSum;
        if (front.getAxis() == Direction.Axis.Z) {
            int z = front == Direction.NORTH ? this.minPos.getZ() : this.maxPos.getZ();
            centerSum = this.minPos.getX() + this.maxPos.getX();
            for (int x = this.minPos.getX(); x <= this.maxPos.getX(); x++) {
                candidates.add(new BlockPos(x, this.minPos.getY(), z));
            }
            candidates.sort(Comparator.comparingInt(pos -> Math.abs(pos.getX() * 2 - centerSum)));
        } else {
            int x = front == Direction.WEST ? this.minPos.getX() : this.maxPos.getX();
            centerSum = this.minPos.getZ() + this.maxPos.getZ();
            for (int z = this.minPos.getZ(); z <= this.maxPos.getZ(); z++) {
                candidates.add(new BlockPos(x, this.minPos.getY(), z));
            }
            candidates.sort(Comparator.comparingInt(pos -> Math.abs(pos.getZ() * 2 - centerSum)));
        }

        candidates.removeIf(pos -> pos.equals(this.worldPosition)
                || !(this.level.getBlockState(pos).getBlock() instanceof ColossalFurnaceWallBlock
                || this.level.getBlockState(pos).getBlock() instanceof ColossalFurnaceInterfaceBlock));
        int sourceCount = Math.min(Math.max(1, this.outerSize - 2), candidates.size());
        return new HashSet<>(candidates.subList(0, sourceCount));
    }

    private void tickLitGracePeriod() {
        // Short fuels can be fully consumed within one tick and must still activate working visuals.
        if (this.heatBuffer > 0 || this.consumedHeatThisTick) {
            this.litGraceTicks = LIT_GRACE_TICKS;
        } else if (this.litGraceTicks > 0) {
            this.litGraceTicks--;
        }
    }

    private void sync() {
        if (this.level != null && !this.isRemoved()) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            if (state.is(this.getBlockState().getBlock())) {
                this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
            }
        }
    }

    private LaneState getPrimaryLane() {
        return this.laneStates.get(0);
    }

    private final class InputHandler extends ItemStackHandler {
        private InputHandler(int size) {
            super(size);
        }

        @Override
        protected void onContentsChanged(int slot) {
            ColossalFurnaceControllerBlockEntity.this.setChanged();
        }
    }

    private final class FuelHandler extends ItemStackHandler {
        private FuelHandler(int size) {
            super(size);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return ColossalFurnaceControllerBlockEntity.this.isFuel(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            ColossalFurnaceControllerBlockEntity.this.setChanged();
        }
    }

    private final class OutputHandler extends ItemStackHandler {
        private OutputHandler(int size) {
            super(size);
        }

        @Override
        protected void onContentsChanged(int slot) {
            ColossalFurnaceControllerBlockEntity.this.setChanged();
        }
    }

    private static final class LaneState {
        private ItemStack input = ItemStack.EMPTY;
        private int inputSlot = -1;
        private ItemStack output = ItemStack.EMPTY;
        @Nullable
        private ResourceKey<Recipe<?>> recipeId;
        private int progress;
        private int cookTimeTotal;

        private boolean active() {
            return !this.input.isEmpty() && !this.output.isEmpty();
        }

        private void clear() {
            this.input = ItemStack.EMPTY;
            this.inputSlot = -1;
            this.output = ItemStack.EMPTY;
            this.recipeId = null;
            this.progress = 0;
            this.cookTimeTotal = 0;
        }

        private void save(ValueOutput output) {
            output.store("Input", ItemStack.OPTIONAL_CODEC, this.input);
            output.putInt("InputSlot", this.inputSlot);
            output.store("Output", ItemStack.OPTIONAL_CODEC, this.output);
            if (this.recipeId != null) {
                output.store("RecipeId", Recipe.KEY_CODEC, this.recipeId);
            }
            output.putInt("Progress", this.progress);
            output.putInt("CookTimeTotal", this.cookTimeTotal);
        }

        private void load(ValueInput input) {
            this.input = input.read("Input", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
            this.inputSlot = input.getIntOr("InputSlot", -1);
            this.output = input.read("Output", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
            this.recipeId = input.read("RecipeId", Recipe.KEY_CODEC).orElse(null);
            this.progress = input.getIntOr("Progress", 0);
            this.cookTimeTotal = input.getIntOr("CookTimeTotal", 0);
        }
    }

    private static final class AutomationItemHandler implements IItemHandlerModifiable {
        private final ColossalFurnaceControllerBlockEntity controller;

        private AutomationItemHandler(ColossalFurnaceControllerBlockEntity controller) {
            this.controller = controller;
        }

        @Override
        public int getSlots() {
            return this.controller.getInputSlotCount() + this.controller.getFuelSlotCount() + this.controller.getOutputSlotCount();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            Section section = this.resolve(slot);
            return switch (section.kind()) {
                case INPUT -> this.controller.inputHandler.getStackInSlot(section.index());
                case FUEL -> this.controller.fuelHandler.getStackInSlot(section.index());
                case OUTPUT -> this.controller.outputHandler.getStackInSlot(section.index());
                case INVALID -> ItemStack.EMPTY;
            };
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            Section section = this.resolve(slot);
            switch (section.kind()) {
                case INPUT -> this.controller.inputHandler.setStackInSlot(section.index(), stack);
                case FUEL -> this.controller.fuelHandler.setStackInSlot(section.index(), stack);
                case OUTPUT -> this.controller.outputHandler.setStackInSlot(section.index(), stack);
                case INVALID -> {
                }
            }
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            Section section = this.resolve(slot);
            return switch (section.kind()) {
                case INPUT -> this.controller.isSmeltable(stack) ? this.controller.inputHandler.insertItem(section.index(), stack, simulate) : stack;
                case FUEL -> this.controller.isFuel(stack) ? this.controller.fuelHandler.insertItem(section.index(), stack, simulate) : stack;
                case OUTPUT, INVALID -> stack;
            };
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            Section section = this.resolve(slot);
            return switch (section.kind()) {
                case OUTPUT -> this.controller.outputHandler.extractItem(section.index(), amount, simulate);
                default -> ItemStack.EMPTY;
            };
        }

        @Override
        public int getSlotLimit(int slot) {
            Section section = this.resolve(slot);
            return switch (section.kind()) {
                case INPUT -> this.controller.inputHandler.getSlotLimit(section.index());
                case FUEL -> this.controller.fuelHandler.getSlotLimit(section.index());
                case OUTPUT -> this.controller.outputHandler.getSlotLimit(section.index());
                case INVALID -> 0;
            };
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            Section section = this.resolve(slot);
            return switch (section.kind()) {
                case INPUT -> this.controller.isSmeltable(stack);
                case FUEL -> this.controller.isFuel(stack);
                default -> false;
            };
        }

        private Section resolve(int slot) {
            int inputSlots = this.controller.getInputSlotCount();
            int fuelSlots = this.controller.getFuelSlotCount();
            int outputSlots = this.controller.getOutputSlotCount();
            if (slot < 0) {
                return new Section(SectionKind.INVALID, -1);
            }
            if (slot < inputSlots) {
                return new Section(SectionKind.INPUT, slot);
            }
            slot -= inputSlots;
            if (slot < fuelSlots) {
                return new Section(SectionKind.FUEL, slot);
            }
            slot -= fuelSlots;
            if (slot < outputSlots) {
                return new Section(SectionKind.OUTPUT, slot);
            }
            return new Section(SectionKind.INVALID, -1);
        }

        private record Section(SectionKind kind, int index) {
        }

        private enum SectionKind {
            INPUT,
            FUEL,
            OUTPUT,
            INVALID
        }
    }
}
