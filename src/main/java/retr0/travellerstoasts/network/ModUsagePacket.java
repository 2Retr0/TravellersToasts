package retr0.travellerstoasts.network;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import retr0.travellerstoasts.util.ModUsageManager;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

public class ModUsagePacket {
    public static final Identifier NOTIFY_MOD_USAGE_ID = new Identifier(MOD_ID, "notify_mod_usage");

    public static void send(PacketSender sender) {
        sender.sendPacket(NOTIFY_MOD_USAGE_ID, PacketByteBufs.empty());
    }

    public static void receive(
            MinecraftServer server, ServerLoginNetworkHandler handler, boolean understood, PacketByteBuf buf, ServerLoginNetworking.LoginSynchronizer synchronizer, PacketSender responseSender)
    {
    }

    @Environment(EnvType.CLIENT)
    public static CompletableFuture<@Nullable PacketByteBuf> receive(
            MinecraftClient client, ClientLoginNetworkHandler handler, PacketByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> listenerAdder)
    {
        ModUsageManager.getInstance().setServerModUsage(true);
        return CompletableFuture.completedFuture(null);
    }
}
