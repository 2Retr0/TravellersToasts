package retr0.travellerstoasts;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import retr0.travellerstoasts.config.TravellersToastsConfig;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

public class BiomeToast implements Toast {
    private static final Identifier ICON_PLAQUE_TEXTURE = new Identifier(MOD_ID, "textures/gui/icon_plaque.png");
    private static final Identifier ICON_FALLBACK_TEXTURE = getBiomeIconIdentifier(new Identifier("meadow"));
    private static final long DURATION = 5000L;

    private long startTime;
    private boolean justUpdated;
    private Identifier biomeId;

    public BiomeToast(Identifier biomeId) { this.biomeId = biomeId; }


    private static Identifier getBiomeIconIdentifier(Identifier biomeId) {
        var biomeAssetPath = "textures/gui/icons/" + biomeId.getNamespace() + "/" + biomeId.getPath() + ".png";

        return new Identifier(MOD_ID, biomeAssetPath);
    }


    @Override
    public Toast.Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        var toastHeader = Text.translatable(MOD_ID + ".toast.header");
        var biomeName = Text.translatable(biomeId.toTranslationKey("biome"));

        if (this.justUpdated) {
            this.startTime = startTime;
            this.justUpdated = false;
        }

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        // Draw toast background
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        DrawableHelper.drawTexture(matrices, 0, 0, 0, 32, this.getWidth(), this.getHeight());

        // Draw toast description
        manager.getClient().textRenderer.draw(matrices, toastHeader, 30.0f, 7.0f, 0xFF500050);
        manager.getClient().textRenderer.draw(matrices, biomeName, 30.0f, 18.0f, 0xFF000000);

        // Draw biome icon plaque
        RenderSystem.setShaderTexture(0, ICON_PLAQUE_TEXTURE);
        DrawableHelper.drawTexture(matrices, 4, 4, TravellersToastsConfig.roundedIconBackground ? 24 : 0, 0, 24, 24);

        // Draw biome icon (default if icon for the biome doesn't exist)
        var iconIdentifier = getBiomeIconIdentifier(biomeId);
        var doesIconExist = manager.getClient().getResourceManager().getResource(iconIdentifier).isPresent();
        RenderSystem.setShaderTexture(0, doesIconExist ? iconIdentifier : ICON_FALLBACK_TEXTURE);
        DrawableHelper.drawTexture(matrices, 8, 8, 0, 0, 16, 16, 16, 16);

        return startTime - this.startTime >= DURATION ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }



    private void addBiome(Identifier biomeId) {
        this.biomeId = biomeId;
        justUpdated = true;
    }



    public static void show(ToastManager manager, RegistryEntry<Biome> biome) {
        BiomeToast biomeToast = manager.getToast(BiomeToast.class, TYPE);
        biome.getKey().ifPresent(key -> {
            if (biomeToast == null)
                manager.add(new BiomeToast(key.getValue()));
            else
                biomeToast.addBiome(key.getValue());
        });
    }
}
