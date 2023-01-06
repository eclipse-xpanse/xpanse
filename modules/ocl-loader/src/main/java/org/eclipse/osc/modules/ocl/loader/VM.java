package org.eclipse.osc.modules.ocl.loader;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
