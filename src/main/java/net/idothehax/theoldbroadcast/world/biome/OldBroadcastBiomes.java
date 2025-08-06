package net.idothehax.theoldbroadcast.world.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.idothehax.theoldbroadcast.Theoldbroadcast;

public class OldBroadcastBiomes {
    public static final ResourceKey<Biome> OLD_BROADCAST_BIOME = ResourceKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Theoldbroadcast.MODID, "the_old_studio"));

    public static void bootstrap(BootstapContext<Biome> context) {
        context.register(OLD_BROADCAST_BIOME, theOldStudioBiome(context));
    }

    private static Biome theOldStudioBiome(BootstapContext<Biome> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> worldCarvers = context.lookup(Registries.CONFIGURED_CARVER);

        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers);

        // Add infinite Backrooms generation
        generation.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES,
                placedFeatures.getOrThrow(ResourceKey.create(Registries.PLACED_FEATURE,
                        ResourceLocation.fromNamespaceAndPath(Theoldbroadcast.MODID, "studio_infinite"))));

        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        // No natural mob spawning in the studio

        BiomeSpecialEffects.Builder effects = new BiomeSpecialEffects.Builder()
                .waterColor(0x0d0d0d)
                .waterFogColor(0x0d0d0d)
                .skyColor(0x0d0d0d)
                .grassColorOverride(0x2d2d2d)
                .foliageColorOverride(0x2d2d2d)
                .fogColor(0x1a1a1a)
                .ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0))
                .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_CREATIVE));

        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.5f)
                .downfall(0.0f)
                .specialEffects(effects.build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }
}
