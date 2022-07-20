package org.eclipse.osc.core.ocl;

import lombok.Data;

@Data
public class Billing {

    private String model;
    private String period;
    private String currency;
    private Double fixedPrice;
    private Double variablePrice;
    private String variableItem;

}
