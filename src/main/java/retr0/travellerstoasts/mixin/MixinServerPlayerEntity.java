package retr0.travellerstoasts.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import retr0.travellerstoasts.TravellersToasts;
import retr0.travellerstoasts.extension.ServerPlayerEntityExtension;
import retr0.travellerstoasts.network.TrackInhabitedTimeS2CPacket;

import static retr0.travellerstoasts.network.TrackInhabitedTimeS2CPacket.INHABITED_TIME_TRACK_RESPONSE_ID;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity implements ServerPlayerEntityExtension {
    @Unique private BlockPos previousBlockPos;
    @Unique private int maxInhabitedTime = -1;

    @Override
    public void beginTracking(int maxInhabitedTimeTicks) { maxInhabitedTime = maxInhabitedTimeTicks; }

    @Override
    public void stopTracking(boolean finishedQuery) {
        maxInhabitedTime = -1; // Stop tracking.
        ServerPlayNetworking.send((ServerPlayerEntity) (Object) this, INHABITED_TIME_TRACK_RESPONSE_ID,
            TrackInhabitedTimeS2CPacket.create(finishedQuery));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (maxInhabitedTime < 0) return;

        var blockPos = getBlockPos();
        // Don't bother checking if the player hasn't moved (tracking would have stopped if a valid chunk was found).
        if (blockPos == previousBlockPos) return;

        var x = ChunkSectionPos.getSectionCoord(blockPos.getX());
        var z = ChunkSectionPos.getSectionCoord(blockPos.getZ());
        var chunk = getWorld().getChunkManager().getWorldChunk(x, z, false);

        // If player is on a chunk with an inhabited time less than MAX_INHABITED_TIME, send a response back to the
        // client and stop tracking.
        if (chunk != null && chunk.getInhabitedTime() <= maxInhabitedTime) {
            TravellersToasts.LOGGER.info("Server: Sent valid inhabited time response to client for player " + this.getName().getString() + "!");
            stopTracking(true);
        }
        // else {
        //     TravellersToasts.LOGGER.info("---Invalid inhabited time for player " + this.getName().getString() + ".");
        // }
        previousBlockPos = blockPos;
    }

    public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }
}
