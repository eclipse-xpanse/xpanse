package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

import java.util.List;

@Data
public class RouteTable {

    private String name;
    private List<RouteTableEntry> table;

}
