package com.colossalfurnaces.menu;

import com.colossalfurnaces.blockentity.ColossalFurnaceControllerBlockEntity;
import com.colossalfurnaces.inventory.ColossalFurnaceResultSlot;
import com.colossalfurnaces.inventory.PagedItemHandlerSlot;
import com.colossalfurnaces.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public class ColossalFurnaceMenu extends AbstractContainerMenu {
    private static final double MAX_INTERACTION_DISTANCE_SQUARED = 8.0D * 8.0D;
    private static final int INPUT_SLOT_COUNT = 9;
    private static final int FUEL_SLOT_COUNT = 3;
    private static final int OUTPUT_SLOT_COUNT = 9;
    private static final int MACHINE_SLOT_COUNT = INPUT_SLOT_COUNT + FUEL_SLOT_COUNT + OUTPUT_SLOT_COUNT;
    private static final int INPUT_SLOT_START = 0;
    private static final int FUEL_SLOT_START = INPUT_SLOT_START + INPUT_SLOT_COUNT;
    private static final int OUTPUT_SLOT_START = FUEL_SLOT_START + FUEL_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;
    private static final int[][] INPUT_SLOT_POSITIONS = {
            {16, 18},
            {34, 18},
            {52, 18},
            {16, 36},
            {34, 36},
            {52, 36},
            {16, 54},
            {34, 54},
            {52, 54}
    };
    private static final int[][] FUEL_SLOT_POSITIONS = {
            {16, 90},
            {34, 90},
            {52, 90}
    };
    private static final int[][] OUTPUT_SLOT_POSITIONS = {
            {108, 36},
            {126, 36},
            {144, 36},
            {108, 54},
            {126, 54},
            {144, 54},
            {108, 72},
            {126, 72},
            {144, 72}
    };
    private final ColossalFurnaceControllerBlockEntity controller;
    private final ContainerData data;

    public ColossalFurnaceMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(ModMenus.COLOSSAL_FURNACE.get(), containerId, inventory, getControllerFromBuffer(inventory, buffer, ColossalFurnaceControllerBlockEntity.class));
    }

    public ColossalFurnaceMenu(int containerId, Inventory inventory, ColossalFurnaceControllerBlockEntity controller, ContainerData data) {
        this(ModMenus.COLOSSAL_FURNACE.get(), containerId, inventory, controller, data);
    }

    protected ColossalFurnaceMenu(MenuType<?> menuType, int containerId, Inventory inventory, ColossalFurnaceControllerBlockEntity controller) {
        this(menuType, containerId, inventory, controller, controller.getMenuData());
    }

    protected ColossalFurnaceMenu(MenuType<?> menuType, int containerId, Inventory inventory, ColossalFurnaceControllerBlockEntity controller, ContainerData data) {
        super(menuType, containerId);
        this.controller = controller;
        this.data = data;
        this.addDataSlots(data);
        this.addMachineSlots(inventory.player);
        this.addPlayerSlots(inventory);
    }

    protected static <T extends ColossalFurnaceControllerBlockEntity> T getControllerFromBuffer(Inventory inventory, FriendlyByteBuf buffer, Class<T> controllerClass) {
        var blockEntity = inventory.player.level().getBlockEntity(buffer.readBlockPos());
        if (controllerClass.isInstance(blockEntity)) {
            return controllerClass.cast(blockEntity);
        }
        throw new IllegalStateException("Missing colossal furnace controller for menu");
    }

    private void addMachineSlots(Player player) {
        this.addPositionedSlots(this.controller.getInputHandler(), INPUT_SLOT_POSITIONS, stack -> this.controller.isSmeltable(stack), true);
        this.addPositionedSlots(this.controller.getFuelHandler(), FUEL_SLOT_POSITIONS, stack -> this.controller.isFuel(stack), true);
        for (int slotIndex = 0; slotIndex < OUTPUT_SLOT_POSITIONS.length; slotIndex++) {
            int[] position = OUTPUT_SLOT_POSITIONS[slotIndex];
            final int resolvedIndex = slotIndex;
            this.addSlot(new ColossalFurnaceResultSlot(
                    player,
                    this.controller,
                    this.controller.getOutputHandler(),
                    () -> resolvedIndex,
                    position[0],
                    position[1]
            ));
        }
    }

    private void addPositionedSlots(IItemHandlerModifiable handler, int[][] positions,
                                    java.util.function.Predicate<ItemStack> placementRule, boolean allowPickup) {
        for (int slotIndex = 0; slotIndex < positions.length; slotIndex++) {
            int[] position = positions[slotIndex];
            final int resolvedIndex = slotIndex;
            this.addSlot(new PagedItemHandlerSlot(handler, () -> resolvedIndex, position[0], position[1], placementRule, allowPickup));
        }
    }

    private void addPlayerSlots(Inventory inventory) {
        int startY = 121;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, startY + row * 18));
            }
        }
        for (int hotbarIndex = 0; hotbarIndex < 9; hotbarIndex++) {
            this.addSlot(new Slot(inventory, hotbarIndex, 8 + hotbarIndex * 18, startY + 58));
        }
    }

    public boolean isFormed() {
        return this.data.get(0) == 1;
    }

    public int getOuterSize() {
        return this.data.get(1);
    }

    public int getProcessingParallelism() {
        return this.data.get(2);
    }

    public int getStoredParallelism() {
        return this.data.get(3);
    }

    public int getHeatBuffer() {
        return this.data.get(4);
    }

    public int getActiveLanes() {
        return this.data.get(5);
    }

    public boolean isLit() {
        return this.data.get(6) == 1;
    }

    public int getLitProgress() {
        return this.data.get(7);
    }

    public int getBurnProgress() {
        return this.data.get(8);
    }

    public ColossalFurnaceControllerBlockEntity getController() {
        return this.controller;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copied = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        copied = stack.copy();
        if (index >= OUTPUT_SLOT_START && index < MACHINE_SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copied);
        } else if (index < MACHINE_SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (this.controller.isSmeltable(stack)) {
            if (!this.moveItemStackTo(stack, INPUT_SLOT_START, INPUT_SLOT_START + INPUT_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (this.controller.isFuel(stack)) {
            if (!this.moveItemStackTo(stack, FUEL_SLOT_START, FUEL_SLOT_START + FUEL_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_INVENTORY_START && index < PLAYER_INVENTORY_END) {
            if (!this.moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= HOTBAR_START && index < HOTBAR_END) {
            if (!this.moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copied.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copied;
    }

    @Override
    public boolean stillValid(Player player) {
        if (!this.controller.isFormed()
                || this.controller.getLevel() == null
                || player.level() != this.controller.getLevel()
                || this.controller.getLevel().getBlockEntity(this.controller.getBlockPos()) != this.controller) {
            return false;
        }

        BlockPos min = this.controller.getMinPos();
        BlockPos max = this.controller.getMaxPos();
        // Include the full extent of the outer blocks, not just their integer origins.
        double closestX = Mth.clamp(player.getX(), min.getX(), max.getX() + 1.0D);
        double closestY = Mth.clamp(player.getY(), min.getY(), max.getY() + 1.0D);
        double closestZ = Mth.clamp(player.getZ(), min.getZ(), max.getZ() + 1.0D);
        return player.distanceToSqr(closestX, closestY, closestZ) <= MAX_INTERACTION_DISTANCE_SQUARED;
    }
}
