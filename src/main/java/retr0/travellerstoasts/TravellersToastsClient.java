package retr0.travellerstoasts;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

import static retr0.travellerstoasts.TravellersToasts.*;

public class TravellersToastsClient implements ClientModInitializer {
    public static boolean doesServerHaveModLoaded = false;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(MOD_LOADED,
            (client, handler, buf, responseSender) -> doesServerHaveModLoaded = true);

        ClientPlayConnectionEvents.DISCONNECT.register(
            (handler, client) -> doesServerHaveModLoaded = false);

        ClientPlayNetworking.registerGlobalReceiver(SYNC_ID,
            (client, handler, buf, responseSender) -> {
                // Read packet data on the event loop
                var inhabitedTime = buf.readLong();
                var blockPos = buf.readBlockPos();

                client.execute(() -> {
                    // Everything in this lambda is run on the render thread
                    TravellersToasts.blockPos = blockPos;
                    TravellersToasts.inhabitedTime = inhabitedTime;
                });
            });
    }
}
