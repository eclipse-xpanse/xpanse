package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

import java.util.List;

@Data
public class Artifact extends RuntimeBase {

    private String name;
    private String base;
    private List<String> provisioners;

}
