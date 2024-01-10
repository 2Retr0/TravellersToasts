package retr0.travellerstoasts;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import retr0.travellerstoasts.config.TravellersToastsConfig;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

public class BiomeToast implements Toast {
    private static final Identifier TEXTURE = new Identifier("toast/recipe");
    private static final Identifier PLAQUE_TEXTURE = new Identifier(MOD_ID, "toast/plaque");
    private static final Identifier PLAQUE_ROUNDED_TEXTURE = new Identifier(MOD_ID, "toast/plaque_rounded");
    private static final Identifier FALLBACK_BIOME_TEXTURE = getBiomeIconIdentifier(BiomeKeys.MEADOW.getValue());
    private static final long DURATION = 5000L;

    private long startTime;
    private boolean justUpdated;
    private Identifier biomeId;

    public BiomeToast(Identifier biomeId) { this.biomeId = biomeId; }

    private static Identifier getBiomeIconIdentifier(Identifier biomeId) {
        return new Identifier(MOD_ID, "biome/" + biomeId.getNamespace() + "/" + biomeId.getPath());
    }

    private void drawBiomeIcon(DrawContext context, ToastManager manager, Identifier biomeId) {
        var guiAtlasManager = manager.getClient().getGuiAtlasManager();
        var sprite = guiAtlasManager.getSprite(getBiomeIconIdentifier(biomeId));
        if (sprite.getContents().getId().equals(MissingSprite.getMissingSpriteId()))
            sprite = guiAtlasManager.getSprite(FALLBACK_BIOME_TEXTURE);

        context.drawSprite(8, 8, 0, 16, 16, sprite);
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
        context.drawGuiTexture(TEXTURE, 0, 0, this.getWidth(), this.getHeight());

        // --- Draw Toast Description ---
        context.drawText(manager.getClient().textRenderer, toastHeader, 30, 7, 0xFF500050, false);
        context.drawText(manager.getClient().textRenderer, biomeName, 30, 18, 0xFF000000, false);

        // --- Draw Biome Icon Plaque ---
        context.drawGuiTexture(TravellersToastsConfig.roundedIconBackground ? PLAQUE_ROUNDED_TEXTURE : PLAQUE_TEXTURE, 4, 4, 24, 24);

        // --- Draw Biome Icon ---
        drawBiomeIcon(context, manager, biomeId);

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
