/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceorder;

import java.util.UUID;
import org.eclipse.xpanse.modules.database.CustomJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Interface to access default JPA methods.
 */
@Repository
public interface ServiceOrderRepository
        extends CustomJpaRepository<ServiceOrderEntity, UUID>,
        JpaSpecificationExecutor<ServiceOrderEntity> {

}
