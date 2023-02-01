package retr0.travellerstoasts.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import retr0.travellerstoasts.TravellersToasts;
import retr0.travellerstoasts.event.ModUsageHandler;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

public class ModUsageS2CPacket {
    public static final Identifier NOTIFY_MOD_USAGE_ID = new Identifier(MOD_ID, "notify_mod_usage");

    public static void receive(
        MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
    {
        client.execute(() -> {
            TravellersToasts.LOGGER.info("Client: Received notification that server uses TravellersToasts!");
            ModUsageHandler.serverUsesMod = true;
        });
    }
}
