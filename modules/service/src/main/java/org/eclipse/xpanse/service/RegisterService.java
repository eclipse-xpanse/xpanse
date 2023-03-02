/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.service;

import java.util.List;
import org.eclipse.xpanse.modules.database.register.RegisterServiceEntity;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.modules.ocl.loader.data.models.query.RegisterServiceQuery;

/**
 * This interface describes register service in charge of interacting with backend fundamental
 * APIs.
 */
public interface RegisterService {

    /**
     * register service using the ocl.
     *
     * @param ocl the Ocl model describing the register service.
     */
    void registerService(Ocl ocl);

    /**
     * Update registered service using id and the ocl file url.
     *
     * @param id          id of the registered service.
     * @param oclLocation url of the ocl file.
     */
    void updateRegisteredServiceByUrl(String id, String oclLocation) throws Exception;

    /**
     * register service using the url of ocl file.
     *
     * @param oclLocation url of the ocl file.
     */
    void registerServiceByUrl(String oclLocation) throws Exception;

    /**
     * Update registered service using id and the ocl model.
     *
     * @param id  id of the registered service.
     * @param ocl the Ocl model describing the register service.
     */
    void updateRegisteredService(String id, Ocl ocl);


    /**
     * Get detail of registered service using ID.
     *
     * @param managedServiceId the ID of
     * @return registerServiceEntity
     */
    RegisterServiceEntity getRegisteredService(String managedServiceId);

    /**
     * Search registered service by query model.
     *
     * @param query the query model for search registered service.
     * @return list of RegisterServiceEntity
     */
    List<RegisterServiceEntity> listRegisteredService(RegisterServiceQuery query);


    /**
     * Unregister service using the ID of registered service.
     *
     * @param managedServiceId the ID of registered service.
     */
    void unregisterService(String managedServiceId);

}
