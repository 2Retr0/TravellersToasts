package retr0.travellerstoasts;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TravellersToasts implements ModInitializer {
    private record Pair<A> (A left, A right) { };

    public static final String MOD_ID = "travellerstoasts";
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Map<Identifier, Pair<Integer>> ICON_OFFSET_MAP = new HashMap<>();

    public static long inhabitedTime = -1L;
    public static BlockPos blockPos = new BlockPos(0, 0, 0);

    public static final Identifier SYNC_ID = new Identifier(MOD_ID, "inhabited_time_sync");
    public static final Identifier MOD_LOADED = new Identifier(MOD_ID, "server_mod_loaded");

    @Override
    public void onInitialize() {
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


        // all on server side:
        //    * when a player joins the server, contact them letting them know the mod is installed
        //    * only track player which ahs contacted server (i.e. they know the server has mod and would like to monitor inhabited time)
        //    * when the proper conditions are met, send a packet back to the client to show the message!

        // all on client side:
        //    * after connecting to the server

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            LOGGER.info("Sent " + MOD_LOADED.getPath() + " notification to " + handler.connection.getAddress().toString());
            sender.sendPacket(MOD_LOADED, PacketByteBufs.empty());
        });

        ServerPlayNetworking.registerGlobalReceiver(SYNC_ID,
            (server, player, handler, buf, responseSender) -> {
                server.execute(() -> {
                    var blockPos = player.getBlockPos();
                    var x = ChunkSectionPos.getSectionCoord(blockPos.getX());
                    var z = ChunkSectionPos.getSectionCoord(blockPos.getZ());
                    var worldChunk = player.getWorld().getChunkManager().getWorldChunk(x, z, false);
                    var inhabitedTime = -1L;

                    if (worldChunk != null)
                        inhabitedTime = worldChunk.getInhabitedTime();

                    var sendBuf = PacketByteBufs.create();
                    sendBuf.writeLong(inhabitedTime);
                    sendBuf.writeBlockPos(blockPos);

                    // ServerPlayNetworking.send(player, SYNC_ID2, sendBuf);
                    responseSender.sendPacket(SYNC_ID, sendBuf);
                });
            });
    }
}
