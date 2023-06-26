/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.monitor.keystone;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
import org.eclipse.xpanse.modules.models.common.exceptions.XpanseUnhandledException;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialsNotFoundException;
import org.eclipse.xpanse.plugins.openstack.constants.OpenstackEnvironmentConstants;
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

    @Autowired
    public KeystoneManager(Environment environment) {
        this.environment = environment;
    }

    /**
     * Authenticates and sets the authentication details in the thread context which can be
     * used for the further calls to Openstack API.
     *
     * @param credential Credential information available for Openstack in the runtime.
     */
    public void authenticate(AbstractCredentialInfo credential) {
        String userName = null;
        String password = null;
        String tenant = null;
        String domain = null;
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
                if (OpenstackEnvironmentConstants.DOMAIN.equals(
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
        String url = this.environment.getRequiredProperty(OpenstackEnvironmentConstants.AUTH_URL);
        String serviceTenant =
                this.environment.getProperty(OpenstackEnvironmentConstants.SERVICE_PROJECT);
        String proxyHost = this.environment.getProperty(OpenstackEnvironmentConstants.PROXY_HOST);
        String proxyPort = this.environment.getProperty(OpenstackEnvironmentConstants.PROXY_PORT);
        OSFactory
                .builderV3()
                .withConfig(buildClientConfig(url, proxyHost, proxyPort))
                .credentials(userName, password, Identifier.byName(domain))
                .scopeToProject(
                        Identifier.byName(Objects.isNull(serviceTenant) ? tenant : serviceTenant),
                        Identifier.byName(domain))
                .endpoint(url)
                .authenticate();
    }

    private static String getIpAddressFromUrl(String url) {
        try {
            return InetAddress.getByName(new URL(url).getHost()).getHostAddress();
        } catch (UnknownHostException | MalformedURLException e) {
            throw new XpanseUnhandledException(e.getMessage());
        }
    }

    private Config buildClientConfig(String url, String proxyHost, String proxyPort) {
        return Config.newConfig()
                .withEndpointNATResolution(getIpAddressFromUrl(url))
                .withEndpointURLResolver(new CustomEndPointResolver())
                .withProxy(Objects.nonNull(proxyHost)
                        ? ProxyHost.of(proxyHost, Integer.parseInt(proxyPort)) : null);
    }
}
