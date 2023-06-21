/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.credential;

import java.util.List;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;

/**
 * Interface to be implemented by CSP plugins to define its authentication mechanisms.
 */
public interface AuthenticationCapabilities {

    /**
     * Get the available credential types.
     */
    List<CredentialType> getAvailableCredentialTypes();

    /**
     * Get the credential definitions.
     */
    List<AbstractCredentialInfo> getCredentialDefinitions();
}
