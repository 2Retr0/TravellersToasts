package retr0.travellerstoasts.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import retr0.travellerstoasts.BiomeToast;
import retr0.travellerstoasts.TravellersToasts;
import retr0.travellerstoasts.TravellersToastsClient;

import java.util.Objects;

import static net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags.CAVES;
import static retr0.travellerstoasts.TravellersToasts.*;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity {
    @Unique private final int TICKS_PER_SECOND = 20;

    @Unique private final ClientPlayerEntity instance = ((ClientPlayerEntity) (Object) this);
    @Unique private RegistryEntry<Biome> previousBiome = instance.clientWorld.getBiome(instance.getBlockPos());
    @Unique private int ticksEnteringBiome = 0;
    @Unique private boolean awaitingServerResponse = false;

    /**
     * Gets the biome at a relative future position considering the given {@code velocity} over {@code PEEK_SECONDS}.
     * @param clientWorld The world for which the biome will be checked.
     * @param currentPos The position to base the future biome from.
     * @param velocity Velocity in blocks/tick.
     * @return The biome at the relative future position.
     */
    @Unique
    private RegistryEntry<Biome> getFutureBiome(ClientWorld clientWorld, Vec3d currentPos, Vec3d velocity) {
        final var FUTURE_TICKS = TICKS_PER_SECOND * 8; // Ticks ahead for which the future biome will be checked.

        // We only check the x and z-directions as underground biomes generally aren't that tall with respect to the
        // speeds a player falls at.
        var futureAbsolutePos = velocity.multiply(FUTURE_TICKS).multiply(1d, 0d, 1d);
        // Relative block position in PEEK_SECONDS seconds based on current velocity.
        var futureBlockPos = new BlockPos(currentPos.add(futureAbsolutePos));

        return clientWorld.getBiome(futureBlockPos);
    }



    /**
     *
     * @param biome
     * @param clientWorld
     * @param blockPos
     * @return
     */
    @Unique
    private boolean areBiomeFeaturesVisible(RegistryEntry<Biome> biome, ClientWorld clientWorld, BlockPos blockPos) {
        // We use the current mood percentage to ensure that when mining, the player is not notified of biome changes
        // they would not be able to see.
        if (clientWorld.getRegistryKey() == World.NETHER)
            // For the nether, the mood percentage tends to fluctuate more often and at higher values (e.g. >0.0008f).
            return instance.getMoodPercentage() < 0.001f;
        else if (clientWorld.getRegistryKey() == World.END)
            // We consider the biome features of The End biomes to always be visible.
            return true;

        // For the overworld, the mood percentage tends to stay at 0f, except in denser environments (e.g. Dark Forest)
        // where it instead may go as high as 0.0004f.
        return instance.getMoodPercentage() < 0.0005f
            || clientWorld.isSkyVisibleAllowingSea(blockPos) // For ocean biomes, where the mood may be too high.
            || biome.isIn(CAVES); // For cave biomes.
    }



    /**
     * Handles showing new biomes for {@link BiomeToast} when the player <i>begins exploring</i> a new biome.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        final var HOLD_TICKS = TICKS_PER_SECOND * 3; // Ticks for which the isEnteringBiome condition must be met.

        var clientWorld = instance.clientWorld;
        var blockPos = instance.getBlockPos();
        var velocity = instance.getVelocity();

        var currentBiome = clientWorld.getBiome(blockPos);
        var futureBiome = getFutureBiome(clientWorld, instance.getPos(), velocity);

        if (ticksEnteringBiome == HOLD_TICKS) {
            // if server has mod, ask to track inhabitedTime **ONCE**, then wait until called back.
            if (TravellersToastsClient.doesServerHaveModLoaded) {
                if (!awaitingServerResponse) {
                    ClientPlayNetworking.send(MONITOR_INHABITED_TIME, PacketByteBufs.empty());
                    ClientPlayNetworking.unregisterReceiver(MONITOR_INHABITED_TIME);
                    ClientPlayNetworking.registerReceiver(MONITOR_INHABITED_TIME,
                        (client, handler, buf, responseSender) -> {
                            TravellersToasts.LOGGER.info("Received valid inhabited time from server");
                            TravellersToasts.LOGGER.info("ticksEnteringBiome == HOLD_TICKS: " + (ticksEnteringBiome == HOLD_TICKS));
                            TravellersToasts.LOGGER.info("ticksEnteringBiome: " + ticksEnteringBiome);
                            if (ticksEnteringBiome == HOLD_TICKS) {
                                BiomeToast.show(MinecraftClient.getInstance().getToastManager(), currentBiome);
                                previousBiome = currentBiome;
                                ticksEnteringBiome = 0;
                            }
                            awaitingServerResponse = false;
                        });
                    awaitingServerResponse = true;
                }
            } else {
                BiomeToast.show(MinecraftClient.getInstance().getToastManager(), currentBiome);
                previousBiome = currentBiome;
                ticksEnteringBiome = 0;
            }
        }

        /* We define a player to "begin exploring" a new biome as follows:
         *   * The player has travelled to a new biome different from its previous one.
         *
         *   * The biome at a future position considering the player's velocity is the same as the new biome
         *     (this prevents spamming toasts when travelling quickly for the most part).
         *
         *   * The player is actively moving forwards or is falling faster than 1 blocks per tick
         *     (the player should be actively moving towards or purposefully falling towards the new biome).
         *
         *   * All previous conditions are met for at least HOLD_TICKS ticks.
         */
        // Don't decrement or increment counter if the player is not moving--this allows for staggered movement
        // (e.g. jumping) to still result in toasts being shown!
        if (!(instance.input.hasForwardMovement() || velocity.getY() <= -1d)) return;

        if (currentBiome != previousBiome
            && currentBiome == futureBiome
            && areBiomeFeaturesVisible(currentBiome, clientWorld, blockPos))
        {
            ticksEnteringBiome = Math.min(++ticksEnteringBiome, HOLD_TICKS);
        } else
            ticksEnteringBiome = Math.max(--ticksEnteringBiome, 0);
    }
}
