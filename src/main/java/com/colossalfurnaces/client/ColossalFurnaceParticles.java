package com.colossalfurnaces.client;

import com.colossalfurnaces.registry.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SpriteSet;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

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

    private static final class ScaledFlameParticle extends FlameParticle {
        private ScaledFlameParticle(ClientLevel level, double x, double y, double z,
                                    double vx, double vy, double vz, SpriteSet sprites, float scale) {
            super(level, x, y, z, vx, vy, vz);
            this.pickSprite(sprites);
            this.quadSize *= scale;
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
