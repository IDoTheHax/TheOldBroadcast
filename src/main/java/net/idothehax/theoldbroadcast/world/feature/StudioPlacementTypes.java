package net.idothehax.theoldbroadcast.world.feature;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.idothehax.theoldbroadcast.Theoldbroadcast;

public class StudioPlacementTypes {
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Theoldbroadcast.MODID);

    public static final RegistryObject<PlacementModifierType<StudioGridPlacement>> STUDIO_GRID =
            PLACEMENT_MODIFIERS.register("studio_grid", () -> () -> StudioGridPlacement.CODEC);

    public static final RegistryObject<PlacementModifierType<StudioMazeGridPlacement>> STUDIO_MAZE_GRID =
            PLACEMENT_MODIFIERS.register("studio_maze_grid", () -> () -> StudioMazeGridPlacement.CODEC);

    public static void register(IEventBus eventBus) {
        PLACEMENT_MODIFIERS.register(eventBus);
    }
}
