/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.credential;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.credential.CreateCredential;
import org.eclipse.xpanse.modules.credential.CredentialDefinition;
import org.eclipse.xpanse.modules.credential.CredentialVariable;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.orchestrator.OrchestratorPlugin;
import org.eclipse.xpanse.orchestrator.OrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The credential center.
 */
@Component
public class CredentialCenter {

    private final OrchestratorService orchestratorService;
    private final DeployServiceStorage deployServiceStorage;

    /**
     * Constructor of CredentialCenter.
     */
    @Autowired
    public CredentialCenter(
            OrchestratorService orchestratorService,
            DeployServiceStorage deployServiceStorage) {
        this.orchestratorService = orchestratorService;
        this.deployServiceStorage = deployServiceStorage;
    }

    /**
     * List the credential capabilities by @Csp and @type.
     *
     * @param csp  The cloud service provider.
     * @param type The type of credential.
     * @return Returns list of credential capabilities.
     */
    public List<AbstractCredentialInfo> getCredentialCapabilitiesByCsp(Csp csp,
                                                                       CredentialType type) {
        OrchestratorPlugin plugin = orchestratorService.getOrchestratorPlugin(csp);
        if (Objects.isNull(type)) {
            return plugin.getCredentialDefinitions();
        }
        return plugin.getCredentialDefinitions().stream().filter(credential ->
                        Objects.equals(credential.getType(), type))
                .collect(Collectors.toList());
    }

    /**
     * List the credential capabilities by the @id and @type.
     *
     * @param id   The UUID of the deployed service.
     * @param type The type of credential.
     * @return Returns list of credential capabilities.
     */
    public List<AbstractCredentialInfo> getCredentialCapabilitiesByServiceId(String id,
                                                                             CredentialType type) {
        DeployServiceEntity deployServiceEntity = getDeployServiceEntityById(id);
        return getCredentialCapabilitiesByCsp(deployServiceEntity.getCsp(), type);
    }

    /**
     * List the credential info of the @csp and @userName and @type.
     *
     * @param csp      The cloud service provider.
     * @param userName The name of user who provided the credential info.
     * @param type     The type of the credential.
     * @return the credentials.
     */
    public List<CredentialDefinition> getCredentialDefinitionsByCsp(Csp csp, String userName,
                                                                    CredentialType type) {
        return null;
    }

    /**
     * List credentials of the service.
     *
     * @param id   The id of deployed service.
     * @param type The type of credential.
     * @return Returns credential capabilities list of the service.
     */
    public List<CredentialDefinition> getCredentialDefinitionsByServiceId(String id,
                                                                          CredentialType type) {
        DeployServiceEntity deployServiceEntity = getDeployServiceEntityById(id);
        return getCredentialDefinitionsByCsp(deployServiceEntity.getCsp(),
                deployServiceEntity.getUserName(), type);
    }


    /**
     * Add credential info for the csp.
     *
     * @param csp              The cloud service provider.
     * @param createCredential The credential for the service.
     * @return Deployment bean for the provided deployerKind.
     */
    public boolean addCredential(Csp csp, CreateCredential createCredential) {
        return true;
    }

    /**
     * Add credential info for the service.
     *
     * @param id               The UUID of the deployed service.
     * @param createCredential The credential for the service.
     * @return true of false.
     */
    public boolean addCredentialByServiceId(String id, CreateCredential createCredential) {
        DeployServiceEntity deployServiceEntity = getDeployServiceEntityById(id);
        return true;
    }


    /**
     * Update credential info for the service.
     *
     * @param csp              The cloud service provider.
     * @param updateCredential The credential for the service.
     * @return true of false.
     */
    public boolean updateCredential(Csp csp, CreateCredential updateCredential) {
        return true;
    }

    /**
     * Update credential info for the service.
     *
     * @param id               The UUID of the deployed service.
     * @param updateCredential The credential for the service.
     * @return true of false.
     */
    public boolean updateCredentialByServiceId(String id, CreateCredential updateCredential) {
        DeployServiceEntity deployServiceEntity = getDeployServiceEntityById(id);
        return true;
    }


    /**
     * Delete credential for the @Csp with @userName.
     *
     * @param csp      The cloud service provider.
     * @param userName The userName to delete.
     * @return true of false.
     */
    public boolean deleteCredential(Csp csp, String userName) {
        return true;
    }

    /**
     * Delete credential info of the service.
     *
     * @param id   The UUID of the deployed service.
     * @param type The type of credential.
     * @return true of false.
     */
    public boolean deleteCredentialByServiceId(String id, CredentialType type) {
        DeployServiceEntity deployServiceEntity = getDeployServiceEntityById(id);
        return true;
    }

    /**
     * Delete credential for the @Csp with @userName.
     *
     * @param csp      The cloud service provider.
     * @param userName The userName to delete.
     */
    public boolean deleteCredentialByType(Csp csp, String userName, CredentialType
            credentialType) {
        return false;
    }

    /**
     * Get credential for the @Csp with @userName.
     *
     * @param csp      The cloud service provider.
     * @param userName The userName to get.
     */
    public CredentialDefinition getCredential(Csp csp, String userName) {
        AbstractCredentialInfo credentialInfo = findCredentialInfoFromEnv(csp);
        if (Objects.isNull(credentialInfo)) {
            throw new IllegalStateException(
                    String.format("No credential information found for the given Csp:%s.", csp));
        }
        return (CredentialDefinition) credentialInfo;
    }

    /**
     * Get deployed service by id.
     *
     * @param id The id of the deployed service.
     * @return Returns DB entity of the deployed service.
     */
    private DeployServiceEntity getDeployServiceEntityById(String id) {
        DeployServiceEntity deployServiceEntity =
                deployServiceStorage.findDeployServiceById(UUID.fromString(id));
        if (Objects.isNull(deployServiceEntity)) {
            throw new RuntimeException(String.format("Deployed service with id %s not found", id));
        }
        return deployServiceEntity;
    }

    /**
     * Get credentialInfo from the environment using @Csp.
     *
     * @param csp The cloud service provider.
     * @return Returns credentialInfo.
     */
    private AbstractCredentialInfo findCredentialInfoFromEnv(Csp csp) {
        List<AbstractCredentialInfo> credentialAbilities =
                getCredentialCapabilitiesByCsp(csp, null);
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
