package retr0.travellerstoasts.util;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import retr0.travellerstoasts.BiomeToast;
import retr0.travellerstoasts.TravellersToasts;
import retr0.travellerstoasts.config.Config;
import retr0.travellerstoasts.event.ModUsageHandler;
import retr0.travellerstoasts.mixin.AccessorBossBarHud;
import retr0.travellerstoasts.network.TrackInhabitedTimeC2SPacket;

import static net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags.OCEAN;
import static net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags.RIVER;
import static net.minecraft.SharedConstants.TICKS_PER_MINUTE;
import static net.minecraft.SharedConstants.TICKS_PER_SECOND;
import static retr0.travellerstoasts.network.TrackInhabitedTimeC2SPacket.INHABITED_TIME_TRACK_REQUEST_ID;

public class BiomeToastManager {
    private final static int HOLD_TICKS = TICKS_PER_SECOND * 3; // Required ticks for the "entering biome" condition.

    private final CooldownHandler<RegistryEntry<Biome>> biomeCooldownHandler = new CooldownHandler<>(
        () -> (long) (60000 * Config.toastCooldownTime));

    // private final ClientPlayerEntity player;
    private final MinecraftClient client;

    private int ticksExploringBiome = 0;

    private RegistryEntry<Biome> previousBiome;
    private RegistryEntry<Biome> currentBiome;

    private Vec3d previousPos = Vec3d.ZERO;

    private boolean awaitingServerResponse = false;

    public BiomeToastManager(MinecraftClient client) {
        this.client = client;
    }

    public void tryShowToast() {
        if (ticksExploringBiome == HOLD_TICKS) {
            BiomeToast.show(client.getToastManager(), currentBiome);
            biomeCooldownHandler.update(previousBiome);
            previousBiome = currentBiome;
            ticksExploringBiome = 0;
        }
        awaitingServerResponse = false;
    }

    public void reset() {
        ticksExploringBiome = 0;
        awaitingServerResponse = false;
    }



    public void resetCooldownCache() { biomeCooldownHandler.reset(); }


    public void update(ClientPlayerEntity player) {
        currentBiome = player.world.getBiome(player.getBlockPos());

        // Do nothing if the current biome is still on cooldown.
        if (currentBiome.equals(previousBiome) || !biomeCooldownHandler.check(currentBiome)) {
            // TravellersToasts.LOGGER.info("---Exited update(): " + currentBiome.getKey().get().getValue() + " is on cooldown!");
            return;
        }

        if (ModUsageHandler.isServerUsingMod() && Config.maxInhabitedTime > 0f) {
            // If the current server has the mod loaded, send it a request to begin tracking the current chunk's
            // inhabited time.
            if (!awaitingServerResponse && ticksExploringBiome == HOLD_TICKS) {
                var maxInhabitedTime = (int) (TICKS_PER_MINUTE * Config.maxInhabitedTime);

                TravellersToasts.LOGGER.info("Client: Sent request to begin tracking " + MinecraftClient.getInstance().player.getName().getString() + "!");
                ClientPlayNetworking.send(INHABITED_TIME_TRACK_REQUEST_ID, TrackInhabitedTimeC2SPacket.create(maxInhabitedTime));

                awaitingServerResponse = true;
            }
        } else {
            TravellersToasts.LOGGER.info("---Skipped packet send: Server does not have the mod loaded!");
            // If the current server does not have the mod loaded, we immediately try to show the biome toast
            // (i.e. inhabited time will not be considered).
            tryShowToast();
        }

        var isOceanBiome = currentBiome.isIn(RIVER) || currentBiome.isIn(OCEAN);
        var currentPos = player.getPos();
        // Don't decrement or increment ticksEnteringBiome if the player is not moving or if the player is underwater
        // and not swimming--this allows for staggered movement (e.g. jumping) to still result in toasts being shown!
        if (!(player.input.hasForwardMovement() && currentPos.squaredDistanceTo(previousPos) > 0.004)
            || (isOceanBiome && player.isSubmergedInWater() && !player.isInSwimmingPose()))
        {
            TravellersToasts.LOGGER.info("------Exited update(): Failed movement check.");
            return;
        }

        if (BiomePrediction.getFutureBiome(player).equals(currentBiome)
            && BiomePrediction.areBiomeFeaturesVisible(currentBiome, player)
            && ((AccessorBossBarHud) client.inGameHud.getBossBarHud()).getBossBars().isEmpty()
            && !(isOceanBiome && !player.isSubmergedInWater()))
        {
            ++ticksExploringBiome;
            TravellersToasts.LOGGER.info("ticks (r: " + HOLD_TICKS + "; " + "p: " + awaitingServerResponse + "): " + ticksExploringBiome);
        } else {
            --ticksExploringBiome;
            TravellersToasts.LOGGER.info("ticks (f: " + BiomePrediction.getFutureBiome(player).equals(currentBiome)
                + "; v: " + player.getMoodPercentage() + ":" + BiomePrediction.areBiomeFeaturesVisible(currentBiome, player)
                + "; b: " + ((AccessorBossBarHud) client.inGameHud.getBossBarHud()).getBossBars().isEmpty()
                + "; o: " + !(isOceanBiome && !player.isSubmergedInWater()) + "): " + ticksExploringBiome);
        }

        ticksExploringBiome = MathHelper.clamp(ticksExploringBiome, 0, HOLD_TICKS);
        previousPos = currentPos;
    }
}
