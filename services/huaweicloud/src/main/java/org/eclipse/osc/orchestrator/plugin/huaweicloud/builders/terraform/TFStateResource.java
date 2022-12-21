package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TFStateResource {

    public String type;

    public String name;

    public List<TFStateResourceInstance> instances;
}
