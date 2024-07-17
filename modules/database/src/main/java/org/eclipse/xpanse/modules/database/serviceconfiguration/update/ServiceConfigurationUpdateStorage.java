/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration.update;

/**
 * Interface for persist of ServiceConfigurationUpdateRequest.
 */
public interface ServiceConfigurationUpdateStorage {

    /**
     * Add or update service configuration data to database.
     *
     * @param serviceConfigurationUpdateRequest serviceConfigurationUpdateRequest.
     */
    ServiceConfigurationUpdateRequest storeAndFlush(
            ServiceConfigurationUpdateRequest serviceConfigurationUpdateRequest);
}
