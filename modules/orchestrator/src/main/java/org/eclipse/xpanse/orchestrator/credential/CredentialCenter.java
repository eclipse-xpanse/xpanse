/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.credential;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.credential.CredentialDefinition;
import org.eclipse.xpanse.modules.credential.CredentialVariable;
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

    private final Map<Csp, AbstractCredentialInfo> credentialInfoMap =
            new ConcurrentHashMap<>();

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
        AbstractCredentialInfo abstractCredentialInfo = credentialInfoMap.get(csp);
        if (abstractCredentialInfo != null) {
            return abstractCredentialInfo;
        }
        AbstractCredentialInfo credentialInfo = findCredentialInfoFromEnv(csp);
        if (credentialInfo == null) {
            throw new IllegalStateException(
                    String.format(
                            "No credential information found for the given Csp:%s.",
                            csp));
        }
        credentialInfoMap.put(csp, credentialInfo);
        return credentialInfo;
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

    /**
     * Get credentialInfo from the environment using Csp.
     *
     * @param csp The csp to get.
     */
    private AbstractCredentialInfo findCredentialInfoFromEnv(Csp csp) {
        List<AbstractCredentialInfo> credentialAbilities = getCredentialAbilities(csp);
        for (AbstractCredentialInfo credentialAbility : credentialAbilities) {
            CredentialDefinition credentialDefinition = (CredentialDefinition) credentialAbility;
            List<CredentialVariable> variables = credentialDefinition.getVariables();
            int credentialVariableSetValueCount = 0;
            for (CredentialVariable variable : variables) {
                String envValue = System.getenv(variable.getName());
                if (StringUtils.isNotBlank(envValue)) {
                    variable.setValue(envValue);
                    credentialVariableSetValueCount++;
                }
            }
            // Check if all variables have been successfully set.
            if (credentialVariableSetValueCount == variables.size()) {
                return credentialAbility;
            }
        }
        return null;
    }

}
