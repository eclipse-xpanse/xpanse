package org.eclipse.osc.api.billing;

import lombok.Data;

@Data
public class BillingRegistration {

    private String billingHook;
    private String model;
    private double amount;

}
