/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Scs Inc.
 *
 */

package org.eclipse.xpanse.plugins.openstack.common.auth.keystone;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.eclipse.xpanse.common.proxy.ProxyConfigurationManager;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialsNotFoundException;
import org.eclipse.xpanse.plugins.openstack.common.auth.constants.OpenstackCommonEnvironmentConstants;
import org.openstack4j.api.OSClient;
import org.openstack4j.core.transport.Config;
import org.openstack4j.core.transport.ProxyHost;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Bean to manage keystone based authentication to SCS APIs.
 */
@Component
public class ScsKeystoneManager {

    private final ProxyConfigurationManager proxyConfigurationManager;

    @Autowired
    public ScsKeystoneManager(ProxyConfigurationManager proxyConfigurationManager) {
        this.proxyConfigurationManager = proxyConfigurationManager;
    }

    /**
     * Get the Openstack API client based on the credential information.
     *
     * @param credential Credential information available for Openstack in the runtime.
     */
    public OSClient.OSClientV3 getAuthenticatedClient(String authUrl,
                                                      AbstractCredentialInfo credential) {

        String userName = null;
        String password = null;
        String tenant = null;
        String domain = null;
        if (CredentialType.VARIABLES.toValue().equals(credential.getType().toValue())) {
            List<CredentialVariable> variables = ((CredentialVariables) credential).getVariables();
            for (CredentialVariable credentialVariable : variables) {
                if (OpenstackCommonEnvironmentConstants.USERNAME.equals(
                        credentialVariable.getName())) {
                    userName = credentialVariable.getValue();
                }
                if (OpenstackCommonEnvironmentConstants.PASSWORD.equals(
                        credentialVariable.getName())) {
                    password = credentialVariable.getValue();
                }
                if (OpenstackCommonEnvironmentConstants.PROJECT.equals(
                        credentialVariable.getName())) {
                    tenant = credentialVariable.getValue();
                }
                if (OpenstackCommonEnvironmentConstants.DOMAIN.equals(
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
        return OSFactory
                .builderV3()
                .credentials(userName, password, Identifier.byName(domain))
                .scopeToProject(
                        Identifier.byName(tenant),
                        Identifier.byName(domain))
                .endpoint(authUrl)
                .withConfig(createProxyConfig(authUrl))
                .authenticate();
    }

    private Config createProxyConfig(String url) {
        Config config = null;
        if (Objects.nonNull(proxyConfigurationManager.getHttpsProxyDetails())
                || Objects.nonNull(proxyConfigurationManager.getHttpProxyDetails())) {
            URI uri = URI.create(url);
            if ("http".equalsIgnoreCase(uri.getScheme())
                    && Objects.nonNull(proxyConfigurationManager.getHttpProxyDetails())) {
                config = Config.newConfig()
                        .withProxy(ProxyHost.of(
                                // bug in openstack4J. It expects full URL for the host argument.
                                proxyConfigurationManager.getHttpProxyDetails().getProxyUrl(),
                                proxyConfigurationManager.getHttpProxyDetails().getProxyPort(),
                                proxyConfigurationManager.getHttpProxyDetails().getProxyUsername(),
                                proxyConfigurationManager.getHttpProxyDetails()
                                        .getProxyPassword()));
            }
            if ("https".equalsIgnoreCase(uri.getScheme())
                    && Objects.nonNull(proxyConfigurationManager.getHttpsProxyDetails())) {
                config = Config.newConfig()
                        .withProxy(ProxyHost.of(
                                // bug in openstack4J. It expects full URL for the host argument.
                                // bug in openstack4J. It expects full URL for the host argument.
                                proxyConfigurationManager.getHttpsProxyDetails().getProxyUrl(),
                                proxyConfigurationManager.getHttpsProxyDetails().getProxyPort(),
                                proxyConfigurationManager.getHttpsProxyDetails().getProxyUsername(),
                                proxyConfigurationManager.getHttpsProxyDetails()
                                        .getProxyPassword()));
            }
        }
        if (Objects.isNull(config)) {
            config = Config.newConfig();
        }
        return config;
    }
}
