/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.common;

import java.security.Principal;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Class to hold the ID of the user.
 */
@Data
@RequiredArgsConstructor
public class SimplePrincipal implements Principal {
    // in xpanse this is the ID of the user.
    private final String name;
}