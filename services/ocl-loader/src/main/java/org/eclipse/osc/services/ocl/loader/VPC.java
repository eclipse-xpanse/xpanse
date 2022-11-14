package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

@Data
public class VPC extends RuntimeBase {

    private String name;
    private String cidr;

}
