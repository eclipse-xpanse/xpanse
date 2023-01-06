package org.eclipse.osc.modules.ocl.loader;

import lombok.Data;

import java.util.List;

@Data
public class Provisioner {

    private String name;
    private String type;
    private List<String> environments;
    private List<String> inline;

}
