package retr0.travellerstoasts.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import retr0.travellerstoasts.BiomeToast;
import retr0.travellerstoasts.config.Config;
import retr0.travellerstoasts.network.ClientPlayerEntityExtension;
import retr0.travellerstoasts.network.packet.InhabitedTimeTrackingHandler;
import retr0.travellerstoasts.network.packet.ModUsageHandler;
import retr0.travellerstoasts.util.BiomePrediction;
import retr0.travellerstoasts.util.CooldownHandler;

import static net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags.OCEAN;
import static net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags.RIVER;
import static net.minecraft.SharedConstants.TICKS_PER_MINUTE;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity
    extends AbstractClientPlayerEntity implements ClientPlayerEntityExtension
{
    @Unique private final CooldownHandler<RegistryEntry<Biome>> biomeCooldownHandler = new CooldownHandler<>(
        () -> (long) (60000 * Config.toastCooldownTime));

    @Unique private RegistryEntry<Biome> previousBiome;
    @Unique private RegistryEntry<Biome> currentBiome;

    @Unique private int ticksExploringBiome = 0;

    @Override
    public void tryShowToast() {
        if (ticksExploringBiome == HOLD_TICKS) {
            BiomeToast.show(toastManager, currentBiome);
            biomeCooldownHandler.update(previousBiome);
            previousBiome = currentBiome;
            ticksExploringBiome = 0;
        }
    }



    @Override
    public void resetCooldownCache() {
        biomeCooldownHandler.reset();
    }



    /**
     * Handles showing new biomes for {@link BiomeToast} when the player <i>begins exploring</i> a new biome.
     */
    // We define a player to "begin exploring" a new biome as follows:
    //   * The player has travelled to a new biome different from its previous one.
    //
    //   * The biome at a future position (factoring the player's velocity) is the same as the new biome
    //     (this prevents spamming toasts when travelling quickly for the most part).
    //
    //   * The player is actively moving forwards or is falling faster than 1 blocks per tick
    //     (the player should be actively moving towards or purposefully falling towards the new biome).
    //
    //   * There is no boss bar present at the player's current location.
    //
    //   * All previous conditions are met for at least HOLD_TICKS ticks.
    //
    //   * The biome has not been explored (i.e. biome toast shown and has left biome) for more than 30 minutes.
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        currentBiome = world.getBiome(getBlockPos());

        // Do nothing if the current biome is still on cooldown.
        if (!biomeCooldownHandler.check(currentBiome)) return;

        if (ModUsageHandler.isServerUsingMod()) {
            // If the current server has the mod loaded, send it a request to begin tracking the current chunk's
            // inhabited time.
            if (ticksExploringBiome == HOLD_TICKS) {
                var maxInhabitedTime = (int) (TICKS_PER_MINUTE * Config.maxInhabitedTime);

                InhabitedTimeTrackingHandler.sendTrackRequest(maxInhabitedTime, this::tryShowToast);
            }
        } else {
            // If the current server does not have the mod loaded, we immediately try to show the biome toast
            // (i.e. inhabited time will not be considered).
            tryShowToast();
        }

        var instance = ((ClientPlayerEntity) (Object) this);
        var isOceanBiome = currentBiome.isIn(RIVER) || currentBiome.isIn(OCEAN);

        // Don't decrement or increment ticksEnteringBiome if the player is not moving or if the player is underwater
        // and not swimming--this allows for staggered movement (e.g. jumping) to still result in toasts being shown!
        if (!((instance.input.hasForwardMovement() && !this.horizontalCollision) || getVelocity().getY() <= -1d)
            || (isOceanBiome && isSubmergedInWater() && !isInSwimmingPose()))
        {
            return;
        }


        if (currentBiome != previousBiome
            && BiomePrediction.getFutureBiome(instance) == currentBiome
            && BiomePrediction.areBiomeFeaturesVisible(currentBiome, instance)
            && bossBars.getBossBars().isEmpty()
            && !(isOceanBiome && !isSubmergedInWater()))
        {
            ++ticksExploringBiome;
        } else
            --ticksExploringBiome;

        ticksExploringBiome = MathHelper.clamp(ticksExploringBiome, 0, HOLD_TICKS);
    }

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }
}
