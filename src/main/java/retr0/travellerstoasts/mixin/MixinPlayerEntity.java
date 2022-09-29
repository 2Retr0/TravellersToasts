package retr0.travellerstoasts.mixin;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags.CAVES;
import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity {
    @Unique private final int TICKS_PER_SECOND = 20;

    @Unique private final PlayerEntity instance = ((PlayerEntity) (Object) this);
    @Unique private RegistryEntry<Biome> previousBiome = instance.world.getBiome(instance.getBlockPos());
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
        var server = MinecraftClient.getInstance().getServer();
        if (server != null && !server.getBossBarManager().getAll().isEmpty())
            return false;

        // We use the current mood percentage to ensure that when mining, the player is not notified of biome changes
        // they would not be able to see.
        if (clientWorld.getRegistryKey() == World.NETHER)
            // For the nether, the mood percentage tends to fluctuate more often and at higher values (e.g. >0.0008f).
            return instance.getMoodPercentage() < 0.001f;

        // For the overworld, the mood percentage tends to stay at 0f, except in denser environments (e.g. Dark Forest)
        // where it instead may go as high as 0.0004f.
        return instance.getMoodPercentage() < 0.0005f
            || clientWorld.isSkyVisibleAllowingSea(blockPos) // For ocean biomes, where the mood may be too high.
            || biome.isIn(CAVES); // For cave biomes.
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        // if server has the mod, track on the server, otherwise track here!
    }
}
