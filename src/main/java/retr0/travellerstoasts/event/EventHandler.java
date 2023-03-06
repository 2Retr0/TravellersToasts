package retr0.travellerstoasts.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import retr0.carrotconfig.config.CarrotConfig;
import retr0.travellerstoasts.config.TravellersToastsConfig;
import retr0.travellerstoasts.extension.ExtensionServerPlayerEntity;
import retr0.travellerstoasts.network.ModUsageS2CPacket;
import retr0.travellerstoasts.network.TrackInhabitedTimeC2SPacket;
import retr0.travellerstoasts.util.BiomeToastManager;

/**
 * Handles client events concerning resetting the {@link BiomeToastManager} instance's state (e.g. on dimension change),
 * as well as handling server events concerning notifying clients that it has the mod installed.
 */
public class EventHandler {
    public static boolean serverModUsage = false;

    public static boolean getServerModUsage() {
        return serverModUsage;
    }

    public static void setServerModUsage(boolean serverModUsage) {
        EventHandler.serverModUsage = serverModUsage;
    }

    public static void register() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
                serverModUsage = false;
                BiomeToastManager.INSTANCE.resetState(true);
            });

            CarrotConfig.ConfigSavedCallback.EVENT.register(configClass -> {
                if (!configClass.isAssignableFrom(TravellersToastsConfig.class)) return;

                BiomeToastManager.INSTANCE.resetState(false);
                TrackInhabitedTimeC2SPacket.send(-1); // Stop server from tracking player.
            });
        }

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(((player, origin, destination) ->
            ((ExtensionServerPlayerEntity) player).stopTracking(false)));

        // Whenever a client joins the server, notify them that the mod is installed--clients which have been notified
        // and have the mod installed will request to track inhabited time.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ModUsageS2CPacket.send(handler.player));
    }

    private EventHandler() { }
}
