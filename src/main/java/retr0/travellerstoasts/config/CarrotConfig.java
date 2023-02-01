package retr0.travellerstoasts.config;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.util.ActionResult;
import retr0.travellerstoasts.TravellersToasts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CarrotConfig {
    public static final Map<String, Class<?>> configClassMap = new HashMap<>();
    public static final List<EntryInfo> configEntries = new ArrayList<>();

    private static final Gson gson = new GsonBuilder()
        .excludeFieldsWithModifiers(Modifier.TRANSIENT)
        .excludeFieldsWithModifiers(Modifier.PRIVATE)
        .addSerializationExclusionStrategy(new HiddenAnnotationExclusionStrategy())
        .setPrettyPrinting()
        .create();

    public record EntryInfo(String key, Field field, Object defaultValue, boolean isColor) { }

    public static void init(String modId, Class<?> configClass) {
        var configPath = FabricLoader.getInstance().getConfigDir().resolve(modId + ".json");
        configClassMap.put(modId, configClass);

        for (var field : configClass.getFields()) {
            if (field.isAnnotationPresent(Entry.class)) {
                var key = modId + ".carrotconfig." + field.getName();

                Object defaultValue = null;
                try { defaultValue = field.get(null); } catch (IllegalAccessException ignored) {}

                configEntries.add(new EntryInfo(key, field, defaultValue, field.getAnnotation(Entry.class).isColor()));
            }
        }

        try {
            gson.fromJson(Files.newBufferedReader(configPath), configClass);
        } catch (Exception e) {
            write(modId);
        }
    }

    public static void write(String modId) {
        var path = FabricLoader.getInstance().getConfigDir().resolve(modId + ".json");
        var configClass = configClassMap.get(modId);

        try {
            if (!Files.exists(path)) Files.createFile(path);

            // Write config class values to the config file and notify ConfigSavedCallback listeners.
            Files.write(path, gson.toJson(configClass.getDeclaredConstructor().newInstance()).getBytes());
            CarrotConfig.ConfigSavedCallback.EVENT.invoker().onConfigSaved(configClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Screen getScreen(Screen parent, String modId) {
        return new CarrotConfigScreen(parent, modId);
    }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Entry {
        int width() default 100;
        double min() default Double.MIN_NORMAL;
        double max() default Double.MAX_VALUE;
        String name() default "";
        boolean isColor() default false;
    }

    public static class HiddenAnnotationExclusionStrategy implements ExclusionStrategy {
        public boolean shouldSkipClass(Class<?> _class) { return false; }

        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return fieldAttributes.getAnnotation(Entry.class) == null;
        }
    }

    @FunctionalInterface
    public interface ConfigSavedCallback {
        Event<ConfigSavedCallback> EVENT = EventFactory.createArrayBacked(ConfigSavedCallback.class,
            (listeners) -> (configClass) -> {
                for (var listener : listeners) listener.onConfigSaved(configClass);
            });

        void onConfigSaved(Class<?> configClass);
    }
}
