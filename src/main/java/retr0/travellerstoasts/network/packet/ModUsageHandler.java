package retr0.travellerstoasts.network.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

public class ModUsageHandler {
    public static final Identifier MOD_USAGE = new Identifier(MOD_ID, "mod_usage");

    @Environment(EnvType.CLIENT)
    private static boolean serverUsesMod = false;

    @Environment(EnvType.CLIENT)
    public static boolean isServerUsingMod() { return serverUsesMod; }

    public static void initialize() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(MOD_USAGE, (client, handler, buf, responseSender) ->
                serverUsesMod = true);

            ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> serverUsesMod = false);
        }

        // Whenever a client joins the server, notify them that the mod is installed--clients which have been notified
        // and have the mod installed will request to track inhabited time.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            sender.sendPacket(MOD_USAGE, PacketByteBufs.empty()));
    }
}
