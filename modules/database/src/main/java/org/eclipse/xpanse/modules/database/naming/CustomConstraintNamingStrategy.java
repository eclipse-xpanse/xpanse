/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.database.naming;

import java.util.stream.Collectors;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitForeignKeyNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.model.naming.ImplicitUniqueKeyNameSource;

/**
 * This class controls how the constraints are named when hibernate creates them automatically using
 * hbm-ddl feature.
 */
public class CustomConstraintNamingStrategy extends ImplicitNamingStrategyJpaCompliantImpl {

    @Override
    public Identifier determineForeignKeyName(ImplicitForeignKeyNameSource source) {
        // Generate a foreign key name based on the child and parent table.
        String baseName =
                "FK_"
                        + source.getReferencedTableName().getCanonicalName().toUpperCase()
                        + "_"
                        + source.getTableName().getCanonicalName().toUpperCase();
        return Identifier.toIdentifier(baseName);
    }

    @Override
    public Identifier determineUniqueKeyName(ImplicitUniqueKeyNameSource source) {
        // Generate a unique key based on the columns involved in the key.
        String uniqueKeyName =
                "UK_"
                        + source.getColumnNames().stream()
                                .map(Identifier::getCanonicalName)
                                .map(String::toUpperCase)
                                .collect(Collectors.joining("_"));
        return Identifier.toIdentifier(uniqueKeyName);
    }
}
