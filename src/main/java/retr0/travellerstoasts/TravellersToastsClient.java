package retr0.travellerstoasts;

import net.fabricmc.api.ClientModInitializer;
import retr0.travellerstoasts.config.TravellersToastsConfig;
import retr0.travellerstoasts.event.EventHandler;
import retr0.travellerstoasts.network.PacketRegistry;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

public class TravellersToastsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TravellersToastsConfig.init(MOD_ID, TravellersToastsConfig.class);

        PacketRegistry.registerS2CPackets();
        EventHandler.register();
    }
}
