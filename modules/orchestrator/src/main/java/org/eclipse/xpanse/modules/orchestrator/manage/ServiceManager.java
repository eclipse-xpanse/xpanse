/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.manage;

/**
 * Service management interface.
 */
public interface ServiceManager {

    boolean startService(ServiceManagerRequest serviceManagerRequest);

    boolean stopService(ServiceManagerRequest serviceManagerRequest);

    boolean restartService(ServiceManagerRequest serviceManagerRequest);
}
