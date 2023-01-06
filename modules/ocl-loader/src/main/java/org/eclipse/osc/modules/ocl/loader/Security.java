package org.eclipse.osc.modules.ocl.loader;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class Security extends RuntimeBase {

    private String name;
    private List<SecurityRule> rules;

}
