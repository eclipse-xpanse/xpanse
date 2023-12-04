/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.enumcolumn;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * Implementation of the EnumColumnConstraintManage.
 */
@Slf4j
@Repository
@Transactional
public class EnumColumnConstraintManage {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Query the table and column whether exists in the database.
     *
     * @param tableName  table name.
     * @param columnName column name.
     * @return true if the table and column exists in the database.
     */
    public boolean queryTableColumnExisted(String tableName, String columnName) {
        String queryStatement = String.format(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS"
                        + " WHERE TABLE_NAME = '%s' AND COLUMN_NAME = '%s'",
                tableName, columnName);
        return !CollectionUtils.isEmpty(
                entityManager.createNativeQuery(queryStatement).getResultList());
    }

    /**
     * Update the enum column values in table for MySql.
     *
     * @param tableName        table name.
     * @param columnName       column name.
     * @param enumValuesString the string of enum values.
     */
    public void updateTableEnumColumnValuesForMySql(String tableName, String columnName,
                                                    String enumValuesString) {

        String ddlStatement = String.format("ALTER TABLE %s MODIFY COLUMN %s ENUM(%s)",
                tableName, columnName, enumValuesString);
        entityManager.createNativeQuery(ddlStatement).executeUpdate();
    }


    /**
     * Query the constraints of enum column in table for H2.
     *
     * @param tableName  table name.
     * @param columnName column name.
     * @return the constraints of enum column in table for H2.
     */
    @SuppressWarnings("unchecked")
    public List<String> queryConstraintForH2TableEnumColumn(String tableName, String columnName) {
        String queryStatement = String.format(
                "SELECT T2.CONSTRAINT_NAME FROM INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE T1,"
                        + " INFORMATION_SCHEMA.CHECK_CONSTRAINTS T2 "
                        + " WHERE T1.CONSTRAINT_NAME = T2.CONSTRAINT_NAME "
                        + " AND TABLE_NAME = '%s' AND COLUMN_NAME = '%s'",
                tableName, columnName);
        return entityManager.createNativeQuery(queryStatement).getResultList();
    }


    /**
     * Drop the old constraint of enum column in table for H2.
     *
     * @param tableName      table name.
     * @param constraintName constraint name.
     */
    public void dropOldConstraintForH2TableEnumColumn(String tableName, String constraintName) {
        String ddlStatement =
                String.format("ALTER TABLE %s DROP CONSTRAINT %s", tableName, constraintName);
        entityManager.createNativeQuery(ddlStatement).executeUpdate();
    }


    /**
     * Add the new constraint of enum column in table for H2.
     *
     * @param tableName        table name.
     * @param columnName       column name.
     * @param constraintName   constraint name.
     * @param enumValuesString the string of enum values.
     */
    public void addNewConstraintForH2TableEnumColumn(String tableName, String columnName,
                                                     String constraintName,
                                                     String enumValuesString) {
        String constraintValue = String.format("\"%s\" IN (%s)", columnName, enumValuesString);
        String ddlStatement = String.format("ALTER TABLE %s ADD CONSTRAINT %s CHECK(%s)",
                tableName, constraintName, constraintValue);
        entityManager.createNativeQuery(ddlStatement).executeUpdate();
    }

}
