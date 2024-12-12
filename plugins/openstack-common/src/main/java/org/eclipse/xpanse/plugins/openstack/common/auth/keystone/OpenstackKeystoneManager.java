/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.common.auth.keystone;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
import org.eclipse.xpanse.common.proxy.ProxyConfigurationManager;
import org.eclipse.xpanse.modules.models.common.exceptions.XpanseUnhandledException;
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
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Class to encapsulate all calls to Keystone API. */
@Component
public class OpenstackKeystoneManager {

    private final Environment environment;
    private final ProxyConfigurationManager proxyConfigurationManager;

    /**
     * Constructor for OpenstackKeystoneManager.
     *
     * @param environment Environment Bean
     */
    @Autowired
    public OpenstackKeystoneManager(
            Environment environment, ProxyConfigurationManager proxyConfigurationManager) {
        this.environment = environment;
        this.proxyConfigurationManager = proxyConfigurationManager;
    }

    private String getIpAddressFromUrl(String url) {
        try {
            return InetAddress.getByName(URI.create(url).toURL().getHost()).getHostAddress();
        } catch (UnknownHostException | MalformedURLException e) {
            throw new XpanseUnhandledException(e.getMessage());
        }
    }

    /**
     * Get the Openstack API client based on the credential information.
     *
     * @param authUrl Authentication URL
     * @param credential Credential information available for Openstack in the runtime.
     */
    public OSClient.OSClientV3 getAuthenticatedClient(
            String authUrl, AbstractCredentialInfo credential) {

        String userName = null;
        String password = null;
        String tenant = null;
        String userDomain = null;
        String projectDomain = null;
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
                if (OpenstackCommonEnvironmentConstants.USER_DOMAIN.equals(
                        credentialVariable.getName())) {
                    userDomain = credentialVariable.getValue();
                }
                if (OpenstackCommonEnvironmentConstants.PROJECT_DOMAIN.equals(
                        credentialVariable.getName())) {
                    projectDomain = credentialVariable.getValue();
                }
            }
        }
        if (Objects.isNull(userName)
                || Objects.isNull(password)
                || Objects.isNull(tenant)
                || Objects.isNull(userDomain)) {
            throw new CredentialsNotFoundException(
                    "Values for all openstack credential"
                            + " variables to connect to Openstack API is not found");
        }
        OSFactory.enableHttpLoggingFilter(true);
        // there is no need to return the authenticated client because the below method already sets
        // the authentication details in the thread context.
        String serviceTenant =
                this.environment.getProperty(OpenstackCommonEnvironmentConstants.SERVICE_PROJECT);
        String sslDisabled =
                this.environment.getProperty(
                        OpenstackCommonEnvironmentConstants.SSL_VERIFICATION_DISABLED);
        return OSFactory.builderV3()
                .withConfig(buildClientConfig(authUrl, sslDisabled))
                .credentials(userName, password, Identifier.byName(userDomain))
                .scopeToProject(
                        Identifier.byName(Objects.isNull(serviceTenant) ? tenant : serviceTenant),
                        Identifier.byName(projectDomain))
                .endpoint(authUrl)
                .authenticate();
    }

    private Config buildClientConfig(String url, String sslDisabled) {
        Config config = null;
        if (Objects.nonNull(proxyConfigurationManager.getHttpsProxyDetails())
                || Objects.nonNull(proxyConfigurationManager.getHttpProxyDetails())) {
            URI uri = URI.create(url);
            if ("http".equalsIgnoreCase(uri.getScheme())
                    && Objects.nonNull(proxyConfigurationManager.getHttpProxyDetails())) {
                config =
                        Config.newConfig()
                                .withEndpointNATResolution(getIpAddressFromUrl(url))
                                .withEndpointURLResolver(new CustomEndPointResolver())
                                .withProxy(
                                        ProxyHost.of(
                                                // bug in openstack4J. It expects full URL for the
                                                // host argument.
                                                proxyConfigurationManager
                                                        .getHttpProxyDetails()
                                                        .getProxyUrl(),
                                                proxyConfigurationManager
                                                        .getHttpProxyDetails()
                                                        .getProxyPort(),
                                                proxyConfigurationManager
                                                        .getHttpProxyDetails()
                                                        .getProxyUsername(),
                                                proxyConfigurationManager
                                                        .getHttpProxyDetails()
                                                        .getProxyPassword()));
            }
            if ("https".equalsIgnoreCase(uri.getScheme())
                    && Objects.nonNull(proxyConfigurationManager.getHttpsProxyDetails())) {
                config =
                        Config.newConfig()
                                .withEndpointNATResolution(getIpAddressFromUrl(url))
                                .withEndpointURLResolver(new CustomEndPointResolver())
                                .withProxy(
                                        ProxyHost.of(
                                                // bug in openstack4J. It expects full URL for the
                                                // host argument.
                                                proxyConfigurationManager
                                                        .getHttpsProxyDetails()
                                                        .getProxyUrl(),
                                                proxyConfigurationManager
                                                        .getHttpsProxyDetails()
                                                        .getProxyPort(),
                                                proxyConfigurationManager
                                                        .getHttpsProxyDetails()
                                                        .getProxyUsername(),
                                                proxyConfigurationManager
                                                        .getHttpsProxyDetails()
                                                        .getProxyPassword()));
            }
        }
        if (Objects.isNull(config)) {
            config =
                    Config.newConfig()
                            .withEndpointNATResolution(getIpAddressFromUrl(url))
                            .withEndpointURLResolver(new CustomEndPointResolver());
        }
        if (Objects.nonNull(sslDisabled) && sslDisabled.equals("true")) {
            config.withSSLVerificationDisabled();
        }
        return config;
    }
}
