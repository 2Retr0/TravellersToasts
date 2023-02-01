package retr0.travellerstoasts.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import retr0.travellerstoasts.TravellersToasts;
import retr0.travellerstoasts.TravellersToastsClient;
import retr0.travellerstoasts.extension.ServerPlayerEntityExtension;

import static retr0.travellerstoasts.network.PacketRegistry.NOTIFY_MOD_USAGE_ID;

public class ModUsageHandler {
    public static boolean serverUsesMod = false;

    public static boolean isServerUsingMod() { return serverUsesMod; }



    public static void register() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
                TravellersToasts.LOGGER.info("Client: Reset TravellersToasts server mod usage info!");
                serverUsesMod = false;
                TravellersToastsClient.BIOME_TOAST_MANAGER.resetCooldownCache();
                TravellersToastsClient.BIOME_TOAST_MANAGER.reset();
            });
        }

        // Whenever a client joins the server, notify them that the mod is installed--clients which have been notified
        // and have the mod installed will request to track inhabited time.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            TravellersToasts.LOGGER.info("Server: Notified client that TravellersToasts is installed!");
            sender.sendPacket(NOTIFY_MOD_USAGE_ID, PacketByteBufs.empty());
        });

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(((player, origin, destination) -> {
            TravellersToasts.LOGGER.info("Server: Sent world change interrupt notice!");
            ((ServerPlayerEntityExtension) player).stopTracking(false);
        }));
    }
}
