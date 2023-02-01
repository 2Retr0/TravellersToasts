package retr0.travellerstoasts.util;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import static net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags.CAVES;
import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public final class BiomePrediction {
    /**
     * Gets the biome at a relative future position of {@code player} based on their velocity over {@code PEEK_SECONDS}.
     *
     * @param player The target player.
     * @return The {@link RegistryEntry} of the biome at the relative future position.
     */
    public static RegistryEntry<Biome> getFutureBiome(ClientPlayerEntity player) {
        final var FUTURE_TICKS = TICKS_PER_SECOND * 7; // Ticks ahead for which the future biome will be checked.

        // We only check the x and z-directions as underground biomes generally aren't that tall with respect to the
        // speeds a player falls at.
        var futureAbsolutePos = player.getVelocity().multiply(FUTURE_TICKS).multiply(1d, 0d, 1d);
        // Relative block position in PEEK_SECONDS seconds based on current velocity.
        var futureBlockPos = new BlockPos(player.getPos().add(futureAbsolutePos));

        return player.clientWorld.getBiome(futureBlockPos);
    }



    /**
     * Approximates if the 'features' of {@code biome} (e.g. Jungle trees in jungle biome) are currently visible to
     * {@code player}.
     *
     * @param biome  The target biome.
     * @param player the target player.
     * @return {@code true} if the approximation believes {@code biome}'s features to be visible; otherwise,
     *         {@code false}.
     */
    public static boolean areBiomeFeaturesVisible(RegistryEntry<Biome> biome, ClientPlayerEntity player) {
        var worldRegistryKey = player.clientWorld.getRegistryKey();
        // We use the current mood percentage to ensure that when mining, the player is not notified of biome changes
        // they would not be able to see.
        var moodPercentage = player.getMoodPercentage();

        if (worldRegistryKey == World.NETHER)
            // For the nether, the mood percentage tends to fluctuate more often and at higher values (e.g. >0.0008f).
            return moodPercentage < 0.001f;
        else if (worldRegistryKey == World.END)
            // For The End, we consider biomes' features to always be visible.
            return true;

        // For the overworld, the mood percentage tends to stay at 0f, except in denser environments (e.g. Dark Forest)
        // where it instead may go as high as 0.0004f.
        return moodPercentage < 0.0005f
            // For ocean biomes, the mood may be too high. We instead check if the sky is visible (i.e. underwater).
            || player.clientWorld.isSkyVisibleAllowingSea(player.getBlockPos())
            // For cave biomes, we consider their features to always be visible.
            || biome.isIn(CAVES);
    }
}
