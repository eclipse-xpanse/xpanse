/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.service;

import java.util.UUID;

/**
 * Interface for persist of DeployResource.
 */
public interface DeployResourceStorage {

    void deleteByDeployServiceId(UUID id);
}
