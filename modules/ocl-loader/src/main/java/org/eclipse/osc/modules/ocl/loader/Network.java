package org.eclipse.osc.modules.ocl.loader;

import lombok.Data;

import java.util.List;

@Data
public class Network {

    private String id;

    private List<VPC> vpc;
    private List<Subnet> subnet;
    private List<Security> security;

}
