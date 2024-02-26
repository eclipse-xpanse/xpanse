/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.common.keystone;

import jakarta.annotation.Resource;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.deployment.utils.DeployEnvironments;
import org.eclipse.xpanse.modules.models.common.exceptions.XpanseUnhandledException;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialsNotFoundException;
import org.eclipse.xpanse.plugins.openstack.common.constants.OpenstackEnvironmentConstants;
import org.openstack4j.api.OSClient;
import org.openstack4j.core.transport.Config;
import org.openstack4j.core.transport.ProxyHost;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Class to encapsulate all calls to Keystone API.
 */
@Component
public class KeystoneManager {

    private final Environment environment;
    private final DeployEnvironments deployEnvironments;
    private final DeployServiceStorage deployServiceStorage;

    @Resource
    private ServiceTemplateStorage serviceTemplateStorage;

    /**
     * Constructor for KeystoneManager.
     *
     * @param environment          Environment Bean
     * @param deployEnvironments   DeployEnvironments Bean
     * @param deployServiceStorage DeployServiceStorage JPA
     */
    @Autowired
    public KeystoneManager(Environment environment, DeployEnvironments deployEnvironments,
                           DeployServiceStorage deployServiceStorage) {
        this.environment = environment;
        this.deployEnvironments = deployEnvironments;
        this.deployServiceStorage = deployServiceStorage;
    }

    private static String getIpAddressFromUrl(String url) {
        try {
            return InetAddress.getByName(URI.create(url).toURL().getHost()).getHostAddress();
        } catch (UnknownHostException | MalformedURLException e) {
            throw new XpanseUnhandledException(e.getMessage());
        }
    }

    /**
     * Authenticates and sets the authentication details in the thread context which can be
     * used for the further calls to Openstack API.
     *
     * @param credential Credential information available for Openstack in the runtime.
     */
    public void authenticate(UUID serviceId, AbstractCredentialInfo credential) {
        getAuthenticatedClient(serviceId, credential);
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
        String userDomain = null;
        String projectDomain = null;
        if (CredentialType.VARIABLES.toValue().equals(credential.getType().toValue())) {
            List<CredentialVariable> variables = ((CredentialVariables) credential).getVariables();
            for (CredentialVariable credentialVariable : variables) {
                if (OpenstackEnvironmentConstants.USERNAME.equals(
                        credentialVariable.getName())) {
                    userName = credentialVariable.getValue();
                }
                if (OpenstackEnvironmentConstants.PASSWORD.equals(
                        credentialVariable.getName())) {
                    password = credentialVariable.getValue();
                }
                if (OpenstackEnvironmentConstants.PROJECT.equals(
                        credentialVariable.getName())) {
                    tenant = credentialVariable.getValue();
                }
                if (OpenstackEnvironmentConstants.USER_DOMAIN.equals(
                        credentialVariable.getName())) {
                    userDomain = credentialVariable.getValue();
                }
                if (OpenstackEnvironmentConstants.PROJECT_DOMAIN.equals(
                        credentialVariable.getName())) {
                    projectDomain = credentialVariable.getValue();
                }
            }
        }
        if (Objects.isNull(userName) || Objects.isNull(password) || Objects.isNull(tenant)
                || Objects.isNull(userDomain)) {
            throw new CredentialsNotFoundException(
                    "Values for all openstack credential"
                            + " variables to connect to Openstack API is not found");
        }
        OSFactory.enableHttpLoggingFilter(true);
        // there is no need to return the authenticated client because the below method already sets
        // the authentication details in the thread context.
        String url = Objects.nonNull(serviceId) ? getUrlFromDeploymentVariables(serviceId)
                : this.environment.getProperty(OpenstackEnvironmentConstants.AUTH_URL);
        String serviceTenant =
                this.environment.getProperty(OpenstackEnvironmentConstants.SERVICE_PROJECT);
        String proxyHost = this.environment.getProperty(OpenstackEnvironmentConstants.PROXY_HOST);
        String proxyPort = this.environment.getProperty(OpenstackEnvironmentConstants.PROXY_PORT);
        String sslDisabled = this.environment.getProperty(
                OpenstackEnvironmentConstants.SSL_VERIFICATION_DISABLED);
        return OSFactory
                .builderV3()
                .withConfig(buildClientConfig(url, proxyHost, proxyPort, sslDisabled))
                .credentials(userName, password, Identifier.byName(userDomain))
                .scopeToProject(
                        Identifier.byName(Objects.isNull(serviceTenant) ? tenant : serviceTenant),
                        Identifier.byName(projectDomain))
                .endpoint(url)
                .authenticate();
    }

    private Config buildClientConfig(String url, String proxyHost, String proxyPort,
                                     String sslDisabled) {
        Config config = Config.newConfig()
                .withEndpointNATResolution(getIpAddressFromUrl(url))
                .withEndpointURLResolver(new CustomEndPointResolver())
                .withSSLVerificationDisabled()
                .withProxy(Objects.nonNull(proxyHost)
                        ? ProxyHost.of(proxyHost, Integer.parseInt(proxyPort)) : null);
        if (Objects.nonNull(sslDisabled) && sslDisabled.equals("true")) {
            config.withSSLVerificationDisabled();
        }
        return config;
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
        return (String) serviceRequestVariables.get(OpenstackEnvironmentConstants.AUTH_URL);
    }
}
