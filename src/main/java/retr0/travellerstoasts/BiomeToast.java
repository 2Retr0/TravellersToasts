package retr0.travellerstoasts;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
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
    public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        var toastHeader = Text.translatable(MOD_ID + ".toast.header");
        var biomeName = Text.translatable(biomeId.toTranslationKey("biome"));

        if (this.justUpdated) {
            this.startTime = startTime;
            this.justUpdated = false;
        }

        // --- Draw Toast Background ---
        context.drawTexture(TEXTURE, 0, 0, 0, 32, this.getWidth(), this.getHeight());

        // --- Draw Toast Description ---
        context.drawText(manager.getClient().textRenderer, toastHeader, 30, 7, 0xFF500050, false);
        context.drawText(manager.getClient().textRenderer, biomeName, 30, 18, 0xFF000000, false);

        // --- Draw Biome Icon Plaque ---
        context.drawTexture(ICON_PLAQUE_TEXTURE, 4, 4, TravellersToastsConfig.roundedIconBackground ? 24 : 0, 0, 24, 24);

        // --- Draw Biome Icon ---
        var iconIdentifier = getBiomeIconIdentifier(biomeId);
        var doesIconExist = manager.getClient().getResourceManager().getResource(iconIdentifier).isPresent();
        context.drawTexture(
            doesIconExist ? iconIdentifier : ICON_FALLBACK_TEXTURE, 8, 8, 0, 0, 16, 16, 16, 16);

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
