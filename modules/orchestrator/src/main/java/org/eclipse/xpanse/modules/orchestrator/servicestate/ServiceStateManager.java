/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.servicestate;

/** Service management interface. */
public interface ServiceStateManager {

    boolean startService(ServiceStateManageRequest serviceStateManageRequest);

    boolean stopService(ServiceStateManageRequest serviceStateManageRequest);

    boolean restartService(ServiceStateManageRequest serviceStateManageRequest);
}
