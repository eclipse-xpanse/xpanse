/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.database;

import java.util.Arrays;
import java.util.List;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.DatabaseType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Service for managing database.
 */
@Component
public class DatabaseManager {

    @Value("${spring.datasource.url:jdbc:h2:file:./testdb}")
    private String dataSourceUrl;

    /**
     * Get system status of database.
     *
     * @return system status of database.
     */
    public BackendSystemStatus getDatabaseStatus() {
        List<String> databaseUrlSplitList = Arrays.asList(dataSourceUrl.split(":"));
        if (databaseUrlSplitList.contains(DatabaseType.H2DB.toValue())) {
            BackendSystemStatus databaseStatus = new BackendSystemStatus();
            databaseStatus.setBackendSystemType(BackendSystemType.DATABASE);
            databaseStatus.setName(DatabaseType.H2DB.toValue());
            databaseStatus.setHealthStatus(HealthStatus.OK);
            databaseStatus.setEndpoint(dataSourceUrl);
            return databaseStatus;
        }
        if (databaseUrlSplitList.contains(DatabaseType.MYSQL.toValue())) {
            BackendSystemStatus databaseStatus = new BackendSystemStatus();
            databaseStatus.setBackendSystemType(BackendSystemType.DATABASE);
            databaseStatus.setName(DatabaseType.MYSQL.toValue());
            databaseStatus.setHealthStatus(HealthStatus.OK);
            databaseStatus.setEndpoint(dataSourceUrl);
            return databaseStatus;
        }
        return null;
    }
}
