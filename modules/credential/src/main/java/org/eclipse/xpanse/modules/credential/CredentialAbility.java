/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential;

import java.util.List;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;

/**
 * Credential Ability of the special plugin for the CSP.
 */
public interface CredentialAbility {

    /**
     * Get credential abilities.
     */
    List<CredentialType> getCredentialAbilities();

    /**
     * Get credential definitions.
     */
    List<Credential> getCredentials();
}
