package org.eclipse.osc.modules.ocl.loader;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class OclResource {

    String state = "inactive";
    String id = "";
    String type = "";
    String name = "";

    Map<String, String> properties = new HashMap<>();
}
