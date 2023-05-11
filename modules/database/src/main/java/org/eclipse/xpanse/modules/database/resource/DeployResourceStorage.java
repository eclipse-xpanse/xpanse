/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.resource;

import java.util.UUID;

/**
 * Interface for persist of DeployResource.
 */
public interface DeployResourceStorage {

    void deleteByDeployServiceId(UUID id);
}
