package retr0.travellerstoasts.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import retr0.travellerstoasts.TravellersToasts;
import retr0.travellerstoasts.TravellersToastsClient;
import retr0.travellerstoasts.config.Config;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

public class TrackInhabitedTimeS2CPacket {
    public static final Identifier INHABITED_TIME_TRACK_RESPONSE_ID = new Identifier(MOD_ID, "inhabited_time_track_response");

    public static PacketByteBuf create(boolean finishedQuery) {
        var buf = PacketByteBufs.create();
        buf.writeBoolean(finishedQuery);

        return buf;
    }

    public static void receive(
        MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
    {
        boolean finishedQuery = buf.readBoolean();

        client.execute(() -> {
            if (Config.maxInhabitedTime <= 0f) return;

            if (finishedQuery) {
                TravellersToasts.LOGGER.info("Client: Received valid inhabited time response for player " + MinecraftClient.getInstance().player.getName().getString() + "!");
                TravellersToastsClient.BIOME_TOAST_MANAGER.tryShowToast();
            } else {
                TravellersToasts.LOGGER.info("Client: Received world interrupt status for player " + MinecraftClient.getInstance().player.getName().getString() + "!");
                TravellersToasts.LOGGER.info("Client: Resetting track status...");
                TravellersToastsClient.BIOME_TOAST_MANAGER.reset();
            }
        });
    }
}
