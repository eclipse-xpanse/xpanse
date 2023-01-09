package org.eclipse.osc.modules.ocl.loader;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class OclResources {

    String state = "inactive";
    List<OclResource> resources = new ArrayList<>();

}
