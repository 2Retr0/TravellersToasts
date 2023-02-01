package retr0.travellerstoasts;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import retr0.travellerstoasts.config.Config;
import retr0.travellerstoasts.event.ModUsageHandler;
import retr0.travellerstoasts.network.PacketRegistry;
import retr0.travellerstoasts.network.TrackInhabitedTimeC2SPacket;
import retr0.travellerstoasts.network.ModUsageS2CPacket;
import retr0.travellerstoasts.util.BiomeToastManager;

import static retr0.travellerstoasts.TravellersToasts.*;

public class TravellersToastsClient implements ClientModInitializer {
    public static final BiomeToastManager BIOME_TOAST_MANAGER = new BiomeToastManager(MinecraftClient.getInstance());

    @Override
    public void onInitializeClient() {
        Config.init(MOD_ID, Config.class);

        PacketRegistry.registerS2CPackets();
        ModUsageHandler.register();
    }
}
