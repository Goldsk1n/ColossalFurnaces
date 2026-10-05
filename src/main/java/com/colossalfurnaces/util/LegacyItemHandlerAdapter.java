package com.colossalfurnaces.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Transactional 26.1 capability view over the mod's slot-based inventory implementation. */
@SuppressWarnings("removal")
public final class LegacyItemHandlerAdapter extends SnapshotJournal<List<ItemStack>>
        implements ResourceHandler<ItemResource> {
    private final IItemHandlerModifiable handler;

    public LegacyItemHandlerAdapter(IItemHandlerModifiable handler) {
        this.handler = handler;
    }

    @Override
    public int size() {
        return this.handler.getSlots();
    }

    @Override
    public ItemResource getResource(int index) {
        return ItemResource.of(this.handler.getStackInSlot(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return this.handler.getStackInSlot(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return resource.isEmpty() || this.isValid(index, resource) ? this.handler.getSlotLimit(index) : 0;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return !resource.isEmpty() && this.handler.isItemValid(index, resource.toStack());
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0 || !this.isValid(index, resource)) {
            return 0;
        }
        ItemStack offered = resource.toStack(amount);
        int inserted = amount - this.handler.insertItem(index, offered, true).getCount();
        if (inserted > 0) {
            this.updateSnapshots(transaction);
            this.handler.insertItem(index, resource.toStack(inserted), false);
        }
        return inserted;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        ItemStack stored = this.handler.getStackInSlot(index);
        if (resource.isEmpty() || amount <= 0 || !resource.matches(stored)) {
            return 0;
        }
        int extracted = this.handler.extractItem(index, amount, true).getCount();
        if (extracted > 0) {
            this.updateSnapshots(transaction);
            this.handler.extractItem(index, extracted, false);
        }
        return extracted;
    }

    @Override
    protected List<ItemStack> createSnapshot() {
        List<ItemStack> snapshot = new ArrayList<>(this.handler.getSlots());
        for (int slot = 0; slot < this.handler.getSlots(); slot++) {
            snapshot.add(this.handler.getStackInSlot(slot).copy());
        }
        return snapshot;
    }

    @Override
    protected void revertToSnapshot(List<ItemStack> snapshot) {
        for (int slot = 0; slot < snapshot.size(); slot++) {
            this.handler.setStackInSlot(slot, snapshot.get(slot));
        }
    }
}
