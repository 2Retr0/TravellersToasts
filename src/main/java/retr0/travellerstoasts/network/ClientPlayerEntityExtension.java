package retr0.travellerstoasts.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.ToastManager;
import retr0.travellerstoasts.mixin.MixinBossBarHud;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public interface ClientPlayerEntityExtension {
    int HOLD_TICKS = TICKS_PER_SECOND * 3; // Required ticks for the "entering biome" condition.

    MixinBossBarHud bossBars = (MixinBossBarHud) MinecraftClient.getInstance().inGameHud.getBossBarHud();
    ToastManager toastManager = MinecraftClient.getInstance().getToastManager();

    void tryShowToast();

    void resetCooldownCache();
}
