package org.eclipse.osc.modules.ocl.loader;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Storage extends RuntimeBase {

    private String name;
    private String type;
    private String size;

}
