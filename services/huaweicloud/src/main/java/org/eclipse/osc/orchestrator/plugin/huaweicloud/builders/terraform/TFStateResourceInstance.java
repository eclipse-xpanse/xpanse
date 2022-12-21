package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TFStateResourceInstance {

    public Map<String, Object> attributes;
}
