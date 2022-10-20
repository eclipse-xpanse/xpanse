package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

@Data
public class SecurityRule {

    private String name;
    private Integer priority;
    private String protocol;
    private String cidr;
    private String direction;
    private String ports;
    private String action;

}
