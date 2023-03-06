package retr0.travellerstoasts.mixin;

import com.mojang.authlib.GameProfile;
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
import retr0.travellerstoasts.extension.ExtensionServerPlayerEntity;
import retr0.travellerstoasts.network.TrackInhabitedTimeS2CPacket;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity implements ExtensionServerPlayerEntity {
    @Unique private BlockPos previousBlockPos;
    @Unique private int maxInhabitedTime = -1;

    @Override
    public void beginTracking(int maxInhabitedTimeTicks) { maxInhabitedTime = maxInhabitedTimeTicks; }



    @Override
    public void stopTracking(boolean finishedQuery) {
        maxInhabitedTime = -1; // Stop tracking.
        TrackInhabitedTimeS2CPacket.send(finishedQuery, (ServerPlayerEntity) (Object) this);
    }


    /**
     * Handles inhabited time tracking for the {@link ServerPlayerEntity} instance.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        var blockPos = getBlockPos();

        // Don't bother checking if the player hasn't moved (tracking would have stopped if a valid chunk was found).
        if (maxInhabitedTime < 0 || blockPos == previousBlockPos) return;

        var x = ChunkSectionPos.getSectionCoord(blockPos.getX());
        var z = ChunkSectionPos.getSectionCoord(blockPos.getZ());
        var chunk = getWorld().getChunkManager().getWorldChunk(x, z, false);

        // If player is on a chunk with an inhabited time less than the specified maximum, send a response back to the
        // client and stop tracking.
        if (chunk != null && chunk.getInhabitedTime() <= maxInhabitedTime) stopTracking(true);
        previousBlockPos = blockPos;
    }

    public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }
}
