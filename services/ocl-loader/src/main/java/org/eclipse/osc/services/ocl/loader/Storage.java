package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

@Data
public class Storage extends RuntimeBase {

    private String name;
    private String type;
    private String size;

}
