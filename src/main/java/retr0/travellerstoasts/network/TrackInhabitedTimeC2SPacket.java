package retr0.travellerstoasts.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.SharedConstants;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import retr0.travellerstoasts.extension.ExtensionServerPlayerEntity;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

public class TrackInhabitedTimeC2SPacket {
    public static final Identifier INHABITED_TIME_TRACK_REQUEST_ID = new Identifier(MOD_ID, "inhabited_time_track_request");

    public static void send(float maxInhabitedTimeM) {
        var buf = PacketByteBufs.create();
        buf.writeInt((int) (SharedConstants.TICKS_PER_MINUTE * maxInhabitedTimeM));

        ClientPlayNetworking.send(INHABITED_TIME_TRACK_REQUEST_ID, buf);
    }



    public static void receive(
        MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf,
        PacketSender responseSender)
    {
        var maxInhabitedTimeTicks = buf.readInt();

        server.execute(() -> {
            if (player == null) return;

            ((ExtensionServerPlayerEntity) player).travellersToasts$beginTracking(maxInhabitedTimeTicks);
        });
    }
}
