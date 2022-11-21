package org.eclipse.osc.services.ocl.loader;

import java.util.List;
import lombok.Data;

@Data
public class Artifact extends RuntimeBase {

    private String name;
    private String base;
    private List<String> provisioners;

}
