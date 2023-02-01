package retr0.travellerstoasts.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import retr0.travellerstoasts.TravellersToasts;
import retr0.travellerstoasts.config.Config;
import retr0.travellerstoasts.event.ModUsageHandler;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

public class ModUsageS2CPacket {
    public static void receive(
        MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
    {
        client.execute(() -> {
            TravellersToasts.LOGGER.info("Client: Received notification that server uses TravellersToasts!");
            ModUsageHandler.serverUsesMod = true;
        });
    }
}
