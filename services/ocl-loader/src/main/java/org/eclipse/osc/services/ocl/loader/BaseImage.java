package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

@Data
public class BaseImage {

    private String name;
    private String type;
    private BaseImageFilter filters;

}