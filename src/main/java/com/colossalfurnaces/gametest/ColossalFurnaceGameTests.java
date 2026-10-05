package com.colossalfurnaces.gametest;

import com.colossalfurnaces.ColossalFurnacesMod;
import com.colossalfurnaces.block.ColossalFurnaceControllerBlock;
import com.colossalfurnaces.block.ColossalFurnaceInterfaceBlock;
import com.colossalfurnaces.block.ColossalFurnaceWallBlock;
import com.colossalfurnaces.blockentity.ColossalFurnaceControllerBlockEntity;
import com.colossalfurnaces.blockentity.ColossalFurnaceInterfaceBlockEntity;
import com.colossalfurnaces.blockentity.ColossalFurnaceInterfaceMode;
import com.colossalfurnaces.registry.ModBlocks;
import com.colossalfurnaces.registry.ModParticles;
import com.colossalfurnaces.util.ColossalBlastFurnaceStructure;
import com.colossalfurnaces.util.ColossalFurnaceStructure;
import com.colossalfurnaces.util.ColossalSmokerStructure;
import com.colossalfurnaces.util.MultiblockValidationResult;
import com.colossalfurnaces.util.StructureError;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@GameTestHolder(ColossalFurnacesMod.MOD_ID)
public final class ColossalFurnaceGameTests {
    private static final String BATCH = ColossalFurnacesMod.MOD_ID;
    private static final String TEMPLATE = ColossalFurnacesMod.MOD_ID + ":empty";
    private static final BlockPos BASE = new BlockPos(1, 1, 1);
    private static final int[] SUPPORTED_SIZES = {2, 3, 4, 5};

    private static final List<Family> FAMILIES = List.of(
            new Family(
                    "furnace",
                    ModBlocks.COLOSSAL_FURNACE_CORE,
                    ModBlocks.COLOSSAL_FURNACE_WALL,
                    ModBlocks.COLOSSAL_FURNACE_INTERFACE,
                    ColossalFurnaceStructure::validate,
                    RecipeType.SMELTING,
                    Items.RAW_IRON,
                    Items.DIRT,
                    Items.IRON_INGOT,
                    200
            ),
            new Family(
                    "smoker",
                    ModBlocks.COLOSSAL_SMOKER_CORE,
                    ModBlocks.COLOSSAL_FURNACE_WALL,
                    ModBlocks.COLOSSAL_FURNACE_INTERFACE,
                    ColossalSmokerStructure::validate,
                    RecipeType.SMOKING,
                    Items.BEEF,
                    Items.RAW_IRON,
                    Items.COOKED_BEEF,
                    100
            ),
            new Family(
                    "blast_furnace",
                    ModBlocks.COLOSSAL_BLAST_FURNACE_CORE,
                    ModBlocks.COLOSSAL_FURNACE_WALL,
                    ModBlocks.COLOSSAL_FURNACE_INTERFACE,
                    ColossalBlastFurnaceStructure::validate,
                    RecipeType.BLASTING,
                    Items.RAW_IRON,
                    Items.BEEF,
                    Items.IRON_INGOT,
                    100
            )
    );

    private ColossalFurnaceGameTests() {
    }

    @GameTestGenerator
    public static Collection<TestFunction> generateTests() {
        List<TestFunction> tests = new ArrayList<>();
        for (Family family : FAMILIES) {
            for (int size : SUPPORTED_SIZES) {
                tests.add(test("formation." + family.id() + "_" + size, 20,
                        helper -> testFormation(helper, family, size)));
                tests.add(test("processing." + family.id() + "_" + size, 20,
                        helper -> testProcessingSpeed(helper, family, size)));
                tests.add(test("menu_reach." + family.id() + "_" + size, 20,
                        helper -> testMenuReach(helper, family, size)));
                tests.add(test("core_break." + family.id() + "_" + size, 30,
                        helper -> testCoreRemoval(helper, family, size, true, false)));
                tests.add(test("short_fuel_visuals." + family.id() + "_" + size, 20,
                        helper -> testShortFuelVisuals(helper, family, size)));
                tests.add(test("lit_revalidation." + family.id() + "_" + size, 20,
                        helper -> testLitRevalidation(helper, family, size)));
            }

            tests.add(test("lifecycle." + family.id(), 30, helper -> testBreakAndReform(helper, family)));
            tests.add(test("menu_lifecycle." + family.id(), 30, helper -> testMenuLifecycle(helper, family)));
            tests.add(test("core_break_idle." + family.id(), 30,
                    helper -> testCoreRemoval(helper, family, 3, false, false)));
            tests.add(test("core_replacement." + family.id(), 30,
                    helper -> testCoreRemoval(helper, family, 3, true, true)));
            tests.add(test("recipes." + family.id(), 20, helper -> testRecipeFiltering(helper, family)));
            tests.add(test("mixed_shell." + family.id(), 20, helper -> testMixedShellRejected(helper, family)));
            tests.add(test("persistence." + family.id(), 20, helper -> testNbtRoundTrip(helper, family)));
            tests.add(test("interfaces." + family.id(), 20, helper -> testInterfaceCapabilities(helper, family)));
            tests.add(test("output_push." + family.id(), 30, helper -> testOutputAutoPush(helper, family)));
            tests.add(test("output_push_capacity." + family.id(), 70,
                    helper -> testOutputAutoPushCapacity(helper, family)));
            tests.add(test("input_pull." + family.id(), 30,
                    helper -> testAutoPull(helper, family, ColossalFurnaceInterfaceMode.INPUT)));
            tests.add(test("fuel_pull." + family.id(), 30,
                    helper -> testAutoPull(helper, family, ColossalFurnaceInterfaceMode.FUEL)));
            tests.add(test("input_pull_capacity." + family.id(), 40,
                    helper -> testAutoPullCapacity(helper, family, ColossalFurnaceInterfaceMode.INPUT)));
            tests.add(test("fuel_pull_capacity." + family.id(), 40,
                    helper -> testAutoPullCapacity(helper, family, ColossalFurnaceInterfaceMode.FUEL)));
            tests.add(test("passive_import_modes." + family.id(), 40, helper -> testPassiveAutoImportModes(helper, family)));
            tests.add(test("unformed_pull." + family.id(), 40, helper -> testUnformedAutoPull(helper, family)));
            tests.add(test("sided_pull." + family.id(), 30, helper -> testAutoPullSidedSource(helper, family)));
            tests.add(test("experience." + family.id(), 20, helper -> testExperienceTracking(helper, family)));
            tests.add(test("unfueled_input." + family.id(), 20, helper -> testUnfueledInput(helper, family)));
            tests.add(test("lit_grace." + family.id(), 20, helper -> testLitGracePeriod(helper, family)));
            tests.add(test("fuel_starvation_decay." + family.id(), 20, helper -> testFuelStarvationDecay(helper, family)));
            tests.add(test("idle_fuel." + family.id(), 20, helper -> testIdleFuelDrain(helper, family)));
            tests.add(test("blocked_output." + family.id(), 20, helper -> testBlockedOutput(helper, family)));
            tests.add(test("batch_carryover." + family.id(), 20, helper -> testBatchCarryover(helper, family)));
            tests.add(test("config_limits." + family.id(), 20, helper -> testStructureConfigLimits(helper, family)));
            tests.add(test("modded_fuel." + family.id(), 20, helper -> testModdedFuel(helper, family)));
        }
        tests.add(test("particles.scaling", 20, ColossalFurnaceGameTests::testParticleScaling));
        tests.add(test("crafting.registered_recipes", 20, ColossalFurnaceGameTests::testCraftingRecipes));
        return tests;
    }

    private static TestFunction test(String name, int timeoutTicks, java.util.function.Consumer<GameTestHelper> body) {
        return new TestFunction(BATCH, ColossalFurnacesMod.MOD_ID + "." + name, TEMPLATE, timeoutTicks, 0L, true, body);
    }

    private static void testFormation(GameTestHelper helper, Family family, int size) {
        Fixture fixture = buildShell(helper, family, size);
        helper.succeedWhen(() -> {
            assertFormed(helper, fixture.controller(), size);
            assertShellFormed(helper, family, size, true);
        });
    }

    private static void testProcessingSpeed(GameTestHelper helper, Family family, int size) {
        Fixture fixture = buildAndFormShell(helper, family, size);
        ColossalFurnaceControllerBlockEntity controller = fixture.controller();
        controller.getInputHandler().setStackInSlot(0, new ItemStack(family.validInput(), 1));
        controller.getFuelHandler().setStackInSlot(0, new ItemStack(Items.COAL, 1));

        int workPerTick = size * size;
        helper.assertTrue(controller.getProcessingParallelism() == workPerTick,
                family.id() + " must use size-squared work without an extra specialist multiplier");
        int completionTicks = divideRoundUp(family.cookingTime(), workPerTick);
        for (int tick = 1; tick < completionTicks; tick++) {
            tickController(controller);
        }
        helper.assertTrue(controller.getOutputHandler().getStackInSlot(0).isEmpty(),
                family.id() + " size " + size + " completed before its expected tick");

        tickController(controller);
        ItemStack output = controller.getOutputHandler().getStackInSlot(0);
        helper.assertTrue(output.is(family.expectedOutput()) && output.getCount() == 1,
                family.id() + " size " + size + " did not complete on tick " + completionTicks);
        helper.succeed();
    }

    private static void testBreakAndReform(GameTestHelper helper, Family family) {
        int size = 3;
        Fixture fixture = buildShell(helper, family, size);
        BlockPos breakPos = BASE.offset(size - 1, size - 1, size - 1);

        helper.startSequence()
                .thenExecuteAfter(1, () -> assertFormed(helper, fixture.controller(), size))
                .thenExecute(() -> helper.destroyBlock(breakPos))
                .thenExecuteAfter(1, () -> {
                    helper.assertFalse(fixture.controller().isFormed(), family.id() + " remained formed with a broken shell");
                    assertShellFormed(helper, family, size, false);
                })
                .thenExecute(() -> helper.setBlock(breakPos, family.wall().get()))
                .thenExecuteAfter(1, () -> {
                    assertFormed(helper, fixture.controller(), size);
                    assertShellFormed(helper, family, size, true);
                })
                .thenSucceed();
    }

    private static void testMenuLifecycle(GameTestHelper helper, Family family) {
        helper.setBlock(BASE, family.core().get());
        ColossalFurnaceControllerBlockEntity controller = (ColossalFurnaceControllerBlockEntity)
                helper.getLevel().getBlockEntity(helper.absolutePos(BASE));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos corePos = helper.absolutePos(BASE);
        player.setPos(corePos.getX() + 0.5D, corePos.getY() + 0.5D, corePos.getZ() + 0.5D);
        helper.assertTrue(controller != null && !controller.isFormed(),
                family.id() + " standalone Core unexpectedly formed");
        helper.assertTrue(controller.createMenu(1, player.getInventory(), player) == null,
                family.id() + " unformed Core created a menu");

        Fixture fixture = buildAndFormShell(helper, family, 2);
        controller = fixture.controller();
        controller.getInputHandler().setStackInSlot(0, new ItemStack(family.validInput(), 8));
        controller.getFuelHandler().setStackInSlot(0, new ItemStack(Items.COAL, 3));
        controller.getOutputHandler().setStackInSlot(0, new ItemStack(family.expectedOutput(), 5));
        AbstractContainerMenu menu = controller.createMenu(1, player.getInventory(), player);
        helper.assertTrue(menu != null && menu.stillValid(player),
                family.id() + " formed Core did not provide a usable menu");

        helper.destroyBlock(BASE.above());
        helper.assertFalse(controller.isFormed(), family.id() + " broken shell remained formed");
        helper.assertFalse(menu.stillValid(player), family.id() + " disassembled menu remained usable");
        helper.assertTrue(controller.createMenu(2, player.getInventory(), player) == null,
                family.id() + " disassembled Core created a menu");
        helper.assertTrue(controller.getInputHandler().getStackInSlot(0).getCount() == 8
                        && controller.getFuelHandler().getStackInSlot(0).getCount() == 3
                        && controller.getOutputHandler().getStackInSlot(0).getCount() == 5,
                family.id() + " lost contents while disabling GUI access");

        helper.setBlock(BASE.above(), family.wall().get());
        helper.assertTrue(controller.revalidateStructure(null, true), family.id() + " failed to reform");
        AbstractContainerMenu reopened = controller.createMenu(2, player.getInventory(), player);
        helper.assertTrue(reopened != null && reopened.stillValid(player)
                        && controller.getInputHandler().getStackInSlot(0).getCount() == 8
                        && controller.getFuelHandler().getStackInSlot(0).getCount() == 3
                        && controller.getOutputHandler().getStackInSlot(0).getCount() == 5,
                family.id() + " rebuilding failed to restore access to preserved contents");

        helper.destroyBlock(BASE.above());
        helper.destroyBlock(fixture.controllerPos());
        helper.assertFalse(reopened.stillValid(player), family.id() + " removed Core menu remained usable");
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(corePos).inflate(2.0D));
        helper.assertTrue(countDroppedItems(drops, family.validInput()) == 8
                        && countDroppedItems(drops, Items.COAL) == 3
                        && countDroppedItems(drops, family.expectedOutput()) == 5,
                family.id() + " unformed Core did not drop its retained contents exactly once");
        helper.succeed();
    }

    private static void testCoreRemoval(GameTestHelper helper, Family family, int size,
                                        boolean cooking, boolean replaceWithBlock) {
        Fixture fixture = buildAndFormShell(helper, family, size);
        ColossalFurnaceControllerBlockEntity controller = fixture.controller();
        controller.getInputHandler().setStackInSlot(0, new ItemStack(family.validInput(), 8));
        controller.getFuelHandler().setStackInSlot(0, new ItemStack(Items.COAL, 3));
        controller.getOutputHandler().setStackInSlot(0, new ItemStack(family.expectedOutput(), 5));
        if (cooking) {
            tickController(controller);
            helper.assertTrue(getPrimaryLaneProgress(controller) > 0
                            && helper.getBlockState(fixture.controllerPos()).getValue(ColossalFurnaceControllerBlock.LIT),
                    family.id() + " Core must be lit with a visible in-progress input before removal");
        }
        int expectedFuel = controller.getFuelHandler().getStackInSlot(0).getCount();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos corePos = helper.absolutePos(fixture.controllerPos());
        player.setPos(corePos.getX() + 0.5D, corePos.getY() + 0.5D, corePos.getZ() + 0.5D);
        AbstractContainerMenu menu = controller.createMenu(1, player.getInventory(), player);
        helper.assertTrue(menu != null && menu.stillValid(player), family.id() + " formed menu must start valid");

        if (replaceWithBlock) {
            helper.getLevel().setBlock(corePos, Blocks.STONE.defaultBlockState(), 3);
        } else {
            helper.assertTrue(helper.getLevel().destroyBlock(corePos, true),
                    family.id() + " Core did not break on the first attempt");
        }
        assertCoreRemoved(helper, family, fixture, size, replaceWithBlock, menu, player, expectedFuel);
        helper.startSequence()
                .thenExecuteAfter(2, () -> assertCoreRemoved(helper, family, fixture, size,
                        replaceWithBlock, menu, player, expectedFuel))
                .thenSucceed();
    }

    private static void assertCoreRemoved(GameTestHelper helper, Family family, Fixture fixture, int size,
                                          boolean replaced, AbstractContainerMenu menu, Player player, int expectedFuel) {
        BlockPos corePos = helper.absolutePos(fixture.controllerPos());
        BlockState state = helper.getBlockState(fixture.controllerPos());
        helper.assertTrue(replaced ? state.is(Blocks.STONE) : state.isAir(),
                family.id() + " removed Core resurrected or overwrote its replacement");
        helper.assertTrue(helper.getLevel().getBlockEntity(corePos) == null && !fixture.controller().isFormed(),
                family.id() + " removed Core retained a block entity or formed state");
        helper.assertFalse(menu.stillValid(player), family.id() + " removed Core menu remained usable");
        helper.assertTrue(fixture.interfaceBlockEntity().getLinkedController() == null,
                family.id() + " Interface remained linked to the removed Core");
        assertShellFormed(helper, family, size, false);
        for (BlockPos pos : BlockPos.betweenClosed(BASE, BASE.offset(size - 1, size - 1, size - 1))) {
            BlockState shellState = helper.getBlockState(pos);
            helper.assertTrue(!shellState.hasProperty(ColossalFurnaceControllerBlock.LIT)
                            || !shellState.getValue(ColossalFurnaceControllerBlock.LIT),
                    family.id() + " removed structure retained a lit shell block at " + pos);
            if (!pos.equals(fixture.controllerPos()) && isBoundary(
                    pos.getX() - BASE.getX(), pos.getY() - BASE.getY(), pos.getZ() - BASE.getZ(), size)) {
                helper.assertTrue(shellState.is(pos.equals(fixture.interfacePos())
                                ? family.interfaceBlock().get() : family.wall().get()),
                        family.id() + " removal changed a remaining shell block");
            }
        }
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(corePos).inflate(2.0D));
        helper.assertTrue(countDroppedItems(drops, family.validInput()) == 8
                        && countDroppedItems(drops, Items.COAL) == expectedFuel
                        && countDroppedItems(drops, family.expectedOutput()) == 5
                        && countDroppedItems(drops, family.core().get().asItem()) == (replaced ? 0 : 1),
                family.id() + " removed Core or its visible input/fuel/output did not drop exactly once");
    }

    private static void testMenuReach(GameTestHelper helper, Family family, int size) {
        Fixture fixture = buildAndFormShell(helper, family, size);
        ColossalFurnaceControllerBlockEntity controller = fixture.controller();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        AbstractContainerMenu menu = controller.createMenu(1, player.getInventory(), player);
        helper.assertTrue(menu != null, family.id() + " formed Core did not create a reach-test menu");

        BlockPos min = controller.getMinPos();
        BlockPos max = controller.getMaxPos();
        double maxX = max.getX() + 1.0D;
        double maxY = max.getY() + 1.0D;
        double maxZ = max.getZ() + 1.0D;
        double centerX = (min.getX() + maxX) * 0.5D;
        double centerY = (min.getY() + maxY) * 0.5D;
        double centerZ = (min.getZ() + maxZ) * 0.5D;
        for (Direction direction : Direction.values()) {
            double faceX = direction == Direction.WEST ? min.getX()
                    : direction == Direction.EAST ? maxX : centerX;
            double faceY = direction == Direction.DOWN ? min.getY()
                    : direction == Direction.UP ? maxY : centerY;
            double faceZ = direction == Direction.NORTH ? min.getZ()
                    : direction == Direction.SOUTH ? maxZ : centerZ;
            player.setPos(faceX + direction.getStepX() * 0.75D,
                    faceY + direction.getStepY() * 0.75D, faceZ + direction.getStepZ() * 0.75D);
            helper.assertTrue(menu.stillValid(player),
                    family.id() + " size " + size + " rejected access beside its " + direction + " face");

            player.setPos(faceX + direction.getStepX() * 8.0D,
                    faceY + direction.getStepY() * 8.0D, faceZ + direction.getStepZ() * 8.0D);
            helper.assertTrue(menu.stillValid(player),
                    family.id() + " size " + size + " rejected the eight-block " + direction + " boundary");
            player.setPos(faceX + direction.getStepX() * 8.01D,
                    faceY + direction.getStepY() * 8.01D, faceZ + direction.getStepZ() * 8.01D);
            helper.assertFalse(menu.stillValid(player),
                    family.id() + " size " + size + " allowed access past its " + direction + " limit");
        }

        player.setPos(maxX + 0.5D, maxY + 0.5D, maxZ + 0.5D);
        helper.assertTrue(menu.stillValid(player), family.id() + " rejected its corner farthest from the Core");
        player.setPos(maxX + 4.0D, maxY + 4.0D, maxZ + 4.0D);
        helper.assertTrue(menu.stillValid(player), family.id() + " rejected a diagonal within eight blocks");
        player.setPos(maxX + 5.0D, maxY + 5.0D, maxZ + 5.0D);
        helper.assertFalse(menu.stillValid(player), family.id() + " used an excessive diagonal reach");
        helper.succeed();
    }

    private static int countDroppedItems(List<ItemEntity> drops, Item item) {
        return drops.stream().map(ItemEntity::getItem).filter(stack -> stack.is(item))
                .mapToInt(ItemStack::getCount).sum();
    }

    private static void testRecipeFiltering(GameTestHelper helper, Family family) {
        Fixture fixture = buildAndFormShell(helper, family, 2);
        helper.assertTrue(fixture.controller().isSmeltable(new ItemStack(family.validInput())),
                family.id() + " rejected its intended recipe");
        helper.assertFalse(fixture.controller().isSmeltable(new ItemStack(family.invalidInput())),
                family.id() + " accepted a recipe from another furnace family");
        helper.assertTrue(fixture.controller().isFuel(new ItemStack(Items.COAL)),
                family.id() + " rejected vanilla fuel");
        helper.succeed();
    }

    private static void testMixedShellRejected(GameTestHelper helper, Family family) {
        int size = 3;
        Fixture fixture = buildShell(helper, family, size);
        Family otherFamily = FAMILIES.get((FAMILIES.indexOf(family) + 1) % FAMILIES.size());
        BlockPos replacementPos = BASE.offset(size - 1, size - 1, size - 1);
        helper.setBlock(replacementPos, otherFamily.core().get());

        MultiblockValidationResult result = family.validator().validate(
                helper.getLevel(),
                helper.absolutePos(fixture.controllerPos()),
                5
        );
        helper.assertFalse(result.valid(), family.id() + " accepted a mixed-family shell");
        helper.assertFalse(fixture.controller().isFormed(), family.id() + " remained formed after a mixed-family replacement");
        helper.succeed();
    }

    private static void testNbtRoundTrip(GameTestHelper helper, Family family) {
        Fixture fixture = buildAndFormShell(helper, family, 3);
        ColossalFurnaceControllerBlockEntity original = fixture.controller();
        original.getInputHandler().setStackInSlot(0, new ItemStack(family.validInput(), 3));
        original.getFuelHandler().setStackInSlot(0, new ItemStack(Items.COAL, 2));
        original.getOutputHandler().setStackInSlot(0, new ItemStack(family.expectedOutput(), 4));
        tickController(original);

        CompoundTag controllerTag = original.getUpdateTag(helper.getLevel().registryAccess());
        ColossalFurnaceControllerBlockEntity restored = (ColossalFurnaceControllerBlockEntity) original.getType()
                .create(helper.absolutePos(fixture.controllerPos()), original.getBlockState());
        helper.assertTrue(restored != null, "Failed to create restored " + family.id() + " controller");
        restored.loadWithComponents(controllerTag, helper.getLevel().registryAccess());

        helper.assertTrue(restored.isFormed(), family.id() + " lost formed state after NBT round-trip");
        helper.assertTrue(restored.getOuterSize() == 3, family.id() + " lost outer size after NBT round-trip");
        helper.assertTrue(restored.getMinPos().equals(original.getMinPos()) && restored.getMaxPos().equals(original.getMaxPos()),
                family.id() + " lost structure bounds after NBT round-trip");
        helper.assertTrue(restored.getInputHandler().getStackInSlot(0).getCount()
                        == original.getInputHandler().getStackInSlot(0).getCount(),
                family.id() + " lost input inventory after NBT round-trip");
        helper.assertTrue(restored.getFuelHandler().getStackInSlot(0).getCount()
                        == original.getFuelHandler().getStackInSlot(0).getCount(),
                family.id() + " lost fuel inventory after NBT round-trip");
        helper.assertTrue(restored.getOutputHandler().getStackInSlot(0).getCount()
                        == original.getOutputHandler().getStackInSlot(0).getCount(),
                family.id() + " lost output inventory after NBT round-trip");
        helper.assertTrue(restored.getHeatBuffer() == original.getHeatBuffer(),
                family.id() + " lost heat after NBT round-trip");

        ColossalFurnaceInterfaceBlockEntity originalInterface = fixture.interfaceBlockEntity();
        originalInterface.setMode(ColossalFurnaceInterfaceMode.OUTPUT);
        CompoundTag interfaceTag = originalInterface.getUpdateTag(helper.getLevel().registryAccess());
        ColossalFurnaceInterfaceBlockEntity restoredInterface = (ColossalFurnaceInterfaceBlockEntity) originalInterface.getType()
                .create(helper.absolutePos(fixture.interfacePos()), originalInterface.getBlockState());
        helper.assertTrue(restoredInterface != null, "Failed to create restored " + family.id() + " interface");
        restoredInterface.loadWithComponents(interfaceTag, helper.getLevel().registryAccess());
        helper.assertTrue(restoredInterface.getMode() == ColossalFurnaceInterfaceMode.OUTPUT,
                family.id() + " lost interface mode after NBT round-trip");
        helper.succeed();
    }

    private static void testInterfaceCapabilities(GameTestHelper helper, Family family) {
        Fixture fixture = buildAndFormShell(helper, family, 2);
        ColossalFurnaceControllerBlockEntity controller = fixture.controller();
        ColossalFurnaceInterfaceBlockEntity interfaceBlockEntity = fixture.interfaceBlockEntity();
        helper.assertTrue(helper.getLevel().getLightEmission(helper.absolutePos(fixture.interfacePos())) == 0,
                family.id() + " formed interface emitted block light");
        IItemHandler handler = helper.getLevel().getCapability(
                Capabilities.ItemHandler.BLOCK,
                helper.absolutePos(fixture.interfacePos()),
                null
        );
        helper.assertTrue(handler != null, family.id() + " interface exposed no item capability");

        interfaceBlockEntity.setMode(ColossalFurnaceInterfaceMode.INPUT);
        helper.assertTrue(handler.getSlots() == controller.getInputSlotCount(), family.id() + " input mode exposed wrong slot count");
        helper.assertTrue(handler.insertItem(0, new ItemStack(family.validInput()), false).isEmpty(),
                family.id() + " input mode rejected valid input");
        helper.assertTrue(handler.insertItem(1, new ItemStack(Items.COAL), false).getCount() == 1,
                family.id() + " input mode accepted fuel");
        controller.getInputHandler().setStackInSlot(0, ItemStack.EMPTY);

        interfaceBlockEntity.setMode(ColossalFurnaceInterfaceMode.FUEL);
        helper.assertTrue(handler.getSlots() == controller.getFuelSlotCount(), family.id() + " fuel mode exposed wrong slot count");
        helper.assertTrue(handler.insertItem(0, new ItemStack(Items.COAL), false).isEmpty(),
                family.id() + " fuel mode rejected fuel");
        helper.assertTrue(handler.insertItem(1, new ItemStack(family.validInput()), false).getCount() == 1,
                family.id() + " fuel mode accepted cooking input");
        controller.getFuelHandler().setStackInSlot(0, ItemStack.EMPTY);

        interfaceBlockEntity.setMode(ColossalFurnaceInterfaceMode.OUTPUT);
        controller.getOutputHandler().setStackInSlot(0, new ItemStack(family.expectedOutput(), 2));
        helper.assertTrue(handler.getSlots() == controller.getOutputSlotCount(), family.id() + " output mode exposed wrong slot count");
        helper.assertTrue(handler.insertItem(0, new ItemStack(family.expectedOutput()), false).getCount() == 1,
                family.id() + " output mode accepted insertion");
        ItemStack extracted = handler.extractItem(0, 1, false);
        helper.assertTrue(extracted.is(family.expectedOutput()) && extracted.getCount() == 1,
                family.id() + " output mode rejected extraction");

        interfaceBlockEntity.setMode(ColossalFurnaceInterfaceMode.UNIVERSAL);
        int expectedUniversalSlots = controller.getInputSlotCount() + controller.getFuelSlotCount() + controller.getOutputSlotCount();
        helper.assertTrue(handler.getSlots() == expectedUniversalSlots, family.id() + " universal mode exposed wrong slot count");
        helper.assertTrue(handler.insertItem(0, new ItemStack(family.validInput()), false).isEmpty(),
                family.id() + " universal mode rejected valid input");
        helper.assertTrue(handler.insertItem(controller.getInputSlotCount(), new ItemStack(Items.COAL), false).isEmpty(),
                family.id() + " universal mode rejected fuel");
        helper.succeed();
    }

    private static void testAutoPull(GameTestHelper helper, Family family, ColossalFurnaceInterfaceMode mode) {
        Fixture fixture = buildAndFormShell(helper, family, 2);
        fixture.interfaceBlockEntity().setMode(mode);
        Container source = placeSourceBarrel(helper, fixture.interfacePos().east());
        source.setItem(0, new ItemStack(family.invalidInput(), 4));
        Item valid = mode == ColossalFurnaceInterfaceMode.INPUT ? family.validInput() : Items.COAL;
        Item wrongSection = mode == ColossalFurnaceInterfaceMode.INPUT ? Items.COAL : family.validInput();
        source.setItem(1, new ItemStack(wrongSection, 4));
        source.setItem(2, new ItemStack(valid, 12));
        IItemHandler destination = mode == ColossalFurnaceInterfaceMode.INPUT
                ? fixture.controller().getInputHandler() : fixture.controller().getFuelHandler();
        IItemHandler otherSection = mode == ColossalFurnaceInterfaceMode.INPUT
                ? fixture.controller().getFuelHandler() : fixture.controller().getInputHandler();

        helper.runAfterDelay(10, () -> {
            helper.assertTrue(destination.getStackInSlot(0).is(valid)
                            && destination.getStackInSlot(0).getCount() == 12 && source.getItem(2).isEmpty(),
                    family.id() + " did not auto-pull valid " + mode + " items");
            helper.assertTrue(source.getItem(0).getCount() == 4 && source.getItem(1).getCount() == 4,
                    family.id() + " auto-pulled an invalid or wrong-section item");
            helper.assertTrue(otherSection.getStackInSlot(0).isEmpty(),
                    family.id() + " auto-pull routed items into the wrong section");
            helper.succeed();
        });
    }

    private static void testAutoPullCapacity(GameTestHelper helper, Family family, ColossalFurnaceInterfaceMode mode) {
        Fixture fixture = buildAndFormShell(helper, family, 2);
        fixture.interfaceBlockEntity().setMode(mode);
        Container source = placeSourceBarrel(helper, fixture.interfacePos().east());
        Item item = mode == ColossalFurnaceInterfaceMode.INPUT ? family.validInput() : Items.COAL;
        source.setItem(0, new ItemStack(item, 8));
        var destination = mode == ColossalFurnaceInterfaceMode.INPUT
                ? fixture.controller().getInputHandler() : fixture.controller().getFuelHandler();
        for (int slot = 0; slot < destination.getSlots(); slot++) {
            destination.setStackInSlot(slot, new ItemStack(item, slot == destination.getSlots() - 1 ? 61 : 64));
        }
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(source.getItem(0).getCount() == 5
                            && destination.getStackInSlot(destination.getSlots() - 1).getCount() == 64,
                    family.id() + " must import only the three items that fit");
            helper.runAfterDelay(10, () -> {
                helper.assertTrue(source.getItem(0).getCount() == 5
                                && destination.getStackInSlot(destination.getSlots() - 1).getCount() == 64,
                        family.id() + " removed items from a source while " + mode + " slots were full");
                helper.succeed();
            });
        });
    }

    private static void testPassiveAutoImportModes(GameTestHelper helper, Family family) {
        Fixture fixture = buildAndFormShell(helper, family, 2);
        Container source = placeSourceBarrel(helper, fixture.interfacePos().east());
        source.setItem(0, new ItemStack(family.validInput(), 8));
        source.setItem(1, new ItemStack(Items.COAL, 8));
        fixture.controller().getOutputHandler().setStackInSlot(0, new ItemStack(family.expectedOutput(), 3));
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(source.getItem(0).getCount() == 8 && source.getItem(1).getCount() == 8
                            && source.getItem(2).isEmpty()
                            && fixture.controller().getInputHandler().getStackInSlot(0).isEmpty()
                            && fixture.controller().getFuelHandler().getStackInSlot(0).isEmpty()
                            && fixture.controller().getOutputHandler().getStackInSlot(0).getCount() == 3,
                    family.id() + " Universal mode must not auto-import or auto-export");
            fixture.interfaceBlockEntity().setMode(ColossalFurnaceInterfaceMode.OUTPUT);
            helper.runAfterDelay(10, () -> {
                helper.assertTrue(source.getItem(0).getCount() == 8 && source.getItem(1).getCount() == 8
                                && fixture.controller().getInputHandler().getStackInSlot(0).isEmpty()
                                && fixture.controller().getFuelHandler().getStackInSlot(0).isEmpty(),
                        family.id() + " Output mode must not auto-import");
                helper.assertTrue(source.getItem(2).is(family.expectedOutput()) && source.getItem(2).getCount() == 3
                                && fixture.controller().getOutputHandler().getStackInSlot(0).isEmpty(),
                        family.id() + " Output mode must retain its existing auto-export behavior");
                helper.succeed();
            });
        });
    }

    private static void testUnformedAutoPull(GameTestHelper helper, Family family) {
        Fixture fixture = buildAndFormShell(helper, family, 2);
        Container source = placeSourceBarrel(helper, fixture.interfacePos().east());
        source.setItem(0, new ItemStack(family.validInput(), 8));
        source.setItem(1, new ItemStack(Items.COAL, 8));
        helper.destroyBlock(BASE.above());
        fixture.interfaceBlockEntity().setMode(ColossalFurnaceInterfaceMode.INPUT);
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(source.getItem(0).getCount() == 8,
                    family.id() + " unformed Input interface pulled items");
            fixture.interfaceBlockEntity().setMode(ColossalFurnaceInterfaceMode.FUEL);
            helper.runAfterDelay(10, () -> {
                helper.assertTrue(source.getItem(1).getCount() == 8
                                && fixture.controller().getInputHandler().getStackInSlot(0).isEmpty()
                                && fixture.controller().getFuelHandler().getStackInSlot(0).isEmpty(),
                        family.id() + " unformed Fuel interface pulled items");
                helper.succeed();
            });
        });
    }

    private static void testAutoPullSidedSource(GameTestHelper helper, Family family) {
        Fixture fixture = buildAndFormShell(helper, family, 2);
        fixture.interfaceBlockEntity().setMode(ColossalFurnaceInterfaceMode.INPUT);
        BlockPos sourcePos = fixture.interfacePos().east();
        helper.setBlock(sourcePos, Blocks.FURNACE);
        Container source = (Container) helper.getLevel().getBlockEntity(helper.absolutePos(sourcePos));
        source.setItem(0, new ItemStack(family.validInput(), 8));
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(source.getItem(0).getCount() == 8
                            && fixture.controller().getInputHandler().getStackInSlot(0).isEmpty(),
                    family.id() + " bypassed the source furnace's side-only fuel access");
            helper.succeed();
        });
    }

    private static Container placeSourceBarrel(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Blocks.BARREL);
        return (Container) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
    }

    private static void testOutputAutoPush(GameTestHelper helper, Family family) {
        Fixture fixture = buildAndFormShell(helper, family, 2);
        fixture.interfaceBlockEntity().setMode(ColossalFurnaceInterfaceMode.OUTPUT);
        fixture.controller().getOutputHandler().setStackInSlot(0, new ItemStack(family.expectedOutput(), 1));
        BlockPos chestPos = fixture.interfacePos().east();
        helper.setBlock(chestPos, Blocks.CHEST);

        helper.succeedWhen(() -> {
            helper.assertContainerContains(chestPos, family.expectedOutput());
            helper.assertTrue(fixture.controller().getOutputHandler().getStackInSlot(0).isEmpty(),
                    family.id() + " duplicated an auto-pushed output");
        });
    }

    private static void testOutputAutoPushCapacity(GameTestHelper helper, Family family) {
        Fixture fixture = buildAndFormShell(helper, family, 2);
        ColossalFurnaceControllerBlockEntity controller = fixture.controller();
        fixture.interfaceBlockEntity().setMode(ColossalFurnaceInterfaceMode.OUTPUT);
        controller.getOutputHandler().setStackInSlot(0, new ItemStack(family.expectedOutput(), 32));
        controller.getOutputHandler().setStackInSlot(1, new ItemStack(Items.COAL, 5));
        controller.getOutputHandler().setStackInSlot(8, new ItemStack(family.expectedOutput(), 9));
        controller.getInputHandler().setStackInSlot(0, new ItemStack(family.validInput(), 1));
        controller.getFuelHandler().setStackInSlot(0, new ItemStack(Items.COAL, 1));
        tickController(controller);
        controller.getInputHandler().setStackInSlot(0, ItemStack.EMPTY);
        helper.assertTrue(controller.getHeatBuffer() > 0, "Capacity test must exercise idle heat drain");

        Container barrel = placeSourceBarrel(helper, fixture.interfacePos().east());
        for (int slot = 0; slot < barrel.getContainerSize(); slot++) {
            barrel.setItem(slot, new ItemStack(Items.DIRT, 64));
        }
        helper.startSequence()
                .thenExecuteAfter(16, () -> {
                    assertAutoPushSource(helper, family, controller, 32);
                    for (int slot = 0; slot < barrel.getContainerSize(); slot++) {
                        helper.assertTrue(barrel.getItem(slot).is(Items.DIRT) && barrel.getItem(slot).getCount() == 64,
                                family.id() + " modified a full output destination");
                    }
                    helper.assertTrue(controller.getHeatBuffer() > 0, "Full destination check must run while heat remains");
                })
                .thenExecute(() -> barrel.setItem(0, new ItemStack(family.expectedOutput(), 61)))
                .thenExecuteAfter(16, () -> assertAutoPushPartialDestination(helper, family, controller, barrel))
                .thenExecuteAfter(16, () -> assertAutoPushPartialDestination(helper, family, controller, barrel))
                .thenSucceed();
    }

    private static void assertAutoPushPartialDestination(GameTestHelper helper, Family family,
                                                         ColossalFurnaceControllerBlockEntity controller, Container barrel) {
        assertAutoPushSource(helper, family, controller, 29);
        helper.assertTrue(barrel.getItem(0).is(family.expectedOutput()) && barrel.getItem(0).getCount() == 64,
                family.id() + " auto-push did not fill exactly the three available destination spaces");
        for (int slot = 1; slot < barrel.getContainerSize(); slot++) {
            helper.assertTrue(barrel.getItem(slot).is(Items.DIRT) && barrel.getItem(slot).getCount() == 64,
                    family.id() + " changed an unrelated destination stack");
        }
    }

    private static void assertAutoPushSource(GameTestHelper helper, Family family,
                                             ColossalFurnaceControllerBlockEntity controller, int expectedFirstCount) {
        for (int slot = 0; slot < controller.getOutputSlotCount(); slot++) {
            ItemStack stack = controller.getOutputHandler().getStackInSlot(slot);
            boolean unchanged = switch (slot) {
                case 0 -> stack.is(family.expectedOutput()) && stack.getCount() == expectedFirstCount;
                case 1 -> stack.is(Items.COAL) && stack.getCount() == 5;
                case 8 -> stack.is(family.expectedOutput()) && stack.getCount() == 9;
                default -> stack.isEmpty();
            };
            helper.assertTrue(unchanged, family.id() + " lost, duplicated, or moved blocked output in slot " + slot);
        }
    }

    private static void testExperienceTracking(GameTestHelper helper, Family family) {
        int size = 5;
        int itemCount = 10;
        Fixture fixture = buildAndFormShell(helper, family, size);
        ColossalFurnaceControllerBlockEntity controller = fixture.controller();
        controller.getInputHandler().setStackInSlot(0, new ItemStack(family.validInput(), itemCount));
        controller.getFuelHandler().setStackInSlot(0, new ItemStack(Items.COAL, 2));
        processItems(controller, family, size, itemCount);

        ItemStack output = controller.getOutputHandler().getStackInSlot(0);
        helper.assertTrue(output.is(family.expectedOutput()) && output.getCount() == itemCount,
                family.id() + " did not complete the XP test batch");
        helper.assertTrue(countStoredRecipeUses(controller) == itemCount,
                family.id() + " did not persist all completed recipe uses");

        CompoundTag recipesUsedTag = controller.getUpdateTag(helper.getLevel().registryAccess()).getCompound("RecipesUsed");
        ResourceLocation recipeId = ResourceLocation.tryParse(recipesUsedTag.getAllKeys().iterator().next());
        RecipeHolder<?> recipe = recipeId == null ? null : helper.getLevel().getRecipeManager().byKey(recipeId).orElse(null);
        helper.assertTrue(recipe != null, family.id() + " did not resolve its stored recipe");

        fixture.interfaceBlockEntity().setMode(ColossalFurnaceInterfaceMode.OUTPUT);
        IItemHandler automation = helper.getLevel().getCapability(
                Capabilities.ItemHandler.BLOCK,
                helper.absolutePos(fixture.interfacePos()),
                null
        );
        helper.assertTrue(automation != null, family.id() + " output interface exposed no item capability");
        ItemStack automatedOutput = automation.extractItem(0, 1, false);
        helper.assertTrue(automatedOutput.is(family.expectedOutput()) && automatedOutput.getCount() == 1,
                family.id() + " failed automated extraction during the XP test");
        helper.assertTrue(countStoredRecipeUses(controller) == itemCount,
                family.id() + " discarded stored XP during automated extraction");

        BlockPos absoluteControllerPos = helper.absolutePos(fixture.controllerPos());
        Vec3 collectionPosition = Vec3.atCenterOf(absoluteControllerPos);
        List<RecipeHolder<?>> collectedRecipes = controller.collectUsedRecipesAndPopExperience(helper.getLevel(), collectionPosition);
        helper.assertTrue(countStoredRecipeUses(controller) == 0,
                family.id() + " did not clear stored XP after manual collection");
        helper.assertTrue(collectedRecipes.contains(recipe),
                family.id() + " did not return the completed recipe for unlocking");

        helper.assertFalse(findExperienceOrbs(helper, collectionPosition).isEmpty(),
                family.id() + " did not create experience after manual collection");

        findExperienceOrbs(helper, collectionPosition).forEach(ExperienceOrb::discard);
        controller.getInputHandler().setStackInSlot(0, new ItemStack(family.validInput(), itemCount));
        controller.getFuelHandler().setStackInSlot(0, new ItemStack(Items.COAL, 2));
        processItems(controller, family, size, itemCount);
        helper.assertTrue(countStoredRecipeUses(controller) == itemCount,
                family.id() + " did not record the Core-break XP batch");

        Vec3 corePosition = Vec3.atCenterOf(absoluteControllerPos);
        helper.destroyBlock(fixture.controllerPos());
        helper.assertFalse(findExperienceOrbs(helper, corePosition).isEmpty(),
                family.id() + " did not release stored XP when its Core was broken");
        helper.succeed();
    }

    private static void testUnfueledInput(GameTestHelper helper, Family family) {
        Fixture fixture = buildAndFormShell(helper, family, 2);
        ColossalFurnaceControllerBlockEntity controller = fixture.controller();
        controller.getInputHandler().setStackInSlot(0, new ItemStack(family.validInput(), 2));

        tickController(controller);

        helper.assertTrue(controller.getInputHandler().getStackInSlot(0).getCount() == 2,
                family.id() + " reserved an input item without fuel");
        helper.assertTrue(controller.getActiveLaneCount() == 0,
                family.id() + " created a processing lane without fuel");
        helper.assertFalse(controller.isMenuLit(),
                family.id() + " reported an active GUI flame without fuel");
        helper.assertFalse(helper.getBlockState(fixture.controllerPos()).getValue(ColossalFurnaceControllerBlock.LIT),
                family.id() + " lit its formed texture without fuel");

        controller.getFuelHandler().setStackInSlot(0, new ItemStack(Items.STICK));
        tickController(controller);
        helper.assertTrue(controller.getInputHandler().getStackInSlot(0).getCount() == 2,
                family.id() + " hid an in-progress input item");
        helper.assertTrue(controller.getActiveLaneCount() == 1,
                family.id() + " did not start a lane after fuel became available");

        controller.getInputHandler().setStackInSlot(0, ItemStack.EMPTY);
        tickController(controller);
        helper.assertTrue(controller.getActiveLaneCount() == 0,
                family.id() + " retained progress after its visible source item was removed");
        helper.assertTrue(controller.getOutputHandler().getStackInSlot(0).isEmpty(),
                family.id() + " produced output after its visible source item was removed");
        helper.succeed();
    }

    private static void testCraftingRecipes(GameTestHelper helper) {
        Item wall = ModBlocks.COLOSSAL_FURNACE_WALL.get().asItem();
        List<ItemStack> wallGrid = List.of(
                ItemStack.EMPTY, new ItemStack(Items.COBBLESTONE), ItemStack.EMPTY,
                new ItemStack(Items.COBBLESTONE), new ItemStack(Items.SMOOTH_STONE), new ItemStack(Items.COBBLESTONE),
                ItemStack.EMPTY, new ItemStack(Items.COBBLESTONE), ItemStack.EMPTY);
        assertCraftingResult(helper, "colossal_furnace_wall", wall, 3, 3, wallGrid);

        List<ItemStack> wrongCenter = new ArrayList<>(wallGrid);
        wrongCenter.set(4, new ItemStack(Items.STONE));
        helper.assertTrue(helper.getLevel().getRecipeManager()
                        .getRecipeFor(RecipeType.CRAFTING, CraftingInput.of(3, 3, wrongCenter), helper.getLevel())
                        .map(recipe -> !recipe.id().equals(ResourceLocation.fromNamespaceAndPath(
                                ColossalFurnacesMod.MOD_ID, "colossal_furnace_wall"))).orElse(true),
                "Wall recipe must require smooth stone, not ordinary stone");
        List<ItemStack> occupiedCorner = new ArrayList<>(wallGrid);
        occupiedCorner.set(0, new ItemStack(Items.COBBLESTONE));
        helper.assertTrue(helper.getLevel().getRecipeManager()
                        .getRecipeFor(RecipeType.CRAFTING, CraftingInput.of(3, 3, occupiedCorner), helper.getLevel())
                        .map(recipe -> !recipe.id().equals(ResourceLocation.fromNamespaceAndPath(
                                ColossalFurnacesMod.MOD_ID, "colossal_furnace_wall"))).orElse(true),
                "Wall recipe must require empty corners");

        for (Family family : FAMILIES) {
            Item vanillaMachine = switch (family.id()) {
                case "smoker" -> Items.SMOKER;
                case "blast_furnace" -> Items.BLAST_FURNACE;
                default -> Items.FURNACE;
            };
            assertShapelessCraftingResult(helper, "colossal_" + family.id() + "_core",
                    family.core().get().asItem(), wall, vanillaMachine);
        }
        assertShapelessCraftingResult(helper, "colossal_furnace_interface",
                ModBlocks.COLOSSAL_FURNACE_INTERFACE.get().asItem(), wall, Items.HOPPER);
        helper.succeed();
    }

    private static void assertShapelessCraftingResult(GameTestHelper helper, String recipeName, Item result,
                                                     Item first, Item second) {
        assertCraftingResult(helper, recipeName, result, 2, 2, List.of(
                new ItemStack(first), ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(second)));
        assertCraftingResult(helper, recipeName, result, 2, 2, List.of(
                new ItemStack(second), ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(first)));
    }

    private static void assertCraftingResult(GameTestHelper helper, String recipeName, Item result,
                                             int width, int height, List<ItemStack> grid) {
        CraftingInput input = CraftingInput.of(width, height, grid);
        var match = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        helper.assertTrue(match.isPresent(), "Crafting recipe did not load or match: " + recipeName);
        var recipe = match.orElseThrow();
        helper.assertTrue(recipe.id().equals(ResourceLocation.fromNamespaceAndPath(ColossalFurnacesMod.MOD_ID, recipeName)),
                "Unexpected crafting recipe matched instead of " + recipeName);
        ItemStack output = recipe.value().assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(output.is(result) && output.getCount() == 1,
                "Crafting recipe must produce exactly one expected item: " + recipeName);
    }

    private static void testParticleScaling(GameTestHelper helper) {
        float[] scales = {2.0F, 3.0F, 4.0F, 5.0F};
        double[] frontOffsets = {0.20D, 0.30D, 0.40D, 0.50D};
        helper.assertTrue(ModParticles.ENTRIES.size() == 8, "Each supported size needs separate smoke and flame types");
        for (int size : SUPPORTED_SIZES) {
            int index = (size - 2) * 2;
            ModParticles.Entry smoke = ModParticles.ENTRIES.get(index);
            ModParticles.Entry flame = ModParticles.ENTRIES.get(index + 1);
            helper.assertTrue(ModParticles.scaleForSize(size) == scales[size - 2]
                            && smoke.scale() == scales[size - 2] && flame.scale() == scales[size - 2],
                    "Incorrect particle scale for size " + size);
            helper.assertTrue(Math.abs(ModParticles.frontOffsetForSize(size) - frontOffsets[size - 2]) < 1.0E-9D,
                    "Incorrect front-surface particle clearance for size " + size);
            helper.assertTrue(!smoke.flame() && flame.flame()
                            && ModParticles.smokeForSize(size) == smoke.type().get()
                            && ModParticles.flameForSize(size) == flame.type().get(),
                    "Incorrect smoke/flame particle selection for size " + size);
            helper.assertTrue(smoke.type().get() != net.minecraft.core.particles.ParticleTypes.SMOKE
                            && flame.type().get() != net.minecraft.core.particles.ParticleTypes.FLAME,
                    "Scaled particles must not replace vanilla particle types");
        }
        helper.assertTrue(ModParticles.scaleForSize(1) == 2.0F
                        && Math.abs(ModParticles.frontOffsetForSize(1) - frontOffsets[0]) < 1.0E-9D
                        && ModParticles.smokeForSize(1) == ModParticles.smokeForSize(2)
                        && ModParticles.flameForSize(1) == ModParticles.flameForSize(2),
                "Undersized particle requests must clamp to size 2");
        helper.assertTrue(ModParticles.scaleForSize(6) == 5.0F
                        && Math.abs(ModParticles.frontOffsetForSize(6) - frontOffsets[3]) < 1.0E-9D
                        && ModParticles.smokeForSize(6) == ModParticles.smokeForSize(5)
                        && ModParticles.flameForSize(6) == ModParticles.flameForSize(5),
                "Oversized particle requests must clamp to size 5");
        helper.succeed();
    }

    private static void testShortFuelVisuals(GameTestHelper helper, Family family, int size) {
        Fixture fixture = buildAndFormShell(helper, family, size);
        ColossalFurnaceControllerBlockEntity controller = fixture.controller();
        controller.getInputHandler().setStackInSlot(0, new ItemStack(family.validInput(), 64));
        controller.getFuelHandler().setStackInSlot(0, new ItemStack(Items.BAMBOO, 16));
        int workPerTick = size * size;
        for (int tick = 1; tick <= 8; tick++) {
            tickController(controller);
            helper.assertTrue(helper.getBlockState(fixture.controllerPos()).getValue(ColossalFurnaceControllerBlock.LIT),
                    family.id() + " size " + size + " did not keep working visuals active with bamboo");
            assertShellLightSources(helper, family, size, Math.max(1, size - 2));
            int workDone = tick * workPerTick;
            int produced = controller.getOutputHandler().getStackInSlot(0).getCount();
            helper.assertTrue(produced == workDone / family.cookingTime()
                            && getPrimaryLaneProgress(controller) == workDone % family.cookingTime(),
                    family.id() + " bamboo changed processing speed or lost work");
            int expectedFuelUsed = divideRoundUp(workDone, 50);
            helper.assertTrue(controller.getFuelHandler().getStackInSlot(0).getCount() == 16 - expectedFuelUsed
                            && controller.getHeatBuffer() == expectedFuelUsed * 50 - workDone,
                    family.id() + " bamboo consumed extra idle heat or changed fuel efficiency");
        }

        controller.getInputHandler().setStackInSlot(0, ItemStack.EMPTY);
        controller.getFuelHandler().setStackInSlot(0, ItemStack.EMPTY);
        for (int tick = 0; tick < 50 && controller.getHeatBuffer() > 0; tick++) {
            tickController(controller);
        }
        helper.assertTrue(controller.getHeatBuffer() == 0 && !controller.isMenuLit()
                        && controller.getMenuLitProgress() == 0,
                family.id() + " visual grace must not create heat or a misleading GUI flame");
        helper.assertTrue(helper.getBlockState(fixture.controllerPos()).getValue(ColossalFurnaceControllerBlock.LIT),
                family.id() + " bamboo shutdown lost its visual grace");
        tickController(controller);
        helper.assertTrue(helper.getBlockState(fixture.controllerPos()).getValue(ColossalFurnaceControllerBlock.LIT),
                family.id() + " bamboo shutdown grace was too short");
        for (int tick = 0; tick < 4; tick++) {
            tickController(controller);
        }
        helper.assertFalse(helper.getBlockState(fixture.controllerPos()).getValue(ColossalFurnaceControllerBlock.LIT),
                family.id() + " bamboo visuals remained active after shutdown grace");
        assertShellLightSources(helper, family, size, 0);
        helper.succeed();
    }

    private static void testLitRevalidation(GameTestHelper helper, Family family, int size) {
        Fixture fixture = buildAndFormShell(helper, family, size);
        ColossalFurnaceControllerBlockEntity controller = fixture.controller();
        controller.getInputHandler().setStackInSlot(0, new ItemStack(family.validInput(), 64));
        controller.getFuelHandler().setStackInSlot(0, new ItemStack(Items.COAL, 4));
        tickController(controller);
        int sourceCount = Math.max(1, size - 2);
        assertShellLightSources(helper, family, size, sourceCount);
        int heat = controller.getHeatBuffer();
        int progress = getPrimaryLaneProgress(controller);
        int inputCount = controller.getInputHandler().getStackInSlot(0).getCount();
        int fuelCount = controller.getFuelHandler().getStackInSlot(0).getCount();
        int outputCount = controller.getOutputHandler().getStackInSlot(0).getCount();

        for (int attempt = 0; attempt < 3; attempt++) {
            helper.assertTrue(controller.revalidateStructure(null, true),
                    family.id() + " active structure failed revalidation");
            helper.assertTrue(helper.getBlockState(fixture.controllerPos()).getValue(ColossalFurnaceControllerBlock.LIT),
                    family.id() + " active Core lost its lit state on revalidation");
            assertShellLightSources(helper, family, size, sourceCount);
            helper.assertTrue(controller.getHeatBuffer() == heat && getPrimaryLaneProgress(controller) == progress,
                    family.id() + " revalidation changed heat or cooking progress");
            helper.assertTrue(controller.getInputHandler().getStackInSlot(0).getCount() == inputCount
                            && controller.getFuelHandler().getStackInSlot(0).getCount() == fuelCount
                            && controller.getOutputHandler().getStackInSlot(0).getCount() == outputCount,
                    family.id() + " revalidation changed stored items");
        }
        tickController(controller);
        assertShellLightSources(helper, family, size, sourceCount);
        controller.getFuelHandler().setStackInSlot(0, ItemStack.EMPTY);
        for (int tick = 0; tick < 500 && helper.getBlockState(fixture.controllerPos()).getValue(ColossalFurnaceControllerBlock.LIT); tick++) {
            tickController(controller);
        }
        helper.assertFalse(helper.getBlockState(fixture.controllerPos()).getValue(ColossalFurnaceControllerBlock.LIT),
                family.id() + " did not shut down after exhausting heat");
        helper.assertTrue(controller.revalidateStructure(null, true),
                family.id() + " idle structure failed revalidation");
        assertShellLightSources(helper, family, size, 0);
        helper.succeed();
    }

    private static void testLitGracePeriod(GameTestHelper helper, Family family) {
        Fixture fixture = buildAndFormShell(helper, family, 5);
        ColossalFurnaceControllerBlockEntity controller = fixture.controller();
        controller.getInputHandler().setStackInSlot(0, new ItemStack(family.validInput(), 2));
        controller.getFuelHandler().setStackInSlot(0, new ItemStack(Items.STICK));

        boolean exhausted = false;
        for (int tick = 0; tick < 10; tick++) {
            tickController(controller);
            if (controller.getHeatBuffer() == 0 && controller.getFuelHandler().getStackInSlot(0).isEmpty()) {
                exhausted = true;
                break;
            }
        }

        helper.assertTrue(exhausted, family.id() + " did not exhaust test fuel");
        helper.assertTrue(helper.getBlockState(fixture.controllerPos()).getValue(ColossalFurnaceControllerBlock.LIT),
                family.id() + " dropped its lit texture immediately when fuel expired");
        helper.assertFalse(controller.isMenuLit(),
                family.id() + " kept the GUI flame active during visual grace");
        helper.assertTrue(helper.getLevel().getLightEmission(helper.absolutePos(fixture.controllerPos())) == 0,
                family.id() + " emitted light from its hidden core");
        assertShellLightSources(helper, family, 5, 3);

        tickController(controller);
        helper.assertTrue(helper.getBlockState(fixture.controllerPos()).getValue(ColossalFurnaceControllerBlock.LIT),
                family.id() + " visual grace period was too short");
        for (int tick = 0; tick < 4; tick++) {
            tickController(controller);
        }
        helper.assertFalse(helper.getBlockState(fixture.controllerPos()).getValue(ColossalFurnaceControllerBlock.LIT),
                family.id() + " remained lit after its visual grace period");
        assertShellLightSources(helper, family, 5, 0);
        helper.succeed();
    }

    private static void testFuelStarvationDecay(GameTestHelper helper, Family family) {
        Fixture fixture = buildAndFormShell(helper, family, 2);
        ColossalFurnaceControllerBlockEntity controller = fixture.controller();
        controller.getInputHandler().setStackInSlot(0, new ItemStack(family.validInput()));
        controller.getFuelHandler().setStackInSlot(0, new ItemStack(Items.BAMBOO));

        for (int tick = 0; tick < 20 && (controller.getHeatBuffer() > 0
                || !controller.getFuelHandler().getStackInSlot(0).isEmpty()); tick++) {
            tickController(controller);
        }

        int progressBeforeDecay = getPrimaryLaneProgress(controller);
        helper.assertTrue(progressBeforeDecay > 2,
                family.id() + " did not retain partial progress after fuel starvation");
        tickController(controller);
        int decayedProgress = getPrimaryLaneProgress(controller);
        helper.assertTrue(decayedProgress == progressBeforeDecay - 2,
                family.id() + " did not decay stalled progress by the vanilla rate");
        helper.assertTrue(controller.getInputHandler().getStackInSlot(0).getCount() == 1,
                family.id() + " removed its reserved input while progress decayed");

        controller.getFuelHandler().setStackInSlot(0, new ItemStack(Items.STICK));
        tickController(controller);
        helper.assertTrue(getPrimaryLaneProgress(controller) > decayedProgress,
                family.id() + " did not resume from decayed progress when refueled");
        helper.succeed();
    }

    private static void testIdleFuelDrain(GameTestHelper helper, Family family) {
        int size = 2;
        Fixture fixture = buildAndFormShell(helper, family, size);
        ColossalFurnaceControllerBlockEntity controller = fixture.controller();
        controller.getInputHandler().setStackInSlot(0, new ItemStack(family.validInput()));
        controller.getFuelHandler().setStackInSlot(0, new ItemStack(Items.COAL));
        processItems(controller, family, size, 1);

        int heatBeforeIdleTick = controller.getHeatBuffer();
        ItemStack outputBeforeIdleTick = controller.getOutputHandler().getStackInSlot(0).copy();
        tickController(controller);

        helper.assertTrue(controller.getHeatBuffer() == heatBeforeIdleTick - 1,
                family.id() + " did not drain exactly one idle fuel tick");
        helper.assertTrue(ItemStack.matches(outputBeforeIdleTick, controller.getOutputHandler().getStackInSlot(0)),
                family.id() + " changed its output while idle");
        helper.succeed();
    }

    private static void testBlockedOutput(GameTestHelper helper, Family family) {
        int size = 2;
        Fixture fixture = buildAndFormShell(helper, family, size);
        ColossalFurnaceControllerBlockEntity controller = fixture.controller();
        controller.getInputHandler().setStackInSlot(0, new ItemStack(family.validInput()));
        controller.getFuelHandler().setStackInSlot(0, new ItemStack(Items.COAL));
        tickController(controller);

        int progressBeforeBlock = getPrimaryLaneProgress(controller);
        for (int slot = 0; slot < controller.getOutputSlotCount(); slot++) {
            controller.getOutputHandler().setStackInSlot(
                    slot,
                    new ItemStack(family.expectedOutput(), new ItemStack(family.expectedOutput()).getMaxStackSize())
            );
        }
        int heatBeforeBlock = controller.getHeatBuffer();
        tickController(controller);

        helper.assertTrue(getPrimaryLaneProgress(controller) == progressBeforeBlock,
                family.id() + " advanced cooking progress while its output was blocked");
        helper.assertTrue(controller.getHeatBuffer() == heatBeforeBlock - 1,
                family.id() + " did not apply vanilla-style idle fuel drain while blocked");

        controller.getOutputHandler().setStackInSlot(0, ItemStack.EMPTY);
        int workPerTick = size * size;
        int remainingTicks = divideRoundUp(family.cookingTime() - progressBeforeBlock, workPerTick);
        for (int tick = 0; tick < remainingTicks; tick++) {
            tickController(controller);
        }
        ItemStack resumedOutput = controller.getOutputHandler().getStackInSlot(0);
        helper.assertTrue(resumedOutput.is(family.expectedOutput()) && resumedOutput.getCount() == 1,
                family.id() + " did not resume correctly after output space became available");
        helper.assertTrue(countStoredRecipeUses(controller) == 1,
                family.id() + " duplicated or lost a recipe while output was blocked");
        helper.succeed();
    }

    private static void testBatchCarryover(GameTestHelper helper, Family family) {
        int size = 4;
        Fixture fixture = buildAndFormShell(helper, family, size);
        ColossalFurnaceControllerBlockEntity controller = fixture.controller();
        controller.getInputHandler().setStackInSlot(0, new ItemStack(family.validInput(), 2));
        controller.getFuelHandler().setStackInSlot(0, new ItemStack(Items.COAL));

        int workPerTick = size * size;
        int firstCompletionTicks = divideRoundUp(family.cookingTime(), workPerTick);
        int expectedCarryover = firstCompletionTicks * workPerTick - family.cookingTime();
        helper.assertTrue(expectedCarryover > 0, family.id() + " selected a carryover test case with no remainder");
        for (int tick = 0; tick < firstCompletionTicks; tick++) {
            tickController(controller);
        }

        ItemStack firstOutput = controller.getOutputHandler().getStackInSlot(0);
        helper.assertTrue(firstOutput.is(family.expectedOutput()) && firstOutput.getCount() == 1,
                family.id() + " did not complete the first batch item on schedule");
        helper.assertTrue(getPrimaryLaneProgress(controller) == expectedCarryover,
                family.id() + " did not carry " + expectedCarryover + " work into the next item");

        int totalCompletionTicks = divideRoundUp(family.cookingTime() * 2, workPerTick);
        for (int tick = firstCompletionTicks; tick < totalCompletionTicks; tick++) {
            tickController(controller);
        }
        ItemStack finalOutput = controller.getOutputHandler().getStackInSlot(0);
        helper.assertTrue(finalOutput.is(family.expectedOutput()) && finalOutput.getCount() == 2,
                family.id() + " did not preserve full two-item batch throughput");
        helper.succeed();
    }

    private static void testStructureConfigLimits(GameTestHelper helper, Family family) {
        Fixture fixture = buildAndFormShell(helper, family, 3);
        BlockPos controllerPos = helper.absolutePos(fixture.controllerPos());

        MultiblockValidationResult rejected = family.validator().validate(helper.getLevel(), controllerPos, 2);
        helper.assertFalse(rejected.valid(), family.id() + " accepted a structure above the supplied maximum size");
        helper.assertTrue(rejected.error() == StructureError.TOO_LARGE,
                family.id() + " reported the wrong error for an oversized structure");

        MultiblockValidationResult accepted = family.validator().validate(helper.getLevel(), controllerPos, 3);
        helper.assertTrue(accepted.valid() && accepted.outerSize() == 3,
                family.id() + " rejected a structure equal to the supplied maximum size");
        helper.succeed();
    }

    private static void testModdedFuel(GameTestHelper helper, Family family) {
        int burnTime = 321;
        ItemStack testFuel = new ItemStack(Items.DIAMOND);
        Consumer<FurnaceFuelBurnTimeEvent> listener = event -> {
            if (event.getRecipeType() == family.recipeType()
                    && event.getItemStack().is(testFuel.getItem())) {
                event.setBurnTime(burnTime);
            }
        };

        NeoForge.EVENT_BUS.addListener(listener);
        try {
            int size = 2;
            Fixture fixture = buildAndFormShell(helper, family, size);
            ColossalFurnaceControllerBlockEntity controller = fixture.controller();
            helper.assertTrue(controller.isFuel(testFuel), family.id() + " rejected the event-defined fuel");

            fixture.interfaceBlockEntity().setMode(ColossalFurnaceInterfaceMode.FUEL);
            IItemHandler fuelInterface = helper.getLevel().getCapability(
                    Capabilities.ItemHandler.BLOCK,
                    helper.absolutePos(fixture.interfacePos()),
                    null
            );
            helper.assertTrue(fuelInterface != null, family.id() + " fuel interface exposed no item capability");
            helper.assertTrue(fuelInterface.insertItem(0, testFuel.copy(), false).isEmpty(),
                    family.id() + " fuel interface rejected the event-defined fuel");

            controller.getInputHandler().setStackInSlot(0, new ItemStack(family.validInput()));
            tickController(controller);
            int expectedHeat = burnTime - size * size;
            helper.assertTrue(controller.getHeatBuffer() == expectedHeat,
                    family.id() + " used the wrong modded fuel burn time");
            helper.assertTrue(controller.getFuelHandler().getStackInSlot(0).isEmpty(),
                    family.id() + " did not consume the modded fuel item");
            helper.succeed();
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
        }
    }

    private static int countStoredRecipeUses(ColossalFurnaceControllerBlockEntity controller) {
        CompoundTag recipesUsedTag = controller.getUpdateTag(controller.getLevel().registryAccess()).getCompound("RecipesUsed");
        return recipesUsedTag.getAllKeys().stream().mapToInt(recipesUsedTag::getInt).sum();
    }

    private static int getPrimaryLaneProgress(ColossalFurnaceControllerBlockEntity controller) {
        ListTag lanes = controller.getUpdateTag(controller.getLevel().registryAccess()).getList("Lanes", Tag.TAG_COMPOUND);
        return lanes.isEmpty() ? 0 : lanes.getCompound(0).getInt("Progress");
    }

    private static List<ExperienceOrb> findExperienceOrbs(GameTestHelper helper, Vec3 position) {
        return helper.getLevel().getEntitiesOfClass(
                ExperienceOrb.class,
                new AABB(position, position).inflate(2.0D)
        );
    }

    private static void processItems(ColossalFurnaceControllerBlockEntity controller, Family family, int size, int itemCount) {
        int workPerTick = size * size;
        int completionTicks = divideRoundUp(family.cookingTime() * itemCount, workPerTick);
        for (int tick = 0; tick < completionTicks; tick++) {
            tickController(controller);
        }
    }

    private static Fixture buildAndFormShell(GameTestHelper helper, Family family, int size) {
        Fixture fixture = buildShell(helper, family, size);
        helper.assertTrue(fixture.controller().revalidateStructure(null, true),
                family.id() + " size " + size + " failed explicit validation");
        assertFormed(helper, fixture.controller(), size);
        return fixture;
    }

    private static Fixture buildShell(GameTestHelper helper, Family family, int size) {
        BlockPos controllerPos = BASE;
        BlockPos interfacePos = BASE.offset(size - 1, 0, 0);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    if (!isBoundary(x, y, z, size)) {
                        continue;
                    }

                    BlockPos pos = BASE.offset(x, y, z);
                    if (pos.equals(controllerPos)) {
                        continue;
                    }
                    helper.setBlock(pos, pos.equals(interfacePos) ? family.interfaceBlock().get() : family.wall().get());
                }
            }
        }
        helper.setBlock(controllerPos, family.core().get());

        ColossalFurnaceControllerBlockEntity controller =
                (ColossalFurnaceControllerBlockEntity) helper.getBlockEntity(controllerPos);
        ColossalFurnaceInterfaceBlockEntity interfaceBlockEntity =
                (ColossalFurnaceInterfaceBlockEntity) helper.getBlockEntity(interfacePos);
        return new Fixture(controllerPos, interfacePos, controller, interfaceBlockEntity);
    }

    private static void assertFormed(GameTestHelper helper, ColossalFurnaceControllerBlockEntity controller, int expectedSize) {
        helper.assertTrue(controller.isFormed(), "Structure did not form");
        helper.assertTrue(controller.getOuterSize() == expectedSize,
                "Expected outer size " + expectedSize + ", got " + controller.getOuterSize());
        helper.assertTrue(controller.getStoredParallelism() == expectedSize * expectedSize,
                "Stored parallelism did not match size squared");
    }

    private static void assertShellFormed(GameTestHelper helper, Family family, int size, boolean expected) {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    if (!isBoundary(x, y, z, size)) {
                        continue;
                    }
                    BlockState state = helper.getBlockState(BASE.offset(x, y, z));
                    helper.assertTrue(readFormed(state) == expected,
                            family.id() + " shell formed state mismatch at " + BASE.offset(x, y, z));
                }
            }
        }
    }

    private static void assertShellLightSources(GameTestHelper helper, Family family, int size, int expectedCount) {
        int sourceCount = 0;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    if (!isBoundary(x, y, z, size)) {
                        continue;
                    }
                    int emission = helper.getLevel().getLightEmission(helper.absolutePos(BASE.offset(x, y, z)));
                    if (emission == 0) {
                        continue;
                    }
                    helper.assertTrue(emission == 13,
                            family.id() + " shell light source did not match vanilla furnace level");
                    helper.assertTrue(y == 0 && z == 0,
                            family.id() + " emitted light outside its lower front row");
                    sourceCount++;
                }
            }
        }
        helper.assertTrue(sourceCount == expectedCount,
                family.id() + " expected " + expectedCount + " shell light sources, got " + sourceCount);
    }

    private static boolean readFormed(BlockState state) {
        if (state.getBlock() instanceof ColossalFurnaceControllerBlock) {
            return state.getValue(ColossalFurnaceControllerBlock.FORMED);
        }
        if (state.getBlock() instanceof ColossalFurnaceInterfaceBlock) {
            return state.getValue(ColossalFurnaceInterfaceBlock.FORMED);
        }
        if (state.getBlock() instanceof ColossalFurnaceWallBlock) {
            return state.getValue(ColossalFurnaceWallBlock.FORMED);
        }
        return false;
    }

    private static void tickController(ColossalFurnaceControllerBlockEntity controller) {
        Level level = controller.getLevel();
        if (level == null) {
            throw new IllegalStateException("Controller is not attached to a level");
        }
        ColossalFurnaceControllerBlockEntity.serverTick(
                level,
                controller.getBlockPos(),
                controller.getBlockState(),
                controller
        );
    }

    private static boolean isBoundary(int x, int y, int z, int size) {
        int max = size - 1;
        return x == 0 || x == max || y == 0 || y == max || z == 0 || z == max;
    }

    private static int divideRoundUp(int value, int divisor) {
        return (value + divisor - 1) / divisor;
    }

    private record Family(
            String id,
            Supplier<Block> core,
            Supplier<Block> wall,
            Supplier<Block> interfaceBlock,
            StructureValidator validator,
            RecipeType<? extends AbstractCookingRecipe> recipeType,
            Item validInput,
            Item invalidInput,
            Item expectedOutput,
            int cookingTime
    ) {
    }

    @FunctionalInterface
    private interface StructureValidator {
        MultiblockValidationResult validate(Level level, BlockPos startPos, int maximumSize);
    }

    private record Fixture(
            BlockPos controllerPos,
            BlockPos interfacePos,
            ColossalFurnaceControllerBlockEntity controller,
            ColossalFurnaceInterfaceBlockEntity interfaceBlockEntity
    ) {
    }
}
