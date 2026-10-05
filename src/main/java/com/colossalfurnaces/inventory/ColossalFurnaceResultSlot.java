package com.colossalfurnaces.inventory;

import com.colossalfurnaces.blockentity.ColossalFurnaceControllerBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.function.IntSupplier;

public final class ColossalFurnaceResultSlot extends PagedItemHandlerSlot {
    private final Player player;
    private final ColossalFurnaceControllerBlockEntity controller;
    private int removeCount;

    public ColossalFurnaceResultSlot(Player player, ColossalFurnaceControllerBlockEntity controller,
                                     IItemHandlerModifiable itemHandler, IntSupplier indexSupplier,
                                     int x, int y) {
        super(itemHandler, indexSupplier, x, y, stack -> false, true);
        this.player = player;
        this.controller = controller;
    }

    @Override
    public ItemStack remove(int amount) {
        if (this.hasItem()) {
            this.removeCount += Math.min(amount, this.getItem().getCount());
        }
        return super.remove(amount);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        this.checkTakeAchievements(stack);
        super.onTake(player, stack);
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        this.removeCount += amount;
        this.checkTakeAchievements(stack);
    }

    @Override
    protected void checkTakeAchievements(ItemStack stack) {
        stack.onCraftedBy(this.player.level(), this.player, this.removeCount);
        if (this.player instanceof ServerPlayer serverPlayer) {
            this.controller.awardUsedRecipesAndPopExperience(serverPlayer);
        }
        this.removeCount = 0;
        EventHooks.firePlayerSmeltedEvent(this.player, stack);
    }
}
