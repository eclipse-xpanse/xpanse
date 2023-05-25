/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.eclipse.xpanse.api.CredentialManageApi;
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.credential.CreateCredential;
import org.eclipse.xpanse.modules.credential.CredentialDefinition;
import org.eclipse.xpanse.modules.credential.CredentialVariable;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.orchestrator.credential.CredentialCenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.Link;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Test for CredentialManageApiTest.
 */
@ExtendWith(SpringExtension.class)
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
        final List<CredentialDefinition> credentialDefinitions =
                List.of(new CredentialDefinition(Csp.AWS, "name", "description",
                        CredentialType.VARIABLES,
                        List.of(new CredentialVariable("name", "description"))));
        when(mockCredentialCenter.getCredentialDefinitionsByCsp(Csp.AWS, "userName",
                CredentialType.VARIABLES)).thenReturn(credentialDefinitions);

        // Run the test
        final List<CredentialDefinition> result =
                credentialManageApiUnderTest.getCredentialDefinitionsByCsp(Csp.AWS, "userName",
                        CredentialType.VARIABLES);

        // Verify the results
        assertThat(result).isEqualTo(credentialDefinitions);
    }

    @Test
    void testGetCredentialDefinitionsByCsp_CredentialCenterReturnsNoItems() {
        // Setup
        when(mockCredentialCenter.getCredentialDefinitionsByCsp(Csp.AWS, "userName",
                CredentialType.VARIABLES)).thenReturn(Collections.emptyList());

        // Run the test
        final List<CredentialDefinition> result =
                credentialManageApiUnderTest.getCredentialDefinitionsByCsp(Csp.AWS, "userName",
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
        createCredential.setUserName("userName");
        createCredential.setCsp(Csp.AWS);
        createCredential.setDescription("description");
        createCredential.setType(CredentialType.VARIABLES);

        // Configure CredentialCenter.addCredential(...).
        final CreateCredential createCredential1 = new CreateCredential();
        createCredential1.setName("name");
        createCredential1.setUserName("userName");
        createCredential1.setCsp(Csp.AWS);
        createCredential1.setDescription("description");
        createCredential1.setType(CredentialType.VARIABLES);
        when(mockCredentialCenter.addCredential(Csp.AWS, createCredential1)).thenReturn(false);

        // Run the test
        final Boolean result =
                credentialManageApiUnderTest.addCredential(Csp.AWS, createCredential);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testUpdateCredential() {
        // Setup
        final CreateCredential updateCredential = new CreateCredential();
        updateCredential.setName("name");
        updateCredential.setUserName("userName");
        updateCredential.setCsp(Csp.AWS);
        updateCredential.setDescription("description");
        updateCredential.setType(CredentialType.VARIABLES);

        // Configure CredentialCenter.updateCredential(...).
        final CreateCredential updateCredential1 = new CreateCredential();
        updateCredential1.setName("name");
        updateCredential1.setUserName("userName");
        updateCredential1.setCsp(Csp.AWS);
        updateCredential1.setDescription("description");
        updateCredential1.setType(CredentialType.VARIABLES);
        when(mockCredentialCenter.updateCredential(Csp.AWS, updateCredential1)).thenReturn(false);

        // Run the test
        final Boolean result =
                credentialManageApiUnderTest.updateCredential(Csp.AWS, updateCredential);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testDeleteCredential() {
        // Setup
        when(mockCredentialCenter.deleteCredential(Csp.AWS, "userName")).thenReturn(false);

        // Run the test
        final Boolean result = credentialManageApiUnderTest.deleteCredential(Csp.AWS, "userName");

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testGetCredentialTypesByServiceId() {
        // Setup
        when(mockCredentialCenter.getAvailableCredentialTypesByServiceId("id")).thenReturn(
                List.of(CredentialType.VARIABLES));

        // Run the test
        final List<CredentialType> result =
                credentialManageApiUnderTest.getCredentialTypesByServiceId("id");

        // Verify the results
        assertThat(result).isEqualTo(List.of(CredentialType.VARIABLES));
    }

    @Test
    void testGetCredentialTypesByServiceId_CredentialCenterReturnsNoItems() {
        // Setup
        when(mockCredentialCenter.getAvailableCredentialTypesByServiceId("id")).thenReturn(
                Collections.emptyList());

        // Run the test
        final List<CredentialType> result =
                credentialManageApiUnderTest.getCredentialTypesByServiceId("id");

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetCredentialCapabilitiesByServiceId() {
        // Setup
        when(mockCredentialCenter.getCredentialCapabilitiesByServiceId("id",
                CredentialType.VARIABLES)).thenReturn(List.of());

        // Run the test
        final List<AbstractCredentialInfo> result =
                credentialManageApiUnderTest.getCredentialCapabilitiesByServiceId("id",
                        CredentialType.VARIABLES);

        // Verify the results
        assertThat(result).isEmpty();
    }

    @Test
    void testGetCredentialCapabilitiesByServiceId_CredentialCenterReturnsNoItems() {
        // Setup
        when(mockCredentialCenter.getCredentialCapabilitiesByServiceId("id",
                CredentialType.VARIABLES)).thenReturn(Collections.emptyList());

        // Run the test
        final List<AbstractCredentialInfo> result =
                credentialManageApiUnderTest.getCredentialCapabilitiesByServiceId("id",
                        CredentialType.VARIABLES);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetCredentialDefinitionsByServiceId() {

        // Configure CredentialCenter.getCredentialDefinitionsByServiceId(...).
        final List<CredentialDefinition> credentialDefinitions =
                List.of(new CredentialDefinition(Csp.AWS, "name", "description",
                        CredentialType.VARIABLES,
                        List.of(new CredentialVariable("name", "description"))));
        when(mockCredentialCenter.getCredentialDefinitionsByServiceId("id",
                CredentialType.VARIABLES)).thenReturn(credentialDefinitions);

        // Run the test
        final List<CredentialDefinition> result =
                credentialManageApiUnderTest.getCredentialDefinitionsByServiceId("id",
                        CredentialType.VARIABLES);

        // Verify the results
        assertThat(result).isEqualTo(credentialDefinitions);
    }

    @Test
    void testGetCredentialDefinitionsByServiceId_CredentialCenterReturnsNoItems() {
        // Setup
        when(mockCredentialCenter.getCredentialDefinitionsByServiceId("id",
                CredentialType.VARIABLES)).thenReturn(Collections.emptyList());

        // Run the test
        final List<CredentialDefinition> result =
                credentialManageApiUnderTest.getCredentialDefinitionsByServiceId("id",
                        CredentialType.VARIABLES);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testAddCredentialByServiceId() {
        // Setup
        final CreateCredential createCredential = new CreateCredential();
        createCredential.setName("name");
        createCredential.setUserName("userName");
        createCredential.setCsp(Csp.AWS);
        createCredential.setDescription("description");
        createCredential.setType(CredentialType.VARIABLES);

        // Configure CredentialCenter.addCredentialByServiceId(...).
        final CreateCredential createCredential1 = new CreateCredential();
        createCredential1.setName("name");
        createCredential1.setUserName("userName");
        createCredential1.setCsp(Csp.AWS);
        createCredential1.setDescription("description");
        createCredential1.setType(CredentialType.VARIABLES);
        when(mockCredentialCenter.addCredentialByServiceId("id", createCredential1)).thenReturn(
                false);

        // Run the test
        final Boolean result =
                credentialManageApiUnderTest.addCredentialByServiceId("id", createCredential);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testUpdateCredentialByServiceId() {
        // Setup
        final CreateCredential updateCredential = new CreateCredential();
        updateCredential.setName("name");
        updateCredential.setUserName("userName");
        updateCredential.setCsp(Csp.AWS);
        updateCredential.setDescription("description");
        updateCredential.setType(CredentialType.VARIABLES);

        // Configure CredentialCenter.updateCredentialByServiceId(...).
        final CreateCredential updateCredential1 = new CreateCredential();
        updateCredential1.setName("name");
        updateCredential1.setUserName("userName");
        updateCredential1.setCsp(Csp.AWS);
        updateCredential1.setDescription("description");
        updateCredential1.setType(CredentialType.VARIABLES);
        when(mockCredentialCenter.updateCredentialByServiceId("id", updateCredential1)).thenReturn(
                false);

        // Run the test
        final Boolean result =
                credentialManageApiUnderTest.updateCredentialByServiceId("id", updateCredential);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testDeleteCredentialByServiceId() {
        // Setup
        when(mockCredentialCenter.deleteCredentialByServiceId("id",
                CredentialType.VARIABLES)).thenReturn(false);

        // Run the test
        final Boolean result = credentialManageApiUnderTest.deleteCredentialByServiceId("id",
                CredentialType.VARIABLES);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testGetCredentialOpenApiByServiceId() {
        // Setup
        final Link expectedResult = Link.of("href", "OpenApi");
        when(mockCredentialCenter.getCredentialOpenApiUrlByServiceId("id",
                CredentialType.VARIABLES)).thenReturn("href");

        // Run the test
        final Link result = credentialManageApiUnderTest.getCredentialOpenApiByServiceId("id",
                CredentialType.VARIABLES);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
}
