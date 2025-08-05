package net.idothehax.theoldbroadcast.world.dimension;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraftforge.registries.ForgeRegistries;
import net.idothehax.theoldbroadcast.Theoldbroadcast;
import net.minecraft.resources.ResourceLocation;

import java.util.stream.Stream;

public class OldBroadcastBiomeSource extends BiomeSource {
    public static final Codec<OldBroadcastBiomeSource> CODEC = Codec.unit(OldBroadcastBiomeSource::new);

    private final Holder<Biome> studioBiome;

    public OldBroadcastBiomeSource() {
        this.studioBiome = ForgeRegistries.BIOMES.getHolder(
            ResourceLocation.fromNamespaceAndPath(Theoldbroadcast.MODID, "studio_biome")
        ).orElse(ForgeRegistries.BIOMES.getHolder(
            ResourceLocation.fromNamespaceAndPath("minecraft", "plains")
        ).orElseThrow());
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(studioBiome);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        return studioBiome;
    }
}
