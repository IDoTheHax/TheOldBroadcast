package net.idothehax.theoldbroadcast.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "theoldbroadcast");

    public static final RegistryObject<SoundEvent> VHS_PLACE = SOUNDS.register("block.vhs.place",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("theoldbroadcast", "block.vhs.place")));
    public static final RegistryObject<SoundEvent> VHS_STEP = SOUNDS.register("block.vhs.step",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("theoldbroadcast", "block.vhs.step")));
    public static final RegistryObject<SoundEvent> VHS_PASSIVE = SOUNDS.register("block.vhs.passive",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("theoldbroadcast", "block.vhs.passive")));

    public static final RegistryObject<SoundEvent> SHADOW_GHOST_APPEAR = SOUNDS.register("entity.shadow_ghost.appear",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("theoldbroadcast", "entity.shadow_ghost.appear")));
    public static final RegistryObject<SoundEvent> SHADOW_GHOST_DISAPPEAR = SOUNDS.register("entity.shadow_ghost.disappear",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("theoldbroadcast", "entity.shadow_ghost.disappear")));
    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}

