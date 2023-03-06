/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.service;

import java.util.List;
import org.eclipse.xpanse.modules.database.service.DeployResourceEntity;

/**
 * Interface for persist of DeployResource.
 */
public interface DeployResourceStorage {

    void store(DeployResourceEntity deployResourceEntity);

    List<DeployResourceEntity> services();
}
