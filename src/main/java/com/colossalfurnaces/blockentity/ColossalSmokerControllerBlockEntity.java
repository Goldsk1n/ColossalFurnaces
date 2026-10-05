package com.colossalfurnaces.blockentity;

import com.colossalfurnaces.menu.ColossalSmokerMenu;
import com.colossalfurnaces.registry.ModBlockEntities;
import com.colossalfurnaces.registry.ModParticles;
import com.colossalfurnaces.util.ColossalSmokerStructure;
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

public class ColossalSmokerControllerBlockEntity extends ColossalFurnaceControllerBlockEntity {
    public ColossalSmokerControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COLOSSAL_SMOKER_CORE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.colossalfurnaces.colossal_smoker");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (!this.isFormed()) {
            return null;
        }
        return new ColossalSmokerMenu(containerId, inventory, this, this.getMenuData());
    }

    @Override
    protected void spawnWorkingParticles() {
        if (this.level == null
                || this.level.random.nextFloat() >= this.getSmokeParticleChance(Math.max(2, this.getOuterSize()))) {
            return;
        }

        BlockPos min = this.getMinPos();
        BlockPos max = this.getMaxPos();
        double x = (min.getX() + max.getX() + 1.0D) * 0.5D;
        double y = max.getY() + 1.1D;
        double z = (min.getZ() + max.getZ() + 1.0D) * 0.5D;
        this.level.addParticle(ModParticles.smokeForSize(this.getOuterSize()), x, y, z, 0.0D, 0.0D, 0.0D);
    }

    @Override
    protected MultiblockValidationResult validateStructure(Level level, BlockPos origin) {
        return ColossalSmokerStructure.validate(level, origin);
    }

    @Override
    protected boolean isShellBlock(BlockState state) {
        return ColossalSmokerStructure.isShell(state);
    }

    @Override
    protected RecipeType<? extends AbstractCookingRecipe> getRecipeType() {
        return RecipeType.SMOKING;
    }
}
