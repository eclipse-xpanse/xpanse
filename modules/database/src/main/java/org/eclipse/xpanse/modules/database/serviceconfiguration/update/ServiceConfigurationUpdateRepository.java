/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration.update;

import java.util.UUID;
import org.eclipse.xpanse.modules.database.CustomJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Interface to access default JPA methods.
 */
public interface ServiceConfigurationUpdateRepository extends
        CustomJpaRepository<ServiceConfigurationUpdateRequest, UUID>,
        JpaSpecificationExecutor<ServiceConfigurationUpdateRequest> {

}
