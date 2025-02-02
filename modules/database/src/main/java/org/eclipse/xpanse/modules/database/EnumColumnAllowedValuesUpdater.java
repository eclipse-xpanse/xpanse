/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.database;

import jakarta.annotation.Resource;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.enumcolumn.EnumColumnConstraintManage;
import org.eclipse.xpanse.modules.models.system.enums.DatabaseType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/** Service to update database tables column values by code generation automatically. */
@Slf4j
@Profile("dev")
@Component
public class EnumColumnAllowedValuesUpdater implements ApplicationListener<ContextRefreshedEvent> {

    @Resource private EntityManagerFactory entityManagerFactory;

    @Resource private EnumColumnConstraintManage enumColumnConstraintManage;

    @Value("${spring.datasource.name:h2}")
    private String dataSourceName;

    /** Update the values of all enum columns in all tables. */
    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        DatabaseType activeDatabaseType = DatabaseType.getByValue(dataSourceName);
        Map<Class<?>, String> map = getTableNamesMap();
        for (Map.Entry<Class<?>, String> entry : map.entrySet()) {
            List<Field> enumFields = getFieldsWithAnnotationEnum(entry.getKey());
            String tableName = StringUtils.toRootUpperCase(entry.getValue());
            for (Field field : enumFields) {
                Class<?> filedClass = field.getType();
                Column columnAnnotation = field.getAnnotation(Column.class);
                String columnName = StringUtils.toRootUpperCase(field.getName());
                if (Objects.nonNull(columnAnnotation)
                        && StringUtils.isNotBlank(columnAnnotation.name())) {
                    columnName = columnAnnotation.name();
                }
                if (filedClass.isEnum()) {
                    List<String> enumNames = getEnumNames(filedClass);
                    String enumNamesStr =
                            StringUtils.toRootUpperCase(
                                    "'" + StringUtils.join(enumNames, "','") + "'");
                    if (activeDatabaseType == DatabaseType.MYSQL) {
                        updateTableColumnEnumValuesForMySql(tableName, columnName, enumNamesStr);
                    }
                    if (activeDatabaseType == DatabaseType.H2DB) {
                        updateTableColumnEnumValuesForH2(tableName, columnName, enumNamesStr);
                    }
                }
            }
        }
    }

    private void updateTableColumnEnumValuesForMySql(
            String tableName, String columnName, String enumValuesStr) {
        try {
            if (enumColumnConstraintManage.queryTableColumnExisted(tableName, columnName)) {
                enumColumnConstraintManage.updateTableEnumColumnValuesForMySql(
                        tableName, columnName, enumValuesStr);
                log.info(
                        "Update MySql table:{} enum column:{} to values:[{}] completed.",
                        tableName,
                        columnName,
                        enumValuesStr);
            }
        } catch (RuntimeException e) {
            log.error(
                    "Update MySql table:{} enum column:{} to values:[{}] failed. error:{}",
                    tableName,
                    columnName,
                    enumValuesStr,
                    e.getMessage());
        }
    }

    private void updateTableColumnEnumValuesForH2(
            String tableName, String columnName, String enumValuesStr) {
        try {
            if (enumColumnConstraintManage.queryTableColumnExisted(tableName, columnName)) {
                enumColumnConstraintManage.updateTableEnumColumnValuesForH2(
                        tableName, columnName, enumValuesStr);
                log.info(
                        "Update H2 table:{} enum column:{} to values:[{}] completed.",
                        tableName,
                        columnName,
                        enumValuesStr);
            }
        } catch (RuntimeException e) {
            log.error(
                    "Update H2 table:{} enum column:{} to values:[{}] failed. error:{}",
                    tableName,
                    columnName,
                    enumValuesStr,
                    e.getMessage());
        }
    }

    private List<String> getEnumNames(Class<?> enumClass) {
        List<String> enumNames = new ArrayList<>();
        for (Object enumValue : enumClass.getEnumConstants()) {
            Enum<?> enumConstant = (Enum<?>) enumValue;
            String enumName = enumConstant.name();
            enumNames.add(enumName);
        }
        return enumNames;
    }

    private Map<Class<?>, String> getTableNamesMap() {
        Map<Class<?>, String> tableClassNameMap = new HashMap<>();
        Metamodel metamodel = entityManagerFactory.getMetamodel();
        for (EntityType<?> entityType : metamodel.getEntities()) {
            Class<?> clazz = entityType.getJavaType();
            if (Objects.nonNull(clazz)
                    && Objects.nonNull(clazz.getAnnotation(Table.class).name())) {
                tableClassNameMap.put(clazz, clazz.getAnnotation(Table.class).name());
            }
        }
        return tableClassNameMap;
    }

    private List<Field> getFieldsWithAnnotationEnum(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        if (Objects.isNull(clazz)) {
            return fields;
        }
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Enumerated.class)) {
                fields.add(field);
            }
        }
        return fields;
    }
}
