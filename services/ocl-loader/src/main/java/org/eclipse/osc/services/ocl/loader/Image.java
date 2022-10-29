package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

import java.util.List;

@Data
public class Image {

    private List<Provisioner> provisioners;
    private List<BaseImage> base;
    private List<Artifact> artifacts;

}





