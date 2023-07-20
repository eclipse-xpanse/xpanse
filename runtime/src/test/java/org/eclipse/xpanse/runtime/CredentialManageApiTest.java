/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.eclipse.xpanse.api.CredentialManageApi;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.Link;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test for CredentialManageApiTest.
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles("default")
@SpringBootTest(classes = {XpanseApplication.class, CredentialManageApi.class})
@AutoConfigureMockMvc
class CredentialManageApiTest {

    @Mock
    private CredentialCenter mockCredentialCenter;

    private CredentialManageApi credentialManageApiUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        credentialManageApiUnderTest = new CredentialManageApi(mockCredentialCenter);
    }

    @Test
    void testGetCredentialTypesByCsp() {
        // Setup
        when(mockCredentialCenter.getAvailableCredentialTypesByCsp(Csp.AWS)).thenReturn(
                List.of(CredentialType.VARIABLES));

        // Run the test
        final List<CredentialType> result =
                credentialManageApiUnderTest.getCredentialTypesByCsp(Csp.AWS);

        // Verify the results
        assertThat(result).isEqualTo(List.of(CredentialType.VARIABLES));
    }

    @Test
    void testGetCredentialTypesByCsp_CredentialCenterReturnsNoItems() {
        // Setup
        when(mockCredentialCenter.getAvailableCredentialTypesByCsp(Csp.AWS)).thenReturn(
                Collections.emptyList());

        // Run the test
        final List<CredentialType> result =
                credentialManageApiUnderTest.getCredentialTypesByCsp(Csp.AWS);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetCredentialCapabilitiesByCsp() {
        // Setup
        when(mockCredentialCenter.getCredentialCapabilitiesByCsp(Csp.AWS,
                CredentialType.VARIABLES)).thenReturn(List.of());

        // Run the test
        final List<AbstractCredentialInfo> result =
                credentialManageApiUnderTest.getCredentialCapabilitiesByCsp(Csp.AWS,
                        CredentialType.VARIABLES);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetCredentialCapabilitiesByCsp_CredentialCenterReturnsNoItems() {
        // Setup
        when(mockCredentialCenter.getCredentialCapabilitiesByCsp(Csp.AWS,
                CredentialType.VARIABLES)).thenReturn(Collections.emptyList());

        // Run the test
        final List<AbstractCredentialInfo> result =
                credentialManageApiUnderTest.getCredentialCapabilitiesByCsp(Csp.AWS,
                        CredentialType.VARIABLES);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetCredentialDefinitionsByCsp() {

        // Configure CredentialCenter.getCredentialDefinitionsByCsp(...).
        final List<AbstractCredentialInfo> credentialVariables =
                List.of(new CredentialVariables(Csp.AWS, "userName", "name", "description",
                        CredentialType.VARIABLES,
                        List.of(new CredentialVariable("name", "description", false))));
        when(mockCredentialCenter.getCredentials(Csp.AWS, "userName",
                CredentialType.VARIABLES)).thenReturn(credentialVariables);

        // Run the test
        final List<AbstractCredentialInfo> result =
                credentialManageApiUnderTest.getCredentials(Csp.AWS, "userName",
                        CredentialType.VARIABLES);

        // Verify the results
        assertThat(result).isEqualTo(credentialVariables);
    }

    @Test
    void testGetCredentialDefinitionsByCsp_CredentialCenterReturnsNoItems() {
        // Setup
        when(mockCredentialCenter.getCredentials(Csp.AWS, "userName",
                CredentialType.VARIABLES)).thenReturn(Collections.emptyList());

        // Run the test
        final List<AbstractCredentialInfo> result =
                credentialManageApiUnderTest.getCredentials(Csp.AWS, "userName",
                        CredentialType.VARIABLES);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetCredentialOpenApi() {
        // Setup
        final Link expectedResult = Link.of("href", "OpenApi");
        when(mockCredentialCenter.getCredentialOpenApiUrl(Csp.AWS,
                CredentialType.VARIABLES)).thenReturn("href");

        // Run the test
        final Link result = credentialManageApiUnderTest.getCredentialOpenApi(Csp.AWS,
                CredentialType.VARIABLES);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testAddCredential() {
        // Setup
        final CreateCredential createCredential = new CreateCredential();
        createCredential.setName("name");
        createCredential.setXpanseUser("userName");
        createCredential.setCsp(Csp.AWS);
        createCredential.setDescription("description");
        createCredential.setType(CredentialType.VARIABLES);

        // Configure CredentialCenter.addCredential(...).
        final CreateCredential createCredential1 = new CreateCredential();
        createCredential1.setName("name");
        createCredential1.setXpanseUser("userName");
        createCredential1.setCsp(Csp.AWS);
        createCredential1.setDescription("description");
        createCredential1.setType(CredentialType.VARIABLES);
        doThrow(new RuntimeException("test")).when(mockCredentialCenter)
                .addCredential(any(CreateCredential.class));

        // Verify the results
        Assertions.assertThrows(RuntimeException.class,
                () -> credentialManageApiUnderTest.addCredential(createCredential));
    }

    @Test
    void testUpdateCredential() {
        // Setup
        final CreateCredential updateCredential = new CreateCredential();
        updateCredential.setName("name");
        updateCredential.setXpanseUser("userName");
        updateCredential.setCsp(Csp.AWS);
        updateCredential.setDescription("description");
        updateCredential.setType(CredentialType.VARIABLES);

        // Configure CredentialCenter.updateCredential(...).
        final CreateCredential updateCredential1 = new CreateCredential();
        updateCredential1.setName("name");
        updateCredential1.setXpanseUser("userName");
        updateCredential1.setCsp(Csp.AWS);
        updateCredential1.setDescription("description");
        updateCredential1.setType(CredentialType.VARIABLES);
        doThrow(new RuntimeException("test")).when(mockCredentialCenter)
                .updateCredential(any(CreateCredential.class));

        // Run the test
        Assertions.assertThrows(RuntimeException.class,
                () -> credentialManageApiUnderTest.updateCredential(updateCredential));
    }

    @Test
    void testDeleteCredential() {
        // Setup
        doThrow(new RuntimeException("test")).when(mockCredentialCenter)
                .deleteCredential(any(Csp.class), any(String.class), any(CredentialType.class));

        // Run the test
        Assertions.assertThrows(RuntimeException.class,
                () -> credentialManageApiUnderTest.deleteCredential(Csp.AWS, "userName",
                        CredentialType.VARIABLES));
    }
}
