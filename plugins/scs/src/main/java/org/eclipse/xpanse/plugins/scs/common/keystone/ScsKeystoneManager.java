/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Scs Inc.
 *
 */

package org.eclipse.xpanse.plugins.scs.common.keystone;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialsNotFoundException;
import org.eclipse.xpanse.plugins.scs.common.constants.ScsEnvironmentConstants;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Bean to manage keystone based authentication to SCS APIs.
 */
@Component
public class ScsKeystoneManager {

    private final DeployEnvironments deployEnvironments;
    private final DeployServiceStorage deployServiceStorage;

    @Resource
    private ServiceTemplateStorage serviceTemplateStorage;

    /**
     * Constructor for ScsKeystoneManager.
     *
     * @param deployEnvironments   DeployEnvironments Bean
     * @param deployServiceStorage DeployServiceStorage JPA
     */
    @Autowired
    public ScsKeystoneManager(DeployEnvironments deployEnvironments,
                              DeployServiceStorage deployServiceStorage) {
        this.deployEnvironments = deployEnvironments;
        this.deployServiceStorage = deployServiceStorage;
    }

    /**
     * Get the Openstack API client based on the credential information.
     *
     * @param credential Credential information available for Openstack in the runtime.
     */
    public OSClient.OSClientV3 getAuthenticatedClient(UUID serviceId,
                                                      AbstractCredentialInfo credential) {

        String userName = null;
        String password = null;
        String tenant = null;
        String domain = null;
        if (CredentialType.VARIABLES.toValue().equals(credential.getType().toValue())) {
            List<CredentialVariable> variables = ((CredentialVariables) credential).getVariables();
            for (CredentialVariable credentialVariable : variables) {
                if (ScsEnvironmentConstants.USERNAME.equals(
                        credentialVariable.getName())) {
                    userName = credentialVariable.getValue();
                }
                if (ScsEnvironmentConstants.PASSWORD.equals(
                        credentialVariable.getName())) {
                    password = credentialVariable.getValue();
                }
                if (ScsEnvironmentConstants.PROJECT.equals(
                        credentialVariable.getName())) {
                    tenant = credentialVariable.getValue();
                }
                if (ScsEnvironmentConstants.DOMAIN.equals(
                        credentialVariable.getName())) {
                    domain = credentialVariable.getValue();
                }
            }
        }
        if (Objects.isNull(userName) || Objects.isNull(password) || Objects.isNull(tenant)
                || Objects.isNull(domain)) {
            throw new CredentialsNotFoundException(
                    "Values for all openstack credential"
                            + " variables to connect to Openstack API is not found");
        }
        OSFactory.enableHttpLoggingFilter(true);
        // there is no need to return the authenticated client because the below method already sets
        // the authentication details in the thread context.
        String url = Objects.nonNull(serviceId) ? getUrlFromDeploymentVariables(serviceId)
                : System.getenv(ScsEnvironmentConstants.AUTH_URL);

        return OSFactory
                .builderV3()
                .credentials(userName, password, Identifier.byName(domain))
                .scopeToProject(
                        Identifier.byName(tenant),
                        Identifier.byName(domain))
                .endpoint(url)
                .authenticate();
    }


    private String getUrlFromDeploymentVariables(UUID serviceId) {
        DeployServiceEntity deployServiceEntity = deployServiceStorage.findDeployServiceById(
                serviceId);
        ServiceTemplateEntity serviceTemplateEntity = serviceTemplateStorage.getServiceTemplateById(
                deployServiceEntity.getServiceTemplateId());
        Map<String, Object> serviceRequestVariables =
                this.deployEnvironments.getAllDeploymentVariablesForService(
                        deployServiceEntity.getDeployRequest().getServiceRequestProperties(),
                        serviceTemplateEntity.getOcl().getDeployment()
                                .getVariables(),
                        deployServiceEntity.getFlavor(),
                        serviceTemplateEntity.getOcl()
                );
        return (String) serviceRequestVariables.get(ScsEnvironmentConstants.AUTH_URL);
    }
}
