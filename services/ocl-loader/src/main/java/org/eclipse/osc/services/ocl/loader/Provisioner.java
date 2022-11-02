package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

import java.util.List;

@Data
public class Provisioner {

    private String name;
    private String type;
    private List<String> environment_vars;
    private List<String> inline;

}
