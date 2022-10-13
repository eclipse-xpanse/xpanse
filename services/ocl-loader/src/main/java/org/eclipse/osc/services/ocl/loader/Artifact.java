package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Artifact {

    private String name;
    private String type;
    private String url;
    private Map<String, Object> properties = new HashMap<>();

}
