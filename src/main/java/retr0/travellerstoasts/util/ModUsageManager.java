package retr0.travellerstoasts.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import retr0.travellerstoasts.network.ModUsagePacket;
import retr0.travellerstoasts.TravellersToasts;

public class ModUsageManager {
    private static ModUsageManager instance;

    private boolean doesServerUseMod = false;

    public static void init() {
        if (instance != null) return;

        instance = new ModUsageManager();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
                instance.setServerModUsage(false);
                BiomeToastManager.getInstance().resetState(true);
            });

            ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
                if (!instance.doesServerUseMod())
                    TravellersToasts.LOGGER.warn("Mod is not present server-side! Inhabited time checking will be disabled!");
            });
        }

        // Whenever a client joins the server, notify them that the mod is installed--clients which have been notified
        // and have the mod installed will request to track inhabited time.
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> ModUsagePacket.send(sender));
    }

    public static ModUsageManager getInstance() {
        return instance;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean doesServerUseMod() {
        return doesServerUseMod;
    }

    public void setServerModUsage(boolean doesServerUseMod) {
        this.doesServerUseMod = doesServerUseMod;
    }

    private ModUsageManager() { }
}
