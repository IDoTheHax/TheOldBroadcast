package net.idothehax.theoldbroadcast.world.structure;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.idothehax.theoldbroadcast.Theoldbroadcast;

public class StudioStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Theoldbroadcast.MODID);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, Theoldbroadcast.MODID);

    public static final RegistryObject<StructureType<StudioStructure>> STUDIO_TYPE =
            STRUCTURE_TYPES.register("studio", () -> () -> StudioStructure.CODEC);

    public static final RegistryObject<StructurePieceType> STUDIO_ROOM_PIECE =
            STRUCTURE_PIECES.register("studio_room", () -> (context, tag) -> new StudioStructure.StudioRoomPiece(tag));

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
        STRUCTURE_PIECES.register(eventBus);
    }
}
