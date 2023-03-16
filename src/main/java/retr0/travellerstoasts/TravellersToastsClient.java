package retr0.travellerstoasts;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
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

        // Discover available icons
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {
                @Override
                public void reload(ResourceManager manager) {
                    var iconMappings = manager.findResources("textures/gui/icons", identifier ->
                        identifier.getNamespace().equals(MOD_ID) && identifier.getPath().endsWith(".png"));

                    // Register each biome icon available into the texture manager.
                    iconMappings.forEach((identifier, resource) ->
                        MinecraftClient.getInstance().getTextureManager().bindTexture(identifier));
                }

                @Override
                public Identifier getFabricId() {
                    return new Identifier(MOD_ID, "resource_listener");
                }
            });
    }
}
