package net.idothehax.theoldbroadcast.world.feature;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.idothehax.theoldbroadcast.Theoldbroadcast;

public class StudioFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, Theoldbroadcast.MODID);

    public static final RegistryObject<StudioRoomFeature> STUDIO_ROOM = FEATURES.register("studio_room",
            () -> new StudioRoomFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<StudioMazeFeature> STUDIO_MAZE = FEATURES.register("studio_maze",
            () -> new StudioMazeFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
