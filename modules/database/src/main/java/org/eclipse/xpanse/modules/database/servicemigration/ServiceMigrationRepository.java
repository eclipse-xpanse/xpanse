/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicemigration;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Interface to access default JPA methods.
 */
public interface ServiceMigrationRepository extends JpaRepository<ServiceMigrationEntity, UUID>,
        JpaSpecificationExecutor<ServiceMigrationEntity> {

}
