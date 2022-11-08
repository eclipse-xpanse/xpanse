package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BuilderContext {
    private final Map<String, Object> ctx = new HashMap<>();

    public void put(String key, Object object) {
        ctx.put(key, object);
    }

    public Optional<Object> get(String key) {
        if (ctx.containsKey(key)) {
            return Optional.ofNullable(ctx.get(key));
        }

        return Optional.empty();
    }
}
