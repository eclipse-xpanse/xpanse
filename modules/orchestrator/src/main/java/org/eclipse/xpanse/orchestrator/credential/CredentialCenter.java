/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.credential;

import java.util.List;
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.orchestrator.OrchestratorService;
import org.springframework.stereotype.Component;

/**
 * The credential center.
 */
@Component
public class CredentialCenter {

    private final OrchestratorService orchestratorService;

    public CredentialCenter(
            OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    /**
     * Get the credential abilities of Csp.
     *
     * @param csp The csp to get.
     */
    List<AbstractCredentialInfo> getCredentialAbilities(Csp csp) {
        OrchestratorPlugin plugin = orchestratorService.getOrchestratorPlugin(csp);
        return plugin.getCredentialDefinitions();
    }

    /**
     * Create credential for the @Csp with @userName.
     *
     * @param csp        The csp  to create for.
     * @param userName   The userName to create for.
     * @param credential The credential to create.
     */
    boolean createCredential(Csp csp, String userName, AbstractCredentialInfo credential) {
        return false;
    }

    /**
     * Delete credential for the @Csp with @userName.
     *
     * @param csp      The csp  to delete.
     * @param userName The userName to delete.
     */
    boolean deleteCredential(Csp csp, String userName) {
        return false;
    }

    /**
     * Delete credential for the @Csp with @userName.
     *
     * @param csp      The csp  to delete.
     * @param userName The userName to delete.
     */
    boolean deleteCredentialByType(Csp csp, String userName, CredentialType credentialType) {
        return false;
    }

    /**
     * Get credential for the @Csp with @userName.
     *
     * @param csp      The csp  to get.
     * @param userName The userName to get.
     */
    public AbstractCredentialInfo getCredential(Csp csp, String userName) {
        return null;
    }

    /**
     * Get credential for the @Csp with @userName.
     *
     * @param csp      The csp  to get.
     * @param userName The userName to get.
     */
    AbstractCredentialInfo getCredentialByType(Csp csp, String userName,
                                               CredentialType credentialType) {
        return null;
    }
}
