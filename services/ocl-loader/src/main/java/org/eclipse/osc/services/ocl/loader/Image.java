package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

import java.util.List;

@Data
public class Image {

    private List<Provisioner> provisioners;
    private List<BaseImage> base;
    private List<Artifact> artifacts;

}

@Data
class Provisioner {

    private String name;
    private String type;
    private List<String> environment_vars;
    private List<String> inline;

}

@Data
class BaseImageFilter {

    private String name;
    private String id;

}

@Data
class BaseImage {

    private String name;
    private String ssh_user;
    private String type;
    private BaseImageFilter filters;

}