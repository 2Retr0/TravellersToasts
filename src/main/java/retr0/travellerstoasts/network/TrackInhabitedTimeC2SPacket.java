package retr0.travellerstoasts.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import retr0.travellerstoasts.TravellersToasts;
import retr0.travellerstoasts.config.Config;
import retr0.travellerstoasts.extension.ServerPlayerEntityExtension;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;
import static retr0.travellerstoasts.network.PacketRegistry.INHABITED_TIME_TRACK_REQUEST_ID;
import static retr0.travellerstoasts.network.PacketRegistry.INHABITED_TIME_TRACK_RESPONSE_ID;

public class TrackInhabitedTimeC2SPacket {
    public static PacketByteBuf create(int maxInhabitedTime) {
        var buf = PacketByteBufs.create();
        buf.writeInt(maxInhabitedTime);

        return buf;
    }

    public static void receive(
        MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf,
        PacketSender responseSender)
    {
        var maxInhabitedTimeTicks = buf.readInt();

        server.execute(() -> {
            TravellersToasts.LOGGER.info("Server: Received request to begin tracking " + player.getName().getString() + "!");
            ((ServerPlayerEntityExtension) player).beginTracking(maxInhabitedTimeTicks);
        });
    }
}
