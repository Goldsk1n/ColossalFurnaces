package com.colossalfurnaces.blockentity;

import com.colossalfurnaces.registry.ModBlockEntities;
import com.colossalfurnaces.util.LegacyItemHandlerAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ColossalFurnaceInterfaceBlockEntity extends BlockEntity {
    private static final String TAG_MODE = "Mode";
    private static final long AUTO_TRANSFER_INTERVAL = 8L;
    private static final int AUTO_IMPORT_LIMIT = 64;
    private BlockPos controllerPos;
    private boolean formed;
    private ColossalFurnaceInterfaceMode mode = ColossalFurnaceInterfaceMode.UNIVERSAL;
    private final IItemHandlerModifiable proxyHandler;
    private final ResourceHandler<ItemResource> transferHandler;

    public ColossalFurnaceInterfaceBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.COLOSSAL_FURNACE_INTERFACE.get(), pos, state);
    }

    protected ColossalFurnaceInterfaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
        this.proxyHandler = this.resolveItemHandler();
        this.transferHandler = new LegacyItemHandlerAdapter(this.proxyHandler);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ColossalFurnaceInterfaceBlockEntity blockEntity) {
        if (level.isClientSide() || !blockEntity.formed) {
            return;
        }

        ColossalFurnaceControllerBlockEntity controller = blockEntity.getLinkedController();
        if (controller == null) {
            blockEntity.setController(null, false);
            return;
        }

        if (Math.floorMod(level.getGameTime() + pos.asLong(), AUTO_TRANSFER_INTERVAL) == 0) {
            switch (blockEntity.mode) {
                case INPUT, FUEL -> blockEntity.tryAutoPull(controller);
                case OUTPUT -> blockEntity.tryAutoPushOutput(controller);
                case UNIVERSAL -> {
                }
            }
        }
    }

    public void setController(@Nullable BlockPos controllerPos, boolean formed) {
        this.controllerPos = controllerPos;
        this.formed = formed;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public ColossalFurnaceInterfaceMode getMode() {
        return this.mode;
    }

    public void setMode(ColossalFurnaceInterfaceMode mode) {
        if (this.mode == mode) {
            return;
        }
        this.mode = mode;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public ColossalFurnaceInterfaceMode cycleMode() {
        ColossalFurnaceInterfaceMode nextMode = this.mode.next();
        this.setMode(nextMode);
        return nextMode;
    }

    @Nullable
    public ColossalFurnaceControllerBlockEntity getLinkedController() {
        if (this.level == null || this.controllerPos == null) {
            return null;
        }
        if (this.level.getBlockEntity(this.controllerPos) instanceof ColossalFurnaceControllerBlockEntity controller && controller.isFormed()) {
            return controller;
        }
        return null;
    }

    public void openLinkedMenu(Player player) {
        ColossalFurnaceControllerBlockEntity controller = this.getLinkedController();
        if (controller != null) {
            controller.openMenu(player);
            return;
        }
        player.sendOverlayMessage(Component.translatable("message.colossalfurnaces.structure.interface_unlinked"));
    }

    private void tryAutoPushOutput(ColossalFurnaceControllerBlockEntity controller) {
        for (Direction direction : this.getExternalDirections(controller)) {
            if (this.level == null) {
                continue;
            }
            BlockPos targetPos = this.worldPosition.relative(direction);
            ResourceHandler<ItemResource> targetHandler = this.level.getCapability(Capabilities.Item.BLOCK, targetPos, direction.getOpposite());
            if (targetHandler == null) {
                continue;
            }
            if (this.pushOneOutputStack(controller, targetHandler)) {
                return;
            }
        }
    }

    private boolean pushOneOutputStack(ColossalFurnaceControllerBlockEntity controller, ResourceHandler<ItemResource> targetHandler) {
        for (int slot = 0; slot < controller.getOutputSlotCount(); slot++) {
            ItemStack simulatedExtract = controller.getOutputHandler().extractItem(slot, controller.getOutputHandler().getSlotLimit(slot), true);
            if (simulatedExtract.isEmpty()) {
                continue;
            }

            ItemStack simulatedRemainder = ItemUtil.insertItemReturnRemaining(targetHandler, simulatedExtract, true, null);
            int movedAmount = simulatedExtract.getCount() - simulatedRemainder.getCount();
            if (movedAmount <= 0) {
                continue;
            }

            ItemStack extracted = controller.getOutputHandler().extractItem(slot, movedAmount, false);
            ItemStack remainder = ItemUtil.insertItemReturnRemaining(targetHandler, extracted, false, null);
            if (!remainder.isEmpty()) {
                controller.getOutputHandler().insertItem(slot, remainder, false);
            }
            return true;
        }
        return false;
    }

    private void tryAutoPull(ColossalFurnaceControllerBlockEntity controller) {
        if (this.level == null) {
            return;
        }
        for (Direction direction : this.getExternalDirections(controller)) {
            BlockPos sourcePos = this.worldPosition.relative(direction);
            if (!this.level.hasChunkAt(sourcePos)) {
                continue;
            }
            ResourceHandler<ItemResource> source = this.level.getCapability(
                    Capabilities.Item.BLOCK, sourcePos, direction.getOpposite());
            // Native transfer transactions roll both inventories back if extraction or insertion fails.
            if (ResourceHandlerUtil.moveFirst(source, this.transferHandler,
                    resource -> this.mode == ColossalFurnaceInterfaceMode.INPUT
                            ? controller.isSmeltable(resource.toStack()) : controller.isFuel(resource.toStack()),
                    AUTO_IMPORT_LIMIT, null) != null) {
                return;
            }
        }
    }

    private List<Direction> getExternalDirections(ColossalFurnaceControllerBlockEntity controller) {
        List<Direction> directions = new ArrayList<>(3);
        BlockPos minPos = controller.getMinPos();
        BlockPos maxPos = controller.getMaxPos();
        BlockPos pos = this.worldPosition;

        if (pos.getX() == minPos.getX()) {
            directions.add(Direction.WEST);
        }
        if (pos.getX() == maxPos.getX()) {
            directions.add(Direction.EAST);
        }
        if (pos.getY() == minPos.getY()) {
            directions.add(Direction.DOWN);
        }
        if (pos.getY() == maxPos.getY()) {
            directions.add(Direction.UP);
        }
        if (pos.getZ() == minPos.getZ()) {
            directions.add(Direction.NORTH);
        }
        if (pos.getZ() == maxPos.getZ()) {
            directions.add(Direction.SOUTH);
        }

        Direction facing = this.getBlockState().getOptionalValue(com.colossalfurnaces.block.ColossalFurnaceInterfaceBlock.FACING).orElse(null);
        if (facing != null) {
            int index = directions.indexOf(facing);
            if (index > 0) {
                directions.remove(index);
                directions.add(0, facing);
            }
        }

        return directions;
    }

    private IItemHandlerModifiable resolveItemHandler() {
        return new IItemHandlerModifiable() {
            @Override
            public int getSlots() {
                ColossalFurnaceControllerBlockEntity controller = ColossalFurnaceInterfaceBlockEntity.this.getLinkedController();
                if (controller == null) {
                    return 0;
                }
                return switch (ColossalFurnaceInterfaceBlockEntity.this.mode) {
                    case UNIVERSAL -> controller.getAutomationHandler().getSlots();
                    case INPUT -> controller.getInputSlotCount();
                    case FUEL -> controller.getFuelSlotCount();
                    case OUTPUT -> controller.getOutputSlotCount();
                };
            }

            @Override
            public @NotNull net.minecraft.world.item.ItemStack getStackInSlot(int slot) {
                ColossalFurnaceControllerBlockEntity controller = ColossalFurnaceInterfaceBlockEntity.this.getLinkedController();
                if (controller == null) {
                    return net.minecraft.world.item.ItemStack.EMPTY;
                }
                return switch (ColossalFurnaceInterfaceBlockEntity.this.mode) {
                    case UNIVERSAL -> controller.getAutomationHandler().getStackInSlot(slot);
                    case INPUT -> slot >= 0 && slot < controller.getInputSlotCount() ? controller.getInputHandler().getStackInSlot(slot) : net.minecraft.world.item.ItemStack.EMPTY;
                    case FUEL -> slot >= 0 && slot < controller.getFuelSlotCount() ? controller.getFuelHandler().getStackInSlot(slot) : net.minecraft.world.item.ItemStack.EMPTY;
                    case OUTPUT -> slot >= 0 && slot < controller.getOutputSlotCount() ? controller.getOutputHandler().getStackInSlot(slot) : net.minecraft.world.item.ItemStack.EMPTY;
                };
            }

            @Override
            public void setStackInSlot(int slot, @NotNull ItemStack stack) {
                ColossalFurnaceControllerBlockEntity controller = ColossalFurnaceInterfaceBlockEntity.this.getLinkedController();
                if (controller == null) {
                    return;
                }
                switch (ColossalFurnaceInterfaceBlockEntity.this.mode) {
                    case UNIVERSAL -> ((IItemHandlerModifiable) controller.getAutomationHandler()).setStackInSlot(slot, stack);
                    case INPUT -> {
                        if (slot >= 0 && slot < controller.getInputSlotCount()) {
                            controller.getInputHandler().setStackInSlot(slot, stack);
                        }
                    }
                    case FUEL -> {
                        if (slot >= 0 && slot < controller.getFuelSlotCount()) {
                            controller.getFuelHandler().setStackInSlot(slot, stack);
                        }
                    }
                    case OUTPUT -> {
                        if (slot >= 0 && slot < controller.getOutputSlotCount()) {
                            controller.getOutputHandler().setStackInSlot(slot, stack);
                        }
                    }
                }
            }

            @Override
            public @NotNull net.minecraft.world.item.ItemStack insertItem(int slot, @NotNull net.minecraft.world.item.ItemStack stack, boolean simulate) {
                ColossalFurnaceControllerBlockEntity controller = ColossalFurnaceInterfaceBlockEntity.this.getLinkedController();
                if (controller == null) {
                    return stack;
                }
                return switch (ColossalFurnaceInterfaceBlockEntity.this.mode) {
                    case UNIVERSAL -> controller.getAutomationHandler().insertItem(slot, stack, simulate);
                    case INPUT -> controller.isSmeltable(stack) && slot >= 0 && slot < controller.getInputSlotCount()
                            ? controller.getInputHandler().insertItem(slot, stack, simulate) : stack;
                    case FUEL -> controller.isFuel(stack) && slot >= 0 && slot < controller.getFuelSlotCount()
                            ? controller.getFuelHandler().insertItem(slot, stack, simulate) : stack;
                    case OUTPUT -> stack;
                };
            }

            @Override
            public @NotNull net.minecraft.world.item.ItemStack extractItem(int slot, int amount, boolean simulate) {
                ColossalFurnaceControllerBlockEntity controller = ColossalFurnaceInterfaceBlockEntity.this.getLinkedController();
                if (controller == null) {
                    return net.minecraft.world.item.ItemStack.EMPTY;
                }
                return switch (ColossalFurnaceInterfaceBlockEntity.this.mode) {
                    case UNIVERSAL -> controller.getAutomationHandler().extractItem(slot, amount, simulate);
                    case INPUT, FUEL -> net.minecraft.world.item.ItemStack.EMPTY;
                    case OUTPUT -> slot >= 0 && slot < controller.getOutputSlotCount()
                            ? controller.getOutputHandler().extractItem(slot, amount, simulate)
                            : net.minecraft.world.item.ItemStack.EMPTY;
                };
            }

            @Override
            public int getSlotLimit(int slot) {
                ColossalFurnaceControllerBlockEntity controller = ColossalFurnaceInterfaceBlockEntity.this.getLinkedController();
                if (controller == null) {
                    return 0;
                }
                return switch (ColossalFurnaceInterfaceBlockEntity.this.mode) {
                    case UNIVERSAL -> controller.getAutomationHandler().getSlotLimit(slot);
                    case INPUT -> slot >= 0 && slot < controller.getInputSlotCount() ? controller.getInputHandler().getSlotLimit(slot) : 0;
                    case FUEL -> slot >= 0 && slot < controller.getFuelSlotCount() ? controller.getFuelHandler().getSlotLimit(slot) : 0;
                    case OUTPUT -> slot >= 0 && slot < controller.getOutputSlotCount() ? controller.getOutputHandler().getSlotLimit(slot) : 0;
                };
            }

            @Override
            public boolean isItemValid(int slot, @NotNull net.minecraft.world.item.ItemStack stack) {
                ColossalFurnaceControllerBlockEntity controller = ColossalFurnaceInterfaceBlockEntity.this.getLinkedController();
                if (controller == null) {
                    return false;
                }
                return switch (ColossalFurnaceInterfaceBlockEntity.this.mode) {
                    case UNIVERSAL -> controller.getAutomationHandler().isItemValid(slot, stack);
                    case INPUT -> controller.isSmeltable(stack) && slot >= 0 && slot < controller.getInputSlotCount();
                    case FUEL -> controller.isFuel(stack) && slot >= 0 && slot < controller.getFuelSlotCount();
                    case OUTPUT -> false;
                };
            }
        };
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.formed = input.getBooleanOr("Formed", false);
        this.mode = ColossalFurnaceInterfaceMode.byName(input.getStringOr(TAG_MODE, "universal"));
        this.controllerPos = input.read("ControllerPos", BlockPos.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Formed", this.formed);
        output.putString(TAG_MODE, this.mode.getSerializedName());
        if (this.controllerPos != null) {
            output.store("ControllerPos", BlockPos.CODEC, this.controllerPos);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public IItemHandler getItemHandler() {
        return this.proxyHandler;
    }

    public ResourceHandler<ItemResource> getTransferHandler() {
        return this.transferHandler;
    }
}
