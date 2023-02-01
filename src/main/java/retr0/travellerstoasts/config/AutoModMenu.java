package retr0.travellerstoasts.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import retr0.travellerstoasts.TravellersToasts;

import java.util.HashMap;
import java.util.Map;

import static retr0.travellerstoasts.TravellersToasts.MOD_ID;

public class AutoModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> CarrotConfig.getScreen(parent, MOD_ID);
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        HashMap<String, ConfigScreenFactory<?>> factoryMap = new HashMap<>();
        CarrotConfig.configClassMap.forEach((modId, cClass) ->
            factoryMap.put(modId, parent -> CarrotConfig.getScreen(parent, modId)));
        return factoryMap;
    }
}
