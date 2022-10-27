package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Data
public class Artifact {

    private String name;
    private String base;
    private List<String> provisioners;

}
