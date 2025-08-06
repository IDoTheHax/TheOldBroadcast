package net.idothehax.theoldbroadcast.world.feature;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.idothehax.theoldbroadcast.Theoldbroadcast;
import net.minecraftforge.registries.RegistryObject;

public class StudioFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, Theoldbroadcast.MODID);


    // Advanced infinite The Old Studio generation
    public static final RegistryObject<TheOldStudioFeature> THE_OLD_STUDIO = FEATURES.register("studio_infinite",
            () -> new TheOldStudioFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}