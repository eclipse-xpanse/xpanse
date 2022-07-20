package org.eclipse.osc.core.ocl;

import lombok.Data;

import java.util.List;

@Data
public class Network {

    private Vpc vpc;
    private Subnet subnet;
    private List<Dns> dns;

}
