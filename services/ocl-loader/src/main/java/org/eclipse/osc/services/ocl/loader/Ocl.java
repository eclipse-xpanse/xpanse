package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

import java.util.List;

@Data
public class Ocl {

    private String name;
    private String category;
    private String namespace;
    private List<Artifact> artifacts;

    private Billing billing;
    private Compute compute;
    private Network network;
    private List<Storage> storage;

}
