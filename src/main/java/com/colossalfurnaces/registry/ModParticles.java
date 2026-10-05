package com.colossalfurnaces.registry;

import com.colossalfurnaces.ColossalFurnacesMod;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTER =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ColossalFurnacesMod.MOD_ID);
    public static final List<Entry> ENTRIES = List.of(
            register("colossal_smoke_2", 2, false), register("colossal_flame_2", 2, true),
            register("colossal_smoke_3", 3, false), register("colossal_flame_3", 3, true),
            register("colossal_smoke_4", 4, false), register("colossal_flame_4", 4, true),
            register("colossal_smoke_5", 5, false), register("colossal_flame_5", 5, true));

    private ModParticles() {
    }

    private static Entry register(String name, int size, boolean flame) {
        return new Entry(REGISTER.register(name, () -> new SimpleParticleType(false)), scaleForSize(size), flame);
    }

    public static float scaleForSize(int size) {
        return Math.max(2, Math.min(5, size));
    }

    public static double frontOffsetForSize(int size) {
        // Larger camera-facing sprites need clearance from the front surface.
        return 0.10D * scaleForSize(size);
    }

    public static SimpleParticleType smokeForSize(int size) {
        return entryForSize(size, false).type().get();
    }

    public static SimpleParticleType flameForSize(int size) {
        return entryForSize(size, true).type().get();
    }

    private static Entry entryForSize(int size, boolean flame) {
        return ENTRIES.get((Math.max(2, Math.min(5, size)) - 2) * 2 + (flame ? 1 : 0));
    }

    public record Entry(Supplier<SimpleParticleType> type, float scale, boolean flame) {
    }
}
