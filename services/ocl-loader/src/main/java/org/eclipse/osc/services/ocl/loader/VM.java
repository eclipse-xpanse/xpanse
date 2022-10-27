package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

import java.util.List;

@Data
public class VM {

    private String name;
    private String type;
    private String image;
    private List<String> subnet;
    private List<String> security;
    private List<String> storage;
    private boolean publicly;

}
