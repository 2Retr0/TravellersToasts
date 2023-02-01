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
import retr0.travellerstoasts.network.ServerPlayerEntityExtension;
import retr0.travellerstoasts.network.packet.InhabitedTimeTrackingHandler;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity implements ServerPlayerEntityExtension {
    @Unique private BlockPos previousBlockPos;
    @Unique private int maxInhabitedTime = -1;

    @Override
    public void beginTracking(int maxInhabitedTimeTicks) {
        maxInhabitedTime = maxInhabitedTimeTicks;
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
            InhabitedTimeTrackingHandler.sendResponse((ServerPlayerEntity) (Object) this);
            maxInhabitedTime = -1; // Stop tracking.
        }
        previousBlockPos = blockPos;
    }

    public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }
}
