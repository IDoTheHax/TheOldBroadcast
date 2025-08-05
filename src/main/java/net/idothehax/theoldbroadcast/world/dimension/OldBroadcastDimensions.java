package net.idothehax.theoldbroadcast.world.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.idothehax.theoldbroadcast.Theoldbroadcast;

public class OldBroadcastDimensions {
    public static final ResourceKey<Level> OLD_BROADCAST_LEVEL = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Theoldbroadcast.MODID, "old_broadcast"));

    public static final ResourceKey<DimensionType> OLD_BROADCAST_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(Theoldbroadcast.MODID, "old_broadcast"));
}
