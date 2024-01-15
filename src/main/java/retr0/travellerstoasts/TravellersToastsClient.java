package retr0.travellerstoasts;

import net.fabricmc.api.ClientModInitializer;
import retr0.carrotconfig.config.CarrotConfig;
import retr0.travellerstoasts.config.TravellersToastsConfig;
import retr0.travellerstoasts.network.PacketRegistry;
import retr0.travellerstoasts.util.BiomeToastManager;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

public class TravellersToastsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PacketRegistry.registerS2CPackets();
        CarrotConfig.init(MOD_ID, TravellersToastsConfig.class);
        BiomeToastManager.init();
    }
}
