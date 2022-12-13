package org.eclipse.osc.services.k8s.operator;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OscStatus {

    private boolean registered;
    private boolean started;

}
