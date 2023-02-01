package retr0.travellerstoasts;

import net.fabricmc.api.ClientModInitializer;
import retr0.travellerstoasts.config.Config;
import retr0.travellerstoasts.network.packet.InhabitedTimeTrackingHandler;
import retr0.travellerstoasts.network.packet.ModUsageHandler;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

public class TravellersToastsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Config.init(MOD_ID, Config.class);

        ModUsageHandler.initialize();
        InhabitedTimeTrackingHandler.initialize();
    }
}
