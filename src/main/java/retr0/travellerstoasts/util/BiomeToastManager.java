package retr0.travellerstoasts.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import retr0.travellerstoasts.BiomeToast;
import retr0.travellerstoasts.config.TravellersToastsConfig;
import retr0.travellerstoasts.event.EventHandler;
import retr0.travellerstoasts.mixin.AccessorBossBarHud;
import retr0.travellerstoasts.network.TrackInhabitedTimeC2SPacket;

import static net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags.OCEAN;
import static net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags.RIVER;

@Environment(EnvType.CLIENT)
public class BiomeToastManager {
    public static final BiomeToastManager INSTANCE = new BiomeToastManager(MinecraftClient.getInstance());
    // Required ticks for the "entering biome" condition.
    private final static int HOLD_TICKS = SharedConstants.TICKS_PER_SECOND * 3;

    private final CooldownHandler<RegistryEntry<Biome>> biomeCooldownHandler =
            new CooldownHandler<>(() -> (long) (TravellersToastsConfig.toastCooldownTime * 60000));
    private final MinecraftClient client;

    private boolean awaitingServerResponse = false;
    private int ticksExploringBiome = 0;
    private Vec3d previousPos = Vec3d.ZERO;
    private RegistryEntry<Biome> previousBiome;
    private RegistryEntry<Biome> currentBiome;

    /**
     * Attempts to show a biome toast if the player has been 'exploring' a biome for a valid amount of time.
     * @param sendServerRequest If {@code true}, additionally requests the server to check for a valid inhabited time
     *                          before showing the toast.
     */
    private void tryShowingToast(boolean requestServerCheck) {
        if (ticksExploringBiome != HOLD_TICKS) return;

        // If the current server has the mod loaded, we do an additional check by requesting the current chunk's
        // inhabited time to be tracked (waiting for a response).
        if (requestServerCheck) {
            if (!awaitingServerResponse) {
                TrackInhabitedTimeC2SPacket.send(TravellersToastsConfig.maxInhabitedTime);
                awaitingServerResponse = true;
            }
            return;
        }

        BiomeToast.show(client.getToastManager(), currentBiome);
        biomeCooldownHandler.refresh(previousBiome);
        previousBiome = currentBiome;
        resetState(false);
    }



    /**
     * Resets the state of the {@link BiomeToastManager} (not resetting cooldowns by default).
     * @param fullReset If {@code true}, all fields will be reset.
     */
    public void resetState(boolean fullReset) {
        if (fullReset) {
            biomeCooldownHandler.reset();
            previousBiome = null;
        }
        ticksExploringBiome = 0;
        awaitingServerResponse = false;
    }



    /**
     * Runs various checks on the player's 'state' (e.g. velocity, pose, etc.) to estimate whether it is appropriate
     * for a biome toast to show.
     * @implNote Object state change—previous player position is updated within method.
     */
    private boolean doesPlayerHaveValidState(ClientPlayerEntity player) {
        var currentPos = player.getPos();
        var isOceanBiome = (currentBiome.isIn(RIVER) || currentBiome.isIn(OCEAN))
                && !currentBiome.isIn(ConventionalBiomeTags.AQUATIC_ICY);

        // Don't decrement or increment ticksEnteringBiome if the player is not moving.
        var movementChecks = player.input.hasForwardMovement() && currentPos.squaredDistanceTo(previousPos) > 0.004;
        var bossBarChecks = ((AccessorBossBarHud) client.inGameHud.getBossBarHud()).getBossBars().isEmpty();
        // Permit only swimming in ocean biomes to count as 'exploration'; otherwise, allow both swimming and walking.
        var oceanBiomeChecks = (isOceanBiome && player.isSubmergedInWater() && player.isInSwimmingPose())
                || (!isOceanBiome && (!player.isSubmergedInWater() || player.isInSwimmingPose()));

        previousPos = currentPos;

        return movementChecks && bossBarChecks && oceanBiomeChecks;
    }



    /**
     * Runs various checks on the player's location (or predicted future location) to estimate whether it is appropriate
     * for a biome toast to show.
     */
    private boolean isPlayerInValidLocation(ClientPlayerEntity player) {
        return BiomePredictionUtil.getFutureBiome(player).equals(currentBiome) &&
               BiomePredictionUtil.areBiomeFeaturesVisible(currentBiome, player);
    }



    /**
     * Updates the time tracked for 'exploring' a biome. Should be called every game tick.
     */
    public void tick(ClientPlayerEntity player) {
        currentBiome = player.world.getBiome(player.getBlockPos());
        // Do nothing if the current biome is still on cooldown.
        if (currentBiome.equals(previousBiome) || !biomeCooldownHandler.hasCooled(currentBiome)) return;

        tryShowingToast(EventHandler.getServerModUsage() && TravellersToastsConfig.maxInhabitedTime > 0f);

        ticksExploringBiome += doesPlayerHaveValidState(player) && isPlayerInValidLocation(player) ? 1 : -1;
        ticksExploringBiome = MathHelper.clamp(ticksExploringBiome, 0, HOLD_TICKS);
    }



    public void processServerResponse(boolean finishedQuery) {
        awaitingServerResponse = false;
        if (finishedQuery)
            tryShowingToast(false);
        else
            resetState(false);
    }

    private BiomeToastManager(MinecraftClient client) {
        this.client = client;
    }
}
