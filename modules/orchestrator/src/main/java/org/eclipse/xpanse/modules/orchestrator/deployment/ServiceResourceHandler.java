/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.deployment;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;

/**
 * This interface describes the DeployResourceHandler used to extract the resources
 * used by the service.
 */
public interface ServiceResourceHandler {


    /**
     * get the resource handler of the CSP plugin.
     *
     * @return the resource handler of the plugin.
     */
    Map<DeployerKind, DeployResourceHandler> resourceHandlers();

    /**
     * get the existing resources of the specified kind.
     *
     * @param userId    id of the user.
     * @param region    region of the resource.
     * @param kind      kind of the resource.
     * @param serviceId id of the service.
     * @return the existing resources names with the specified kind.
     */
    List<String> getExistingResourceNamesWithKind(String userId, String region,
                                                  DeployResourceKind kind, UUID serviceId);


    /**
     * get the availability zones of the specified region.
     *
     * @param userId id of the user.
     * @param region region.
     * @return the availability zones of the specified region.
     */
    List<String> getAvailabilityZonesOfRegion(String userId, String region, UUID serviceId);
}
