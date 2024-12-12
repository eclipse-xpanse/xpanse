/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.database;

import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.DatabaseType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Service for managing database. */
@Component
public class DatabaseManager {

    @Value("${spring.datasource.name:h2}")
    private String dataSourceName;

    @Value("${spring.datasource.url:jdbc:h2:file:./testdb}")
    private String dataSourceUrl;

    /**
     * Get system status of database.
     *
     * @return system status of database.
     */
    public BackendSystemStatus getDatabaseStatus() {
        DatabaseType databaseType = DatabaseType.getByValue(dataSourceName);
        BackendSystemStatus databaseStatus = new BackendSystemStatus();
        databaseStatus.setBackendSystemType(BackendSystemType.DATABASE);
        databaseStatus.setName(databaseType.toValue());
        databaseStatus.setHealthStatus(HealthStatus.OK);
        databaseStatus.setEndpoint(dataSourceUrl);
        return databaseStatus;
    }
}
