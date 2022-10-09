package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

@Data
public class VPC {

    private String name;
    private String cidrs;
    private String routes;
    private String acl;

}
