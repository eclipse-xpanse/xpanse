package org.eclipse.osc.modules.ocl.loader;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Console {

    private String backend;
    private Map<String, Object> properties = new HashMap<>();

}
