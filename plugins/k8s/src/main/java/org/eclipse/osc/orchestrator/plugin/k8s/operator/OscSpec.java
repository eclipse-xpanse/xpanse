package org.eclipse.osc.orchestrator.plugin.k8s.operator;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OscSpec {

    private String name;
    private String ocl;

}
