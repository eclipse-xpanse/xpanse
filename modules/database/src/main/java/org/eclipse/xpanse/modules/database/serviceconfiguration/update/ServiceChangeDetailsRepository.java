/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration.update;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.CustomJpaRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.lang.Nullable;

/** Interface to access default JPA methods. */
public interface ServiceChangeDetailsRepository
        extends CustomJpaRepository<ServiceChangeDetailsEntity, UUID>,
                JpaSpecificationExecutor<ServiceChangeDetailsEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // it is not possible to read this config from spring properties.
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "2000")})
    @Override
    List<ServiceChangeDetailsEntity> findAll(
            @Nullable Specification<ServiceChangeDetailsEntity> spec);
}
