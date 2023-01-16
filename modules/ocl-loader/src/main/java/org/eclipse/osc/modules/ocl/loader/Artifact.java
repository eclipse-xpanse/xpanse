package org.eclipse.osc.modules.ocl.loader;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Artifact extends RuntimeBase {

    private String name;
    private String base;
    private String type;
    private List<String> provisioners;

}
