package org.eclipse.osc.services.ocl.loader;

import lombok.Data;

import java.util.List;

@Data
public class ACL {
    private String name;
    private List<SecurityRule> rules;
}
