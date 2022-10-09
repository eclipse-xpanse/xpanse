package org.eclipse.osc.services.ocl.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.net.URL;
import java.util.List;

@Data
public class Ocl {

    private String name;
    private String category;
    private String namespace;

    private Billing billing;
    private Compute compute;
    private Network network;
    private List<Storage> storage;

}
