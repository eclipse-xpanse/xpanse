/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.enumcolumn;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/** Implementation of the EnumColumnConstraintManage. */
@Slf4j
@Component
@Transactional
public class EnumColumnConstraintManage {

    @PersistenceContext private EntityManager entityManager;

    /**
     * Query the table and column whether exists in the database.
     *
     * @param tableName table name.
     * @param columnName column name.
     * @return true if the table and column exists in the database.
     */
    public boolean queryTableColumnExisted(String tableName, String columnName) {
        String queryStatement =
                String.format(
                        "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS"
                                + " WHERE TABLE_NAME = '%s' AND COLUMN_NAME = '%s'",
                        tableName, columnName);
        return !CollectionUtils.isEmpty(
                entityManager.createNativeQuery(queryStatement).getResultList());
    }

    /**
     * Update the enum column values in table for MySql.
     *
     * @param tableName table name.
     * @param columnName column name.
     * @param enumValuesString the string of enum values.
     */
    public void updateTableEnumColumnValuesForMySql(
            String tableName, String columnName, String enumValuesString) {

        String ddlStatement =
                String.format(
                        "ALTER TABLE %s MODIFY COLUMN %s ENUM(%s)",
                        tableName, columnName, enumValuesString);
        entityManager.createNativeQuery(ddlStatement).executeUpdate();
    }

    /**
     * Update the enum column values in table for H2.
     *
     * @param tableName table name.
     * @param columnName column name.
     * @param enumValuesString the string of enum values.
     */
    public void updateTableEnumColumnValuesForH2(
            String tableName, String columnName, String enumValuesString) {
        String ddlStatement =
                String.format(
                        "ALTER TABLE %s ALTER COLUMN %s ENUM(%s)",
                        tableName, columnName, enumValuesString);
        entityManager.createNativeQuery(ddlStatement).executeUpdate();
    }
}
