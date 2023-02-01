package retr0.travellerstoasts.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.encryption.PlayerPublicKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import retr0.travellerstoasts.BiomeToast;
import retr0.travellerstoasts.TravellersToastsClient;

@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends AbstractClientPlayerEntity {
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
        TravellersToastsClient.BIOME_TOAST_MANAGER.update((ClientPlayerEntity) (Object) this);
    }

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile, @Nullable PlayerPublicKey publicKey) {
        super(world, profile);
    }
}
