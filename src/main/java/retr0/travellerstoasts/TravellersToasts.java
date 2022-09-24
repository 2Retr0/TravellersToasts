package retr0.travellerstoasts;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TravellersToasts implements ClientModInitializer {
    private record Pair<A> (A left, A right) { };

    public static final String MOD_ID = "travellerstoasts";
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Map<Identifier, Pair<Integer>> ICON_OFFSET_MAP = new HashMap<>();

    @Override
    public void onInitializeClient() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
        //     @Override
        //     public Identifier getFabricId() {
        //         return new Identifier(MOD_ID, "resources");
        //     }
        //
        //     @Override
        //     public void reload(ResourceManager manager) {
        //         // Clear Caches Here
        //
        //         for(Identifier id : manager.findResources("", identifier -> true).keySet()) {
        //             LOGGER.info(id.toString());
        //
        //             try(InputStream stream = manager.getResource(id).get().getInputStream()) {
        //             } catch(Exception e) {
        //                 LOGGER.error("Error occurred while loading resource json " + id.toString(), e);
        //             }
        //         }
        //     }
        // });
        //
        // DynamicRegistrySetupCallback.EVENT.register(registryManager -> {
        //     var biomes = registryManager.get(Registry.BIOME_KEY);
        //
        //     RegistryEntryAddedCallback.event(biomes).register((rawId, id, object) -> {
        //     });
        // });
        //
        // ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) -> {
        //     registry.register(new Identifier(MOD_ID, ));
        // });

    }
}
