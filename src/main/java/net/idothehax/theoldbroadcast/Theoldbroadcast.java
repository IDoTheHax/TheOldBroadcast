package net.idothehax.theoldbroadcast;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.idothehax.theoldbroadcast.block.BroadcastPortalBlock;
import net.idothehax.theoldbroadcast.sound.ModSounds;
import net.idothehax.theoldbroadcast.world.biome.OldBroadcastBiomes;
import net.idothehax.theoldbroadcast.world.dimension.OldBroadcastBiomeSource;
import net.idothehax.theoldbroadcast.world.dimension.OldBroadcastChunkGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.idothehax.theoldbroadcast.block.VintageTelevisionBlock;
import net.idothehax.theoldbroadcast.block.VHSTapeBlock;
import net.idothehax.theoldbroadcast.entity.ModEntities;
import net.idothehax.theoldbroadcast.world.structure.StudioStructures;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Theoldbroadcast.MODID)
public class Theoldbroadcast {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "theoldbroadcast";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "theoldbroadcast" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "theoldbroadcast" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "theoldbroadcast" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    // Register custom chunk generator and biome source codecs for the dimension
    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
        DeferredRegister.create(Registries.CHUNK_GENERATOR, MODID);
    public static final DeferredRegister<Codec<? extends BiomeSource>> BIOME_SOURCES =
        DeferredRegister.create(Registries.BIOME_SOURCE, MODID);
    public static final RegistryObject<Codec<OldBroadcastChunkGenerator>> OLD_BROADCAST_CHUNK_GENERATOR =
        CHUNK_GENERATORS.register("old_broadcast", () -> OldBroadcastChunkGenerator.CODEC);
    public static final RegistryObject<Codec<OldBroadcastBiomeSource>> OLD_BROADCAST_BIOME_SOURCE =
        BIOME_SOURCES.register("old_broadcast", () -> OldBroadcastBiomeSource.CODEC);

    // Create vintage television block
    public static final RegistryObject<Block> VINTAGE_TELEVISION = BLOCKS.register("vintage_television", VintageTelevisionBlock::new);
    public static final RegistryObject<Item> VINTAGE_TELEVISION_ITEM = ITEMS.register("vintage_television", () -> new BlockItem(VINTAGE_TELEVISION.get(), new Item.Properties()));

    // Create VHS tape block and item
    public static final RegistryObject<Block> VHS_TAPE_BLOCK = BLOCKS.register("vhs_tape", VHSTapeBlock::new);
    public static final RegistryObject<Item> VHS_TAPE = ITEMS.register("vhs_tape", () -> new BlockItem(VHS_TAPE_BLOCK.get(), new Item.Properties()));

    // Create broadcast portal block and item
    public static final RegistryObject<Block> BROADCAST_PORTAL_BLOCK = BLOCKS.register("broadcast_portal", BroadcastPortalBlock::new);
    public static final RegistryObject<Item> BROADCAST_PORTAL_ITEM = ITEMS.register("broadcast_portal", () -> new BlockItem(BROADCAST_PORTAL_BLOCK.get(), new Item.Properties()));

    // Creates a creative tab for Old Broadcast items
    public static final RegistryObject<CreativeModeTab> OLD_BROADCAST_TAB = CREATIVE_MODE_TABS.register("old_broadcast_tab", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.theoldbroadcast.old_broadcast_tab"))
        .withTabsBefore(CreativeModeTabs.COMBAT)
        .icon(() -> VINTAGE_TELEVISION_ITEM.get().getDefaultInstance())
        .displayItems((parameters, output) -> {
            output.accept(VINTAGE_TELEVISION_ITEM.get());
            output.accept(VHS_TAPE.get());
            output.accept(BROADCAST_PORTAL_ITEM.get());
        }).build());

    public Theoldbroadcast() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        StudioStructures.register(modEventBus);
        ModEntities.register(modEventBus);
        CHUNK_GENERATORS.register(modEventBus);
        BIOME_SOURCES.register(modEventBus);
        // Register custom sounds
        ModSounds.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Register dimension, chunk generator, and any additional setup
        // (e.g., OldBroadcastDimensions, OldBroadcastChunkGenerator, etc.)
        LOGGER.info("HELLO FROM THE STUDIO!");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

    }

    // Add items to existing creative tabs
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(VINTAGE_TELEVISION_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(VHS_TAPE);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("Welcome to the Evening News!");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("WE ARE ON AIR WITH REPORTER >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
