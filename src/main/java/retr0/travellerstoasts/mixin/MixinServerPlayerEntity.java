package retr0.travellerstoasts.mixin;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import retr0.travellerstoasts.TravellersToasts;
import retr0.travellerstoasts.TravellersToasts.IMixinServerPlayerEntity;

import static retr0.travellerstoasts.TravellersToasts.MONITOR_INHABITED_TIME;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity implements IMixinServerPlayerEntity {
    @Unique private final ServerPlayerEntity instance = ((ServerPlayerEntity) (Object) this);
    @Unique private BlockPos previousBlockPos;

    @Unique public boolean trackInhabitedTime = false;

    @Unique @Override
    public void beginTracking() {
        trackInhabitedTime = true;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        final int TICKS_PER_SECOND = 20;
        final int MAX_INHABITED_TIME = TICKS_PER_SECOND * 60 * 120; // i.e. 2 hours.

        if (!trackInhabitedTime) return;

        var blockPos = instance.getBlockPos();
        if (blockPos == previousBlockPos) return;

        var x = ChunkSectionPos.getSectionCoord(blockPos.getX());
        var z = ChunkSectionPos.getSectionCoord(blockPos.getZ());
        // if server has the mod, track on the server, otherwise track here!
        var chunk = ((ServerWorld) instance.world).getChunkManager().getWorldChunk(x, z, false);

        if (!instance.server.getBossBarManager().getAll().isEmpty()) {
            TravellersToasts.LOGGER.info("Response withheld due to bossbar!");
            return;
        }

        if (chunk != null && chunk.getInhabitedTime() <= MAX_INHABITED_TIME) {
            TravellersToasts.LOGGER.info("Sent " + MONITOR_INHABITED_TIME.getPath() + " notification to " + instance.getDisplayName().toString());
            ServerPlayNetworking.send(instance, MONITOR_INHABITED_TIME, PacketByteBufs.empty());
            trackInhabitedTime = false;
        }
        previousBlockPos = blockPos;
    }
}
