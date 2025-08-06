package net.idothehax.theoldbroadcast.world.feature;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.mojang.serialization.Codec;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, "theoldbroadcast");

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> THE_OLD_STUDIO_FEATURE = FEATURES.register(
        "studio_infinite",
        () -> new TheOldStudioFeature(NoneFeatureConfiguration.CODEC)
    );

    public static void register() {
        FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
