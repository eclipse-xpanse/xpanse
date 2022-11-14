package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

@Data
public class Subnet extends RuntimeBase {

    private String vpc;
    private String name;
    private String cidr;

}
