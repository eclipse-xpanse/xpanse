package org.eclipse.osc.services.ocl.loader;

import java.util.List;
import lombok.Data;

@Data
public class Security extends RuntimeBase {

    private String name;
    private List<SecurityRule> rules;

}
