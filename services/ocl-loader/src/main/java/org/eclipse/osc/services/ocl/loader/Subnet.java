package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

@Data
public class Subnet {

    private String vpc;
    private String name;
    private String cidr;
    private String acl;
    private String routes;

}
