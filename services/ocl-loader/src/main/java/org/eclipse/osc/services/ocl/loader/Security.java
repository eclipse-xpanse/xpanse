package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

import java.util.List;

@Data
public class Security {

    private String name;
    private List<SecurityRule> rules;

}
