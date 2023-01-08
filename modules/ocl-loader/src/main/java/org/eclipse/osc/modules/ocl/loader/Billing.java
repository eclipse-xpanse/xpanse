package org.eclipse.osc.modules.ocl.loader;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Billing {

    private String model;
    private String period;
    private String currency;
    private Double fixedPrice;
    private Double variablePrice;
    private String variableItem;
    private String backend;
    private Map<String, Object> properties = new HashMap<>();

}
