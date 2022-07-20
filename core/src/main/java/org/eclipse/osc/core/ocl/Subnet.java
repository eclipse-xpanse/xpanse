package org.eclipse.osc.core.ocl;

import lombok.Data;

@Data
public class Subnet {

    private String vpc;
    private String name;
    private String cidr;
    private String gateway;

}
