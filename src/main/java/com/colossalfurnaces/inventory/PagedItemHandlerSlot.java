package com.colossalfurnaces.inventory;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.function.IntSupplier;
import java.util.function.Predicate;

public class PagedItemHandlerSlot extends Slot {
    private static final SimpleContainer DUMMY_CONTAINER = new SimpleContainer(0);
    private final IItemHandlerModifiable itemHandler;
    private final IntSupplier indexSupplier;
    private final Predicate<ItemStack> placementRule;
    private final boolean allowPickup;

    public PagedItemHandlerSlot(IItemHandlerModifiable itemHandler, IntSupplier indexSupplier, int x, int y, Predicate<ItemStack> placementRule, boolean allowPickup) {
        super(DUMMY_CONTAINER, 0, x, y);
        this.itemHandler = itemHandler;
        this.indexSupplier = indexSupplier;
        this.placementRule = placementRule;
        this.allowPickup = allowPickup;
    }

    private int getDynamicIndex() {
        int index = this.indexSupplier.getAsInt();
        return index >= 0 && index < this.itemHandler.getSlots() ? index : -1;
    }

    @Override
    public boolean hasItem() {
        int index = this.getDynamicIndex();
        return index >= 0 && !this.itemHandler.getStackInSlot(index).isEmpty();
    }

    @Override
    public ItemStack getItem() {
        int index = this.getDynamicIndex();
        return index >= 0 ? this.itemHandler.getStackInSlot(index) : ItemStack.EMPTY;
    }

    @Override
    public void set(ItemStack stack) {
        int index = this.getDynamicIndex();
        if (index >= 0) {
            this.itemHandler.setStackInSlot(index, stack);
            this.setChanged();
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        int index = this.getDynamicIndex();
        return index >= 0 ? this.itemHandler.getSlotLimit(index) : 64;
    }

    @Override
    public ItemStack remove(int amount) {
        int index = this.getDynamicIndex();
        return index >= 0 ? this.itemHandler.extractItem(index, amount, false) : ItemStack.EMPTY;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        int index = this.getDynamicIndex();
        return index >= 0 && this.placementRule.test(stack) && this.itemHandler.isItemValid(index, stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        return this.allowPickup && this.getDynamicIndex() >= 0;
    }
}
