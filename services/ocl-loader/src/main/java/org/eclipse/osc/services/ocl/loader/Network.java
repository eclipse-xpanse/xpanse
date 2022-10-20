package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

import java.util.List;

@Data
public class Network {

    private List<VPC> vpc;
    private List<Subnet> subnet;
    private List<Security> security;

}
