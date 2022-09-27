package retr0.travellerstoasts.mixin;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity {
    @Unique private final PlayerEntity instance = ((PlayerEntity) (Object) this);

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (instance.world.isClient()) {
            return;
        } else {
            var blockPos = instance.getBlockPos();
            var x = ChunkSectionPos.getSectionCoord(blockPos.getX());
            var z = ChunkSectionPos.getSectionCoord(blockPos.getZ());

            var worldChunk = instance.world.getChunkManager().getWorldChunk(x, z, false);

            if (worldChunk != null) {
                var inhabitedTime = worldChunk.getInhabitedTime();

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeLong(inhabitedTime);
                buf.writeBlockPos(blockPos);

                ServerPlayNetworking.send((ServerPlayerEntity) instance, new Identifier(MOD_ID, "inhabited_time_sync"), buf);
            }
        }
    }
}
