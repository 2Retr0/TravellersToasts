package retr0.travellerstoasts;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.toast.RecipeToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Recipe;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

public class BiomeToast implements Toast {
    private static final long DURATION = 5000L;
    private RegistryEntry<Biome> currentBiome;
    private long startTime;
    private boolean justUpdated;

    public BiomeToast(RegistryEntry<Biome> biome) {
        currentBiome = biome;
    }

    @Override
    public Toast.Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        var biomeKey = currentBiome.getKey();
        var biomeId = biomeKey.get().getValue();

        var biomeName = Text.translatable(biomeId.toTranslationKey("biome"));
        var assetPath = "textures/gui/icons/" + biomeId.getNamespace() + "/" + biomeId.getPath() + ".png";

        if (this.justUpdated) {
            this.startTime = startTime;
            this.justUpdated = false;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        manager.drawTexture(matrices, 0, 0, 0, 32, this.getWidth(), this.getHeight());
        manager.getClient().textRenderer.draw(matrices, "Exploring Biome", 30.0f, 7.0f, 0xFF500050);
        manager.getClient().textRenderer.draw(matrices, biomeName, 30.0f, 18.0f, 0xFF000000);

        RenderSystem.setShaderTexture(0, new Identifier(MOD_ID, "textures/gui/icons/background.png"));
        manager.drawTexture(matrices, 4, 4, 0, 0, 24, 24); // Square
        // manager.drawTexture(matrices, 4, 4, 24, 0, 24, 24); // Crest

        RenderSystem.setShaderTexture(0, new Identifier(MOD_ID, assetPath));
        DrawableHelper.drawTexture(matrices, 8, 8, manager.getZOffset(), 0, 0, 16, 16, 16, 16);

        return startTime - this.startTime >= DURATION ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    private void addBiome(RegistryEntry<Biome> biome) {
        currentBiome = biome;
        justUpdated = true;
    }

    public static void show(ToastManager manager, RegistryEntry<Biome> biome) {
        BiomeToast biomeToast = manager.getToast(BiomeToast.class, TYPE);
        if (biomeToast == null) {
            manager.add(new BiomeToast(biome));
        } else {
            biomeToast.addBiome(biome);
        }
    }
}
