package retr0.travellerstoasts;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retr0.travellerstoasts.event.ModUsageHandler;
import retr0.travellerstoasts.network.PacketRegistry;

public class TravellersToasts implements ModInitializer {
    public static final String MOD_ID = "travellerstoasts";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // TODO: BETTER HANDLING TOAST WHEN SWITCHING BIOMES WHILE ACTIVE.
        // TODO: BETTER WATER CHECKS FOR WHEN IN WATER THAT LOOKS LIKE RIVER BUT IS ANOTHER BIOME (E.G. BEACH).
        // TOPO: IMPLEMMENT COOLDOWN
        // TODO: IMPLMEMNENT CUSTOM BIOMES
        // TODO: IMPLEMENT SERVER TRACK CANCELLING

        PacketRegistry.registerC2SPackets();
        ModUsageHandler.register();
    }
}
