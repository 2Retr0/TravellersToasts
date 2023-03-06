package retr0.travellerstoasts.network;


import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import retr0.travellerstoasts.util.BiomeToastManager;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

public class TrackInhabitedTimeS2CPacket {
    public static final Identifier INHABITED_TIME_TRACK_RESPONSE_ID = new Identifier(MOD_ID, "inhabited_time_track_response");

    public static void send(boolean finishedQuery, ServerPlayerEntity player) {
        var buf = PacketByteBufs.create();
        buf.writeBoolean(finishedQuery);

        ServerPlayNetworking.send(player, INHABITED_TIME_TRACK_RESPONSE_ID, buf);
    }



    public static void receive(
        MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
    {
        boolean finishedQuery = buf.readBoolean();

        client.execute(() -> BiomeToastManager.INSTANCE.processServerResponse(finishedQuery));
    }
}
