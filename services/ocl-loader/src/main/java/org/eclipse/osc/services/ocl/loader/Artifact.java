package org.eclipse.osc.services.ocl.loader;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Artifact extends RuntimeBase {

    private String name;
    private String base;
    private List<String> provisioners;

}
