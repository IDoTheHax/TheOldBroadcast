package net.idothehax.theoldbroadcast.network;

import net.idothehax.theoldbroadcast.client.SanityClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SanitySyncPacket {
    private final int sanity;

    public SanitySyncPacket(int sanity) {
        this.sanity = sanity;
    }

    public static void encode(SanitySyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.sanity);
    }

    public static SanitySyncPacket decode(FriendlyByteBuf buffer) {
        return new SanitySyncPacket(buffer.readInt());
    }

    public static void handle(SanitySyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // This code runs on the client side
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                SanityClientHandler.handleSanitySync(packet.sanity);
            });
        });
        context.setPacketHandled(true);
    }

    public int getSanity() {
        return sanity;
    }
}
