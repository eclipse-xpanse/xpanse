/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.billing;

import java.util.List;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.service.BillingDataResponse;

/**
 * This interface describes the billing for the server.
 */
public interface BillingService {

    /**
     * get the Csp of the billing.
     */
    Csp getCsp();

    /**
     * Method to git on demand billing.
     *
     * @param deployServiceEntity the resource of the deployment.
     * @param unit query method, true is the unit price, false is the total price.
     */
    List<BillingDataResponse> onDemandBilling(DeployServiceEntity deployServiceEntity,
            Boolean unit);

}
