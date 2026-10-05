package com.colossalfurnaces.client;

import com.colossalfurnaces.registry.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.RisingParticle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;

public final class ColossalFurnaceParticles {
    private ColossalFurnaceParticles() {
    }

    public static void registerProviders(RegisterParticleProvidersEvent event) {
        for (ModParticles.Entry entry : ModParticles.ENTRIES) {
            event.registerSpriteSet(entry.type().get(), sprites -> {
                if (entry.flame()) {
                    return (options, level, x, y, z, vx, vy, vz) ->
                        new ScaledFlameParticle(level, x, y, z, vx, vy, vz, sprites, entry.scale());
                }
                return (options, level, x, y, z, vx, vy, vz) ->
                        new ScaledSmokeParticle(level, x, y, z, vx, vy, vz, sprites, entry.scale());
            });
        }
    }

    // Forge 1.20.1 has a package-private flame constructor; preserve its vanilla behavior here.
    private static final class ScaledFlameParticle extends RisingParticle {
        private ScaledFlameParticle(ClientLevel level, double x, double y, double z,
                                    double vx, double vy, double vz, SpriteSet sprites, float scale) {
            super(level, x, y, z, vx, vy, vz);
            this.pickSprite(sprites);
            this.quadSize *= scale;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
        }

        @Override
        public void move(double x, double y, double z) {
            this.setBoundingBox(this.getBoundingBox().move(x, y, z));
            this.setLocationFromBoundingbox();
        }

        @Override
        public float getQuadSize(float partialTick) {
            float ageRatio = (this.age + partialTick) / this.lifetime;
            return this.quadSize * (1.0F - ageRatio * ageRatio * 0.5F);
        }

        @Override
        public int getLightColor(float partialTick) {
            float ageRatio = Mth.clamp((this.age + partialTick) / this.lifetime, 0.0F, 1.0F);
            int light = super.getLightColor(partialTick);
            int blockLight = Math.min(240, (light & 255) + (int) (ageRatio * 15.0F * 16.0F));
            return blockLight | ((light >> 16 & 255) << 16);
        }
    }

    private static final class ScaledSmokeParticle extends SmokeParticle {
        private ScaledSmokeParticle(ClientLevel level, double x, double y, double z,
                                    double vx, double vy, double vz, SpriteSet sprites, float scale) {
            super(level, x, y, z, vx, vy, vz, 1.0F, sprites);
            // Keep vanilla lifetime and collision bounds; scale the quad and all motion equally.
            this.quadSize *= scale;
            this.xd *= scale;
            this.yd *= scale;
            this.zd *= scale;
            this.gravity *= scale;
        }
    }
}
