package retr0.travellerstoasts;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class TravellersToasts implements ModInitializer {
    public static final String MOD_ID = "travellerstoasts";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier MONITOR_INHABITED_TIME = new Identifier(MOD_ID, "monitor_inhabited_time");
    public static final Identifier MOD_LOADED = new Identifier(MOD_ID, "server_mod_loaded");

    public interface IMixinServerPlayerEntity {
        void beginTracking();
    }

    @Override
    public void onInitialize() {
        // TODO: KEEP TRACK OF BIOME LAST VISITED TIME
        // TODO: ADD CONFIG
        // TODO: TRACK BOSS_BAR_MANAGER ON SERVER-SIDE

        // Whenever a client joins the server, notify them that the mod is installed--clients which have been notified
        // will begin requesting
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            LOGGER.info("Sent " + MOD_LOADED.getPath() + " notification to " + handler.connection.getAddress().toString());
            sender.sendPacket(MOD_LOADED, PacketByteBufs.empty());
        });

        ServerPlayNetworking.registerGlobalReceiver(MONITOR_INHABITED_TIME,
            (server, player, handler, buf, responseSender) -> {
                LOGGER.info("Begun tracking " + player.getDisplayName().toString());
                ((IMixinServerPlayerEntity) player).beginTracking();
            });
    }
}
