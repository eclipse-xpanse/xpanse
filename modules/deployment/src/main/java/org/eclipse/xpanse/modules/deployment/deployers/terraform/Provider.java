/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform;

import org.eclipse.xpanse.modules.models.enums.Csp;

/**
 * Interface of terraform provider.
 */
public interface Provider {

    String getProvider();

    Csp getCsp();

}
