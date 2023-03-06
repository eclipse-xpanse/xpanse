/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.service;

import java.util.List;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;

/**
 * Interface for persist of DeployService.
 */
public interface DeployServiceStorage {

    void store(DeployServiceEntity deployServiceEntity);

    List<DeployServiceEntity> services();
}
