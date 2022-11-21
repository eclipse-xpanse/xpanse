package org.eclipse.osc.services.ocl.loader;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class VM extends RuntimeBase {

    private String name;
    private String type;
    private String image;
    private List<String> subnet;
    private List<String> security;
    private List<String> storage;
    private boolean publicly;

}
