/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceobject;

import java.util.Set;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.CustomJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Interface to access default JPA methods. */
@Repository
public interface ServiceObjectRepository
        extends CustomJpaRepository<ServiceObjectEntity, UUID>,
                JpaSpecificationExecutor<ServiceObjectEntity> {

    @Query(
            "SELECT s.objectId FROM ServiceObjectEntity s JOIN s.dependentObjectIds d WHERE d ="
                    + " :dependentObjectId")
    Set<UUID> findObjectIdsByDependentObjectId(@Param("dependentObjectId") UUID dependentObjectId);
}
