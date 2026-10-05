package com.colossalfurnaces.blockentity;

import com.colossalfurnaces.menu.ColossalBlastFurnaceMenu;
import com.colossalfurnaces.registry.ModBlockEntities;
import com.colossalfurnaces.util.ColossalBlastFurnaceStructure;
import com.colossalfurnaces.util.MultiblockValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ColossalBlastFurnaceControllerBlockEntity extends ColossalFurnaceControllerBlockEntity {
    public ColossalBlastFurnaceControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COLOSSAL_BLAST_FURNACE_CORE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.colossalfurnaces.colossal_blast_furnace");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (!this.isFormed()) {
            return null;
        }
        return new ColossalBlastFurnaceMenu(containerId, inventory, this, this.getMenuData());
    }

    @Override
    protected void spawnWorkingParticles() {
        if (this.level != null
                && this.level.random.nextFloat() < this.getSmokeParticleChance(Math.max(2, this.getOuterSize()))) {
            this.spawnFrontSmokeParticle(9.0D / 16.0D);
        }
    }

    @Override
    protected MultiblockValidationResult validateStructure(Level level, BlockPos origin) {
        return ColossalBlastFurnaceStructure.validate(level, origin);
    }

    @Override
    protected boolean isShellBlock(BlockState state) {
        return ColossalBlastFurnaceStructure.isShell(state);
    }

    @Override
    protected RecipeType<? extends AbstractCookingRecipe> getRecipeType() {
        return RecipeType.BLASTING;
    }
}
