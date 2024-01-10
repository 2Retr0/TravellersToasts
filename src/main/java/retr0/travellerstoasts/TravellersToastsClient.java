package retr0.travellerstoasts;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
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
