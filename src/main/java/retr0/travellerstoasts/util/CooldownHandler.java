package retr0.travellerstoasts.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record CooldownHandler<K>(Supplier<Long> cooldownSupplier, Map<K, Long> cooldownCache) {
    public CooldownHandler(Supplier<Long> cooldownMsSupplier) {
        this(cooldownMsSupplier, new HashMap<>());
    }

    public boolean hasCooled(K key) {
        return System.currentTimeMillis() - cooldownCache.getOrDefault(key, -1L) > cooldownSupplier.get();
    }

    public void refresh(K key) {
        cooldownCache.put(key, System.currentTimeMillis());
    }

    public void reset() {
        cooldownCache.clear();
    }
}
