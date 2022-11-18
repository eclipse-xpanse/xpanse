package org.eclipse.osc.orchestrator.plugin.huaweicloud;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.karaf.minho.boot.service.ConfigService;

public class BuilderContext extends HashMap<String, Map<String, String>> {

    @Getter
    @Setter
    private ConfigService config;
}
