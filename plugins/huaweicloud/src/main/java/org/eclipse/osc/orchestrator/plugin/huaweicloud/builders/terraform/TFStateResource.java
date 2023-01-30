package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TFStateResource {

    public String type;

    public String name;

    public List<TFStateResourceInstance> instances;
}
