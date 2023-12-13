/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.monitor.models;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.huaweicloud.sdk.core.http.HttpMethod;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.http.client.methods.HttpRequestBase;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.plugins.flexibleengine.FlexibleEngineClient;
import org.eclipse.xpanse.plugins.flexibleengine.models.constant.FlexibleEngineConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlexibleEngineClientTest {

    private FlexibleEngineClient flexibleEngineClientUnderTest;

    @BeforeEach
    void setUp() {
        flexibleEngineClientUnderTest = new FlexibleEngineClient();
    }

    @Test
    void testBuildGetRequest() throws Exception {
        // Setup
        final AbstractCredentialInfo credential = getCredentialDefinition();

        // Run the test
        final HttpRequestBase result =
                flexibleEngineClientUnderTest.buildGetRequest(credential, "url");

        // Verify the results
        Assertions.assertFalse(Objects.isNull(result));
        Assertions.assertEquals(result.getURI().toString(), "url");
        Assertions.assertEquals(result.getMethod(), HttpMethod.GET.name());

    }

    @Test
    void testBuildGetRequest_ThrowsException() {
        // Setup
        final AbstractCredentialInfo credential = null;
        // Run the test
        assertThatThrownBy(() -> flexibleEngineClientUnderTest.buildGetRequest(credential,
                "url")).isInstanceOf(Exception.class);
    }

    @Test
    void testBuildPostRequest() throws Exception {
        // Setup
        final AbstractCredentialInfo credential = getCredentialDefinition();

        // Run the test
        final HttpRequestBase result =
                flexibleEngineClientUnderTest.buildPostRequest(credential, "url",
                        "postBody");

        // Verify the results
        Assertions.assertFalse(Objects.isNull(result));
        Assertions.assertEquals(result.getURI().toString(), "url");
        Assertions.assertEquals(result.getMethod(), HttpMethod.POST.name());
    }

    @Test
    void testBuildPostRequest_ThrowsException() {
        // Setup
        final AbstractCredentialInfo credential = null;

        // Run the test
        assertThatThrownBy(
                () -> flexibleEngineClientUnderTest.buildPostRequest(credential, "url",
                        "postBody")).isInstanceOf(Exception.class);
    }


    private CredentialVariables getCredentialDefinition() {

        List<CredentialVariable> definedCredentialVariables = new ArrayList<>();
        CredentialVariables credentialVariables = new CredentialVariables(
                Csp.FLEXIBLE_ENGINE, CredentialType.VARIABLES, "AK_SK",
                "The access key and security key.",
                null, definedCredentialVariables);
        definedCredentialVariables.add(
                new CredentialVariable(FlexibleEngineConstants.OS_ACCESS_KEY,
                        "The access key.", true));
        definedCredentialVariables.add(
                new CredentialVariable(FlexibleEngineConstants.OS_SECRET_KEY,
                        "The security key.", true));
        for (CredentialVariable credentialVariable : credentialVariables.getVariables()) {
            if (credentialVariable.getName().equals(FlexibleEngineConstants.OS_ACCESS_KEY)) {
                credentialVariable.setValue(FlexibleEngineConstants.OS_ACCESS_KEY);
            }
            if (credentialVariable.getName().equals(FlexibleEngineConstants.OS_SECRET_KEY)) {
                credentialVariable.setValue(FlexibleEngineConstants.OS_SECRET_KEY);
            }
        }
        return credentialVariables;
    }
}