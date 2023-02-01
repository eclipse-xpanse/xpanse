package org.eclipse.osc.modules.ocl.loader.data.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Subnet extends RuntimeBase {

    private String vpc;
    private String name;
    private String cidr;

}
