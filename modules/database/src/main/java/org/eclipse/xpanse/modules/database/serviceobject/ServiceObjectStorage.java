/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceobject;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Interface for persist of ServiceObjectEntity. */
public interface ServiceObjectStorage {

    ServiceObjectEntity storeAndFlush(ServiceObjectEntity entity);

    void delete(ServiceObjectEntity entity);

    ServiceObjectEntity getEntityById(UUID objectId);

    List<ServiceObjectEntity> getEntitiesByIds(List<UUID> ids);

    Set<UUID> getObjectIdsByDependentObjectId(UUID dependentObjectId);

    List<ServiceObjectEntity> getObjectsByServiceId(UUID serviceId);
}
