/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.terraform.deployment;

import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.Csp;

/**
 * Interface of terraform provider.
 */
public interface Provider {

    public String getProvider();

    public Csp getCsp();

}
