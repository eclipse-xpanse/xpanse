package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

@Data
public class RouteTableEntry {

    private String name;
    private String destination;
    private String type;
    private String nexthop;

}
