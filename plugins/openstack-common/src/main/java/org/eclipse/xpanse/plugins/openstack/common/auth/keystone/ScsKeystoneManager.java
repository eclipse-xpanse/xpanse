/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Scs Inc.
 *
 */

package org.eclipse.xpanse.plugins.openstack.common.auth.keystone;

import java.util.List;
import java.util.Objects;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialsNotFoundException;
import org.eclipse.xpanse.plugins.openstack.common.auth.constants.OpenstackCommonEnvironmentConstants;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;
import org.springframework.stereotype.Component;

/**
 * Bean to manage keystone based authentication to SCS APIs.
 */
@Component
public class ScsKeystoneManager {

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
                .authenticate();
    }
}
