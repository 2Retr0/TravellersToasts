package retr0.travellerstoasts.network.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import retr0.travellerstoasts.config.Config;
import retr0.travellerstoasts.network.ServerPlayerEntityExtension;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

public class InhabitedTimeTrackingHandler {
    public static final Identifier TRACK_INHABITED_TIME = new Identifier(MOD_ID, "track_inhabited_time");

    private static boolean awaitingServerResponse = false;
    private static Runnable currentCallback;

    public static void sendTrackRequest(int maxInhabitedTime, Runnable callback) {
        if (awaitingServerResponse || !(Config.maxInhabitedTime > 0f)) return;

        var buffer = PacketByteBufs.create(); buffer.writeInt(maxInhabitedTime);
        ClientPlayNetworking.send(TRACK_INHABITED_TIME, buffer);

        currentCallback = callback;
        awaitingServerResponse = true;
    }

    public static void sendResponse(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, TRACK_INHABITED_TIME, PacketByteBufs.empty());
    }

    public static void initialize() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(TRACK_INHABITED_TIME, (client, handler, buf, responseSender) ->
                client.execute(() -> {
                    if (Config.maxInhabitedTime > 0f) currentCallback.run();

                    awaitingServerResponse = false;
                }));
        }

        // Whenever the server receives a request on the TRACK_INHABITED_TIME, begin tracking the player's current
        // chunk's inhabited time.
        ServerPlayNetworking.registerGlobalReceiver(TRACK_INHABITED_TIME,
            (server, player, handler, buf, responseSender) -> {
                var maxInhabitedTimeTicks = buf.readInt();

                server.execute(() -> ((ServerPlayerEntityExtension) player).beginTracking(maxInhabitedTimeTicks));
            });
    }
}
