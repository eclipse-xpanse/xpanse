/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.common;

import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialsNotFoundException;
import org.springframework.stereotype.Component;

/** HuaweiCloud BasicCredentials Class. */
@Slf4j
@Component
public class FlexibleEngineCredentials {

    /**
     * Get Credential for Client.
     *
     * @param credentialVariables credential variables.
     * @return credential
     */
    public ICredential getCredential(AbstractCredentialInfo credentialVariables) {
        String accessKey = null;
        String securityKey = null;
        if (CredentialType.VARIABLES.toValue().equals(credentialVariables.getType().toValue())) {
            List<CredentialVariable> variables =
                    ((CredentialVariables) credentialVariables).getVariables();
            for (CredentialVariable credentialVariable : variables) {
                if (FlexibleEngineConstants.OS_ACCESS_KEY.equals(credentialVariable.getName())) {
                    accessKey = credentialVariable.getValue();
                }
                if (FlexibleEngineConstants.OS_SECRET_KEY.equals(credentialVariable.getName())) {
                    securityKey = credentialVariable.getValue();
                }
            }
        }
        if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(securityKey)) {
            log.error(
                    "Get Credential For Client error,accessKey:{},securityKey:{}",
                    accessKey,
                    securityKey);
            throw new CredentialsNotFoundException("Get ICredential error,AK or SK is empty");
        }
        return new BasicCredentials().withAk(accessKey).withSk(securityKey);
    }
}
