package retr0.travellerstoasts.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static retr0.travellerstoasts.TravellersToasts.MOD_LOADED;

@Environment(EnvType.CLIENT)
public class ServerPollHandler {
    private final Identifier channel;
    private boolean awaitingResponse = false;

    public ServerPollHandler(Identifier channel) {
        this.channel = channel;
    }

    public void poll(PacketByteBuf buffer, Consumer<PacketByteBuf> callback) {
        if (awaitingResponse) return;

        awaitingResponse = true;
        ClientPlayNetworking.unregisterGlobalReceiver(channel);
        ClientPlayNetworking.registerGlobalReceiver(channel,
            (client, handler, buf, responseSender) -> {
                callback.accept(buf);
                awaitingResponse = false;
            });

        ClientPlayNetworking.send(channel, buffer);
    }
}
