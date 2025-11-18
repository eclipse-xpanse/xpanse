/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.common;

import static org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudConstants.HW_ACCESS_KEY;
import static org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudConstants.HW_SECRET_KEY;

import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.GlobalCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.iam.v3.region.IamRegion;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.credential.exceptions.CredentialsNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** HuaweiCloud Credentials Class. */
@Slf4j
@Component
public class HuaweiCloudCredentials {

    private final CredentialCenter credentialCenter;

    /** Constructor method. */
    @Autowired
    public HuaweiCloudCredentials(CredentialCenter credentialCenter) {
        this.credentialCenter = credentialCenter;
    }

    /**
     * Get Basic Credential For Huawei Cloud Client.
     *
     * @param siteName site name.
     * @param regionName region name.
     * @param userId user id.
     */
    public ICredential getBasicCredential(String siteName, String regionName, String userId) {

        AbstractCredentialInfo credential =
                credentialCenter.getCredential(
                        Csp.HUAWEI_CLOUD, siteName, CredentialType.VARIABLES, userId);
        Map<String, String> akSkMap = getCredentialVariablesMap((CredentialVariables) credential);
        if (StringUtils.isNotBlank(regionName)) {
            return new BasicCredentials()
                    .withAk(akSkMap.get(HW_ACCESS_KEY))
                    .withSk(akSkMap.get(HW_SECRET_KEY))
                    .withIamEndpoint(IamRegion.valueOf(regionName).getEndpoints().getFirst());
        }
        return new BasicCredentials()
                .withAk(akSkMap.get(HW_ACCESS_KEY))
                .withSk(akSkMap.get(HW_SECRET_KEY));
    }

    /** Get Global Credential For Huawei Cloud Client. */
    public ICredential getGlobalCredential(String site, String region, String userId) {
        AbstractCredentialInfo credential =
                credentialCenter.getCredential(
                        Csp.HUAWEI_CLOUD, site, CredentialType.VARIABLES, userId);
        Map<String, String> akSkMap = getCredentialVariablesMap((CredentialVariables) credential);
        if (StringUtils.isNotBlank(region)) {
            return new GlobalCredentials()
                    .withAk(akSkMap.get(HW_ACCESS_KEY))
                    .withSk(akSkMap.get(HW_SECRET_KEY))
                    .withIamEndpoint(IamRegion.valueOf(region).getEndpoints().getFirst());
        }
        return new GlobalCredentials()
                .withAk(akSkMap.get(HW_ACCESS_KEY))
                .withSk(akSkMap.get(HW_SECRET_KEY));
    }

    /**
     * Get AK/SK/ProjectId from CredentialVariables.
     *
     * @param credentialVariables object of CredentialVariables.
     * @return map of AK/SK/ProjectId.
     */
    private Map<String, String> getCredentialVariablesMap(CredentialVariables credentialVariables) {
        Map<String, String> credentialVariablesMap = new HashMap<>();
        if (CredentialType.VARIABLES.toValue().equals(credentialVariables.getType().toValue())) {
            List<CredentialVariable> variables = credentialVariables.getVariables();
            for (CredentialVariable credentialVariable : variables) {
                credentialVariablesMap.put(
                        credentialVariable.getName(), credentialVariable.getValue());
            }
        }
        if (StringUtils.isBlank(credentialVariablesMap.get(HW_ACCESS_KEY))) {
            throw new CredentialsNotFoundException("Get ICredential error, AccessKey is empty.");
        }
        if (StringUtils.isBlank(credentialVariablesMap.get(HW_SECRET_KEY))) {
            throw new CredentialsNotFoundException("Get ICredential error, SecretKey is empty.");
        }
        return credentialVariablesMap;
    }
}
