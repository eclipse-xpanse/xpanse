package org.eclipse.osc.services.ocl.loader;

import java.util.List;
import lombok.Data;

@Data
public class Provisioner {

    private String name;
    private String type;
    private List<String> environments;
    private List<String> inline;

}
