package org.eclipse.osc.modules.ocl.loader;

import lombok.Data;

@Data
public class BaseImage {

    private String name;
    private String type;
    private BaseImageFilter filters;

}