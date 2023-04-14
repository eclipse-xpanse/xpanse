/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.register;

import java.util.List;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.models.query.RegisteredServiceQuery;
import org.eclipse.xpanse.modules.models.resource.Ocl;
import org.eclipse.xpanse.modules.models.view.CategoryOclVo;

/**
 * This interface describes register service in charge of interacting with backend fundamental
 * APIs.
 */
public interface RegisterService {

    /**
     * register service using the ocl.
     *
     * @param ocl the Ocl model describing the register service.
     * @return Returns registered service DB entity.
     */
    RegisterServiceEntity registerService(Ocl ocl);

    /**
     * Update registered service using id and the ocl file url.
     *
     * @param registeredServiceId id of the registered service.
     * @param oclLocation         url of the ocl file.
     * @return Returns registered service DB entity.
     */
    RegisterServiceEntity updateRegisteredServiceByUrl(String registeredServiceId,
            String oclLocation)
            throws Exception;

    /**
     * register service using the url of ocl file.
     *
     * @param oclLocation url of the ocl file.
     * @return Returns the ID of registered service.
     */
    RegisterServiceEntity registerServiceByUrl(String oclLocation) throws Exception;

    /**
     * Update registered service using id and the ocl model.
     *
     * @param registeredServiceId id of the registered service.
     * @param ocl                 the Ocl model describing the register service.
     * @return Returns registered service DB entity.
     */
    RegisterServiceEntity updateRegisteredService(String registeredServiceId, Ocl ocl);


    /**
     * Get detail of registered service using ID.
     *
     * @param registeredServiceId the ID of registered service.
     * @return Returns registered service DB entity.
     */
    RegisterServiceEntity getRegisteredService(String registeredServiceId);

    /**
     * Search registered service by query model.
     *
     * @param query the query model for search registered service.
     * @return Returns list of registered service DB entity.
     */
    List<RegisterServiceEntity> queryRegisteredServices(RegisteredServiceQuery query);

    /**
     * Search registered service tree by query model.
     *
     * @param query the query model for search managed service.
     * @return Returns Tree of RegisterServiceEntity
     */
    List<CategoryOclVo> getManagedServicesTree(RegisteredServiceQuery query);

    /**
     * Unregister service using the ID of registered service.
     *
     * @param registeredServiceId the ID of registered service.
     */
    void unregisterService(String registeredServiceId);

    /**
     * generate OpenApi for registered service using the ID.
     *
     * @param id ID of registered service.
     * @return path of openapi.html
     */
    String getOpenApiUrl(String id);

}
