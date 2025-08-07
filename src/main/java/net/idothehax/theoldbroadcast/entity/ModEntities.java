package net.idothehax.theoldbroadcast.entity;

import net.idothehax.theoldbroadcast.Theoldbroadcast;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Theoldbroadcast.MODID);

    public static final RegistryObject<EntityType<ShadowGhostEntity>> SHADOW_GHOST = ENTITIES.register("shadow_ghost",
            () -> EntityType.Builder.of(ShadowGhostEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 2.0f)
                    .clientTrackingRange(32)
                    .build("shadow_ghost"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }

    @Mod.EventBusSubscriber(modid = Theoldbroadcast.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class EntityAttributeHandler {
        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(SHADOW_GHOST.get(), ShadowGhostEntity.createAttributes().build());
        }
    }
}
