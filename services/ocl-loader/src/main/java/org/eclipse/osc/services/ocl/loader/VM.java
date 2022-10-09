package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

@Data
public class VM {

    private String name;
    private String type;
    private String platform;
    private String vpc;
    private String subnet;
    private String security;
    private String storage;
    private boolean publicly;

}
