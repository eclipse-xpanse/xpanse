package org.eclipse.xpanse.modules.policy.policyman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.servicepolicy.DatabaseServicePolicyStorage;
import org.eclipse.xpanse.modules.database.servicepolicy.ServicePolicyEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.DatabaseServiceTemplateStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesValidationFailedException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyDuplicateException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyNotFoundException;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicy;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicyCreateRequest;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicyUpdateRequest;
import org.eclipse.xpanse.modules.models.service.deploy.exceptions.FlavorInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.Flavor;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.policy.PolicyManager;
import org.eclipse.xpanse.modules.policy.ServicePolicyManager;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ServicePolicyManagerTest {

    private final UUID policyId = UUID.randomUUID();
    private final UUID serviceTemplateId = UUID.randomUUID();
    private final String namespace = "userNamespace";
    private final OffsetDateTime createTime =
            OffsetDateTime.of(LocalDateTime.of(2023, 1, 1, 0, 0, 0, 0), ZoneOffset.UTC);

    @Mock
    private PolicyManager mockPolicyManager;
    @Mock
    private IdentityProviderManager mockIdentityProviderManager;
    @Mock
    private DatabaseServicePolicyStorage mockServicePolicyStorage;
    @Mock
    private DatabaseServiceTemplateStorage mockServiceTemplateStorage;

    @InjectMocks
    private ServicePolicyManager servicePolicyManagerUnderTest;

    @Test
    void testListServicePolicies() {
        // Setup
        final ServicePolicy servicePolicy = new ServicePolicy();
        servicePolicy.setId(policyId);
        servicePolicy.setPolicy("policy");
        servicePolicy.setServiceTemplateId(serviceTemplateId);
        servicePolicy.setEnabled(false);
        final List<ServicePolicy> expectedResult = List.of(servicePolicy);

        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(serviceTemplateId);
        serviceTemplateEntity.setNamespace(namespace);
        final ServicePolicyEntity servicePolicyEntity = new ServicePolicyEntity();
        servicePolicyEntity.setId(policyId);
        servicePolicyEntity.setPolicy("policy");
        servicePolicyEntity.setEnabled(false);
        serviceTemplateEntity.setServicePolicyList(List.of(servicePolicyEntity));
        servicePolicyEntity.setServiceTemplate(serviceTemplateEntity);

        when(mockServiceTemplateStorage.getServiceTemplateById(serviceTemplateId))
                .thenReturn(serviceTemplateEntity);
        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.of(namespace));

        // Run the test
        final List<ServicePolicy> result =
                servicePolicyManagerUnderTest.listServicePolicies(serviceTemplateId.toString());

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testListServicePolicies_ThrowsServiceTemplateNotRegisteredException() {
        // Setup
        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        when(mockServiceTemplateStorage.getServiceTemplateById(
                serviceTemplateId))
                .thenReturn(null);

        // Run the test
        assertThatThrownBy(
                () -> servicePolicyManagerUnderTest.listServicePolicies(
                        serviceTemplateId.toString()))
                .isInstanceOf(ServiceTemplateNotRegistered.class);
    }

    @Test
    void testListServicePolicies_ThrowsAccessDeniedException() {
        // Setup
        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(serviceTemplateId);
        serviceTemplateEntity.setNamespace(namespace);
        when(mockServiceTemplateStorage.getServiceTemplateById(
                serviceTemplateId))
                .thenReturn(serviceTemplateEntity);

        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.empty());

        // Run the test
        assertThatThrownBy(
                () -> servicePolicyManagerUnderTest.listServicePolicies(
                        serviceTemplateId.toString()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void testAddServicePolicy() {
        final String newPolicy = "newPolicy";
        UUID newPolicyId = UUID.randomUUID();
        // Setup
        final ServicePolicyCreateRequest createRequest = new ServicePolicyCreateRequest();
        createRequest.setServiceTemplateId(serviceTemplateId);
        createRequest.setPolicy(newPolicy);
        createRequest.setEnabled(false);

        final ServicePolicy expectedResult = new ServicePolicy();
        expectedResult.setId(newPolicyId);
        expectedResult.setPolicy(newPolicy);
        expectedResult.setServiceTemplateId(serviceTemplateId);
        expectedResult.setEnabled(false);
        expectedResult.setCreateTime(createTime);

        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(serviceTemplateId);
        serviceTemplateEntity.setNamespace(namespace);
        final ServicePolicyEntity existingPolicy = new ServicePolicyEntity();
        existingPolicy.setId(policyId);
        existingPolicy.setPolicy("policy");
        existingPolicy.setEnabled(false);
        existingPolicy.setServiceTemplate(serviceTemplateEntity);
        serviceTemplateEntity.setServicePolicyList(List.of(existingPolicy));
        when(mockServiceTemplateStorage.getServiceTemplateById(
                serviceTemplateId))
                .thenReturn(serviceTemplateEntity);

        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.of(namespace));

        // Configure DatabaseServicePolicyStorage.storeAndFlush(...).
        final ServicePolicyEntity newServicePolicy = new ServicePolicyEntity();
        newServicePolicy.setId(newPolicyId);
        newServicePolicy.setPolicy(newPolicy);
        newServicePolicy.setEnabled(false);
        newServicePolicy.setCreateTime(createTime);
        newServicePolicy.setServiceTemplate(serviceTemplateEntity);

        when(mockServicePolicyStorage.storeAndFlush(any()))
                .thenReturn(newServicePolicy);

        // Run the test
        final ServicePolicy result = servicePolicyManagerUnderTest.addServicePolicy(createRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testAddServicePolicyWithFlavorNameList() {
        final String newPolicy = "newPolicy";
        final List<String> flavorNameList = List.of("flavor1", "flavor2");
        UUID newPolicyId = UUID.randomUUID();
        // Setup
        final ServicePolicyCreateRequest createRequest = new ServicePolicyCreateRequest();
        createRequest.setServiceTemplateId(serviceTemplateId);
        createRequest.setFlavorNameList(flavorNameList);
        createRequest.setPolicy(newPolicy);
        createRequest.setEnabled(false);

        final ServicePolicy expectedResult = new ServicePolicy();
        expectedResult.setId(newPolicyId);
        expectedResult.setPolicy(newPolicy);
        expectedResult.setFlavorNameList(flavorNameList);
        expectedResult.setServiceTemplateId(serviceTemplateId);
        expectedResult.setEnabled(false);
        expectedResult.setCreateTime(createTime);

        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(serviceTemplateId);
        serviceTemplateEntity.setNamespace(namespace);
        Flavor flavor1 = new Flavor();
        flavor1.setName("flavor1");
        Flavor flavor2 = new Flavor();
        flavor2.setName("flavor2");
        final Ocl ocl = new Ocl();
        ocl.setFlavors(List.of(flavor1, flavor2));
        serviceTemplateEntity.setOcl(ocl);
        final ServicePolicyEntity existingPolicy = new ServicePolicyEntity();
        existingPolicy.setId(policyId);
        existingPolicy.setPolicy("policy");
        existingPolicy.setEnabled(false);
        existingPolicy.setServiceTemplate(serviceTemplateEntity);
        serviceTemplateEntity.setServicePolicyList(List.of(existingPolicy));
        when(mockServiceTemplateStorage.getServiceTemplateById(
                serviceTemplateId))
                .thenReturn(serviceTemplateEntity);

        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.of(namespace));

        // Configure DatabaseServicePolicyStorage.storeAndFlush(...).
        final ServicePolicyEntity newServicePolicy = new ServicePolicyEntity();
        newServicePolicy.setId(newPolicyId);
        newServicePolicy.setFlavorNames(StringUtils.join(flavorNameList, ","));
        newServicePolicy.setPolicy(newPolicy);
        newServicePolicy.setEnabled(false);
        newServicePolicy.setCreateTime(createTime);
        newServicePolicy.setServiceTemplate(serviceTemplateEntity);

        when(mockServicePolicyStorage.storeAndFlush(any()))
                .thenReturn(newServicePolicy);

        // Run the test
        final ServicePolicy result = servicePolicyManagerUnderTest.addServicePolicy(createRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testAddServicePolicyWithFlavorNameList_ThrowsFlavorInvalidException() {
        final String newPolicy = "newPolicy";
        final List<String> flavorNameList = List.of("flavor1", "invalid_flavor");
        // Setup
        final ServicePolicyCreateRequest createRequest = new ServicePolicyCreateRequest();
        createRequest.setServiceTemplateId(serviceTemplateId);
        createRequest.setFlavorNameList(flavorNameList);
        createRequest.setPolicy(newPolicy);
        createRequest.setEnabled(false);

        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(serviceTemplateId);
        serviceTemplateEntity.setNamespace(namespace);
        Flavor flavor = new Flavor();
        flavor.setName("flavor");
        final Ocl ocl = new Ocl();
        ocl.setFlavors(List.of(flavor));
        serviceTemplateEntity.setOcl(ocl);

        when(mockServiceTemplateStorage.getServiceTemplateById(
                serviceTemplateId))
                .thenReturn(serviceTemplateEntity);

        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.of(namespace));

        assertThatThrownBy(
                () -> servicePolicyManagerUnderTest.addServicePolicy(createRequest))
                .isInstanceOf(FlavorInvalidException.class);
    }

    @Test
    void testAddServicePolicy_ThrowsServiceTemplateNotRegisteredException() {
        // Setup
        final ServicePolicyCreateRequest createRequest = new ServicePolicyCreateRequest();
        createRequest.setServiceTemplateId(serviceTemplateId);
        createRequest.setPolicy("policy");
        createRequest.setEnabled(false);
        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        when(mockServiceTemplateStorage.getServiceTemplateById(
                serviceTemplateId))
                .thenReturn(null);

        // Run the test
        assertThatThrownBy(
                () -> servicePolicyManagerUnderTest.addServicePolicy(createRequest))
                .isInstanceOf(ServiceTemplateNotRegistered.class);
    }

    @Test
    void testAddServicePolicy_ThrowsAccessDeniedException() {
        // Setup
        final ServicePolicyCreateRequest createRequest = new ServicePolicyCreateRequest();
        createRequest.setServiceTemplateId(serviceTemplateId);
        createRequest.setPolicy("policy");
        createRequest.setEnabled(false);
        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(serviceTemplateId);
        serviceTemplateEntity.setNamespace(namespace);
        when(mockServiceTemplateStorage.getServiceTemplateById(
                serviceTemplateId))
                .thenReturn(serviceTemplateEntity);

        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.empty());

        // Run the test
        assertThatThrownBy(
                () -> servicePolicyManagerUnderTest.addServicePolicy(createRequest))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void testAddServicePolicy_ThrowsPoliciesValidationFailedException() {
        // Setup
        final ServicePolicyCreateRequest createRequest = new ServicePolicyCreateRequest();
        createRequest.setServiceTemplateId(serviceTemplateId);
        createRequest.setPolicy("policy");
        createRequest.setEnabled(false);
        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(serviceTemplateId);
        serviceTemplateEntity.setNamespace(namespace);

        when(mockServiceTemplateStorage.getServiceTemplateById(serviceTemplateId))
                .thenReturn(serviceTemplateEntity);

        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.of(namespace));

        doThrow(new PoliciesValidationFailedException("test")).when(mockPolicyManager)
                .validatePolicy("policy");

        // Run the test
        assertThatThrownBy(
                () -> servicePolicyManagerUnderTest.addServicePolicy(createRequest))
                .isInstanceOf(PoliciesValidationFailedException.class);
    }

    @Test
    void testAddServicePolicy_ThrowsPolicyDuplicateException() {
        final String newPolicy = "policy";
        UUID newPolicyId = UUID.randomUUID();
        // Setup
        final ServicePolicyCreateRequest createRequest = new ServicePolicyCreateRequest();
        createRequest.setServiceTemplateId(serviceTemplateId);
        createRequest.setPolicy(newPolicy);
        createRequest.setEnabled(false);

        final ServicePolicy expectedResult = new ServicePolicy();
        expectedResult.setId(newPolicyId);
        expectedResult.setPolicy(newPolicy);
        expectedResult.setServiceTemplateId(serviceTemplateId);
        expectedResult.setEnabled(false);
        expectedResult.setCreateTime(createTime);

        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(serviceTemplateId);
        serviceTemplateEntity.setNamespace(namespace);
        final ServicePolicyEntity existingPolicy = new ServicePolicyEntity();
        existingPolicy.setId(policyId);
        existingPolicy.setPolicy("policy");
        existingPolicy.setEnabled(false);
        existingPolicy.setServiceTemplate(serviceTemplateEntity);
        serviceTemplateEntity.setServicePolicyList(List.of(existingPolicy));
        when(mockServiceTemplateStorage.getServiceTemplateById(serviceTemplateId))
                .thenReturn(serviceTemplateEntity);

        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.of(namespace));
        // Run the test
        assertThatThrownBy(
                () -> servicePolicyManagerUnderTest.addServicePolicy(createRequest))
                .isInstanceOf(PolicyDuplicateException.class);
    }

    @Test
    void testUpdateServicePolicy() {
        final String updatePolicy = "updatePolicy";
        // Setup
        final ServicePolicyUpdateRequest updateRequest = new ServicePolicyUpdateRequest();
        updateRequest.setId(policyId);
        updateRequest.setPolicy(updatePolicy);
        updateRequest.setEnabled(true);

        final ServicePolicy expectedResult = new ServicePolicy();
        expectedResult.setId(policyId);
        expectedResult.setPolicy(updatePolicy);
        expectedResult.setServiceTemplateId(serviceTemplateId);
        expectedResult.setEnabled(true);

        // Configure DatabaseServicePolicyStorage.findPolicyById(...).
        final ServicePolicyEntity existingPolicy = new ServicePolicyEntity();
        existingPolicy.setId(policyId);
        existingPolicy.setPolicy("policy");
        existingPolicy.setEnabled(false);
        final ServiceTemplateEntity existingTemplate = new ServiceTemplateEntity();
        existingTemplate.setId(serviceTemplateId);
        existingTemplate.setNamespace(namespace);
        existingTemplate.setServicePolicyList(List.of(existingPolicy));
        existingPolicy.setServiceTemplate(existingTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(existingPolicy);
        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.of(namespace));

        // Configure DatabaseServicePolicyStorage.storeAndFlush(...).
        final ServicePolicyEntity servicePolicyEntity = new ServicePolicyEntity();
        servicePolicyEntity.setId(policyId);
        servicePolicyEntity.setPolicy(updatePolicy);
        servicePolicyEntity.setEnabled(true);
        final ServiceTemplateEntity serviceTemplate = new ServiceTemplateEntity();
        serviceTemplate.setId(serviceTemplateId);
        serviceTemplate.setNamespace(namespace);
        serviceTemplate.setServicePolicyList(List.of(servicePolicyEntity));
        servicePolicyEntity.setServiceTemplate(serviceTemplate);

        when(mockServicePolicyStorage.storeAndFlush(any())).thenReturn(servicePolicyEntity);

        // Run the test
        final ServicePolicy result =
                servicePolicyManagerUnderTest.updateServicePolicy(updateRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testUpdateServicePolicyWithFlavorNameList() {
        final String updatePolicy = "updatePolicy";
        final List<String> flavorNameList = List.of("flavor1", "flavor2");
        // Setup
        final ServicePolicyUpdateRequest updateRequest = new ServicePolicyUpdateRequest();
        updateRequest.setId(policyId);
        updateRequest.setFlavorNameList(flavorNameList);
        updateRequest.setPolicy(updatePolicy);
        updateRequest.setEnabled(true);

        final ServicePolicy expectedResult = new ServicePolicy();
        expectedResult.setId(policyId);
        expectedResult.setFlavorNameList(flavorNameList);
        expectedResult.setPolicy(updatePolicy);
        expectedResult.setServiceTemplateId(serviceTemplateId);
        expectedResult.setEnabled(true);

        // Configure DatabaseServicePolicyStorage.findPolicyById(...).
        final ServicePolicyEntity existingPolicy = new ServicePolicyEntity();
        existingPolicy.setId(policyId);
        existingPolicy.setFlavorNames("flavor");
        existingPolicy.setPolicy("policy");
        existingPolicy.setEnabled(false);
        final ServiceTemplateEntity existingTemplate = new ServiceTemplateEntity();
        existingTemplate.setId(serviceTemplateId);
        existingTemplate.setNamespace(namespace);
        Flavor flavor1 = new Flavor();
        flavor1.setName("flavor1");
        Flavor flavor2 = new Flavor();
        flavor2.setName("flavor2");
        final Ocl ocl = new Ocl();
        ocl.setFlavors(List.of(flavor1, flavor2));
        existingTemplate.setOcl(ocl);
        existingTemplate.setServicePolicyList(List.of(existingPolicy));
        existingPolicy.setServiceTemplate(existingTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(existingPolicy);
        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.of(namespace));

        // Configure DatabaseServicePolicyStorage.storeAndFlush(...).
        final ServicePolicyEntity servicePolicyEntity = new ServicePolicyEntity();
        servicePolicyEntity.setId(policyId);
        servicePolicyEntity.setFlavorNames(StringUtils.join(flavorNameList, ","));
        servicePolicyEntity.setPolicy(updatePolicy);
        servicePolicyEntity.setEnabled(true);
        final ServiceTemplateEntity serviceTemplate = new ServiceTemplateEntity();
        serviceTemplate.setId(serviceTemplateId);
        serviceTemplate.setNamespace(namespace);
        serviceTemplate.setOcl(ocl);
        serviceTemplate.setServicePolicyList(List.of(servicePolicyEntity));
        servicePolicyEntity.setServiceTemplate(serviceTemplate);

        when(mockServicePolicyStorage.storeAndFlush(any())).thenReturn(servicePolicyEntity);

        // Run the test
        final ServicePolicy result =
                servicePolicyManagerUnderTest.updateServicePolicy(updateRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }


    @Test
    void testUpdateServicePolicyWithFlavorName_ThrowsFlavorInvalidException() {
        final String updatePolicy = "updatePolicy";
        final List<String> flavorNameList = List.of("flavor1", "invalid_flavor");
        // Setup
        final ServicePolicyUpdateRequest updateRequest = new ServicePolicyUpdateRequest();
        updateRequest.setId(policyId);
        updateRequest.setFlavorNameList(flavorNameList);
        updateRequest.setPolicy(updatePolicy);
        updateRequest.setEnabled(true);

        final ServicePolicy expectedResult = new ServicePolicy();
        expectedResult.setId(policyId);
        expectedResult.setFlavorNameList(flavorNameList);
        expectedResult.setPolicy(updatePolicy);
        expectedResult.setServiceTemplateId(serviceTemplateId);
        expectedResult.setEnabled(true);

        // Configure DatabaseServicePolicyStorage.findPolicyById(...).
        final ServicePolicyEntity existingPolicy = new ServicePolicyEntity();
        existingPolicy.setId(policyId);
        existingPolicy.setFlavorNames("flavor");
        existingPolicy.setPolicy("policy");
        existingPolicy.setEnabled(false);
        final ServiceTemplateEntity existingTemplate = new ServiceTemplateEntity();
        existingTemplate.setId(serviceTemplateId);
        existingTemplate.setNamespace(namespace);
        Flavor flavor1 = new Flavor();
        flavor1.setName("flavor1");
        Flavor flavor2 = new Flavor();
        flavor2.setName("flavor1");
        final Ocl ocl = new Ocl();
        ocl.setFlavors(List.of(flavor1, flavor2));
        existingTemplate.setOcl(ocl);
        existingTemplate.setServicePolicyList(List.of(existingPolicy));
        existingPolicy.setServiceTemplate(existingTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(existingPolicy);
        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.of(namespace));


        assertThatThrownBy(
                () -> servicePolicyManagerUnderTest.updateServicePolicy(updateRequest))
                .isInstanceOf(FlavorInvalidException.class);
    }

    @Test
    void testUpdateServicePolicy_ThrowsPolicyNotFoundExceptionException() {
        final String updatePolicy = "updatePolicy";
        // Setup
        final ServicePolicyUpdateRequest updateRequest = new ServicePolicyUpdateRequest();
        updateRequest.setId(policyId);
        updateRequest.setPolicy(updatePolicy);
        updateRequest.setEnabled(true);
        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(null);

        // Run the test
        assertThatThrownBy(
                () -> servicePolicyManagerUnderTest.updateServicePolicy(updateRequest))
                .isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    void testUpdateServicePolicy_ThrowsAccessDeniedException() {
        final String updatePolicy = "updatePolicy";
        // Setup
        final ServicePolicyUpdateRequest updateRequest = new ServicePolicyUpdateRequest();
        updateRequest.setId(policyId);
        updateRequest.setPolicy(updatePolicy);
        updateRequest.setEnabled(true);

        // Configure DatabaseServicePolicyStorage.findPolicyById(...).
        final ServicePolicyEntity existingPolicy = new ServicePolicyEntity();
        existingPolicy.setId(policyId);
        existingPolicy.setPolicy("policy");
        existingPolicy.setEnabled(false);
        final ServiceTemplateEntity existingTemplate = new ServiceTemplateEntity();
        existingTemplate.setId(serviceTemplateId);
        existingTemplate.setNamespace(namespace);
        existingTemplate.setServicePolicyList(List.of(existingPolicy));
        existingPolicy.setServiceTemplate(existingTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(existingPolicy);
        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.empty());

        // Run the test
        assertThatThrownBy(
                () -> servicePolicyManagerUnderTest.updateServicePolicy(updateRequest))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void testUpdateServicePolicy_ThrowsPoliciesValidationFailedException() {
        // Setup
        final String updatePolicy = "updatePolicy";
        // Setup
        final ServicePolicyUpdateRequest updateRequest = new ServicePolicyUpdateRequest();
        updateRequest.setId(policyId);
        updateRequest.setPolicy(updatePolicy);
        updateRequest.setEnabled(true);

        final ServicePolicy expectedResult = new ServicePolicy();
        expectedResult.setId(policyId);
        expectedResult.setPolicy(updatePolicy);
        expectedResult.setServiceTemplateId(serviceTemplateId);
        expectedResult.setEnabled(true);

        // Configure DatabaseServicePolicyStorage.findPolicyById(...).
        final ServicePolicyEntity existingPolicy = new ServicePolicyEntity();
        existingPolicy.setId(policyId);
        existingPolicy.setPolicy("policy");
        existingPolicy.setEnabled(false);
        final ServiceTemplateEntity existingTemplate = new ServiceTemplateEntity();
        existingTemplate.setId(serviceTemplateId);
        existingTemplate.setNamespace(namespace);
        existingTemplate.setServicePolicyList(List.of(existingPolicy));
        existingPolicy.setServiceTemplate(existingTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(existingPolicy);
        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.of(namespace));
        doThrow(new PoliciesValidationFailedException("error")).when(mockPolicyManager)
                .validatePolicy(updatePolicy);

        // Run the test
        assertThatThrownBy(
                () -> servicePolicyManagerUnderTest.updateServicePolicy(updateRequest))
                .isInstanceOf(PoliciesValidationFailedException.class);
    }

    @Test
    void testUpdateServicePolicy_ThrowsPolicyDuplicateException() {
        final String updatePolicy = "updatePolicy";
        // Setup
        final ServicePolicyUpdateRequest updateRequest = new ServicePolicyUpdateRequest();
        updateRequest.setId(policyId);
        updateRequest.setPolicy(updatePolicy);
        updateRequest.setEnabled(true);

        // Configure DatabaseServicePolicyStorage.findPolicyById(...).
        final ServicePolicyEntity existingPolicy1 = new ServicePolicyEntity();
        existingPolicy1.setId(policyId);
        existingPolicy1.setPolicy("policy");
        existingPolicy1.setEnabled(false);

        final ServicePolicyEntity existingPolicy2 = new ServicePolicyEntity();
        existingPolicy2.setId(UUID.randomUUID());
        existingPolicy2.setPolicy("updatePolicy");
        existingPolicy2.setEnabled(false);

        final ServiceTemplateEntity existingTemplate = new ServiceTemplateEntity();
        existingTemplate.setId(serviceTemplateId);
        existingTemplate.setNamespace(namespace);
        existingTemplate.setServicePolicyList(List.of(existingPolicy1, existingPolicy2));
        existingPolicy1.setServiceTemplate(existingTemplate);
        existingPolicy2.setServiceTemplate(existingTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(existingPolicy1);
        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.of(namespace));

        // Run the test
        assertThatThrownBy(
                () -> servicePolicyManagerUnderTest.updateServicePolicy(updateRequest))
                .isInstanceOf(PolicyDuplicateException.class);
    }

    @Test
    void testGetServicePolicyDetails() {
        // Setup
        final ServicePolicy expectedResult = new ServicePolicy();
        expectedResult.setId(policyId);
        expectedResult.setPolicy("policy");
        expectedResult.setServiceTemplateId(serviceTemplateId);
        expectedResult.setEnabled(false);
        expectedResult.setCreateTime(createTime);
        // Configure DatabaseServicePolicyStorage.findPolicyById(...).
        final ServicePolicyEntity servicePolicyEntity = new ServicePolicyEntity();
        servicePolicyEntity.setId(policyId);
        servicePolicyEntity.setPolicy("policy");
        servicePolicyEntity.setCreateTime(createTime);
        servicePolicyEntity.setEnabled(false);
        final ServiceTemplateEntity serviceTemplate = new ServiceTemplateEntity();
        serviceTemplate.setId(serviceTemplateId);
        serviceTemplate.setNamespace(namespace);
        serviceTemplate.setServicePolicyList(List.of(servicePolicyEntity));
        servicePolicyEntity.setServiceTemplate(serviceTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(servicePolicyEntity);
        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.of(namespace));

        // Run the test
        final ServicePolicy result =
                servicePolicyManagerUnderTest.getServicePolicyDetails(policyId);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetServicePolicyDetails_ThrowsAccessDeniedException() {
        // Setup
        final ServicePolicy expectedResult = new ServicePolicy();
        expectedResult.setId(policyId);
        expectedResult.setPolicy("policy");
        expectedResult.setServiceTemplateId(serviceTemplateId);
        expectedResult.setEnabled(false);
        expectedResult.setCreateTime(createTime);
        // Configure DatabaseServicePolicyStorage.findPolicyById(...).
        final ServicePolicyEntity servicePolicyEntity = new ServicePolicyEntity();
        servicePolicyEntity.setId(policyId);
        servicePolicyEntity.setPolicy("policy");
        servicePolicyEntity.setCreateTime(createTime);
        servicePolicyEntity.setEnabled(false);
        final ServiceTemplateEntity serviceTemplate = new ServiceTemplateEntity();
        serviceTemplate.setId(serviceTemplateId);
        serviceTemplate.setNamespace(namespace);
        serviceTemplate.setServicePolicyList(List.of(servicePolicyEntity));
        servicePolicyEntity.setServiceTemplate(serviceTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(servicePolicyEntity);
        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.empty());

        // Run the test
        assertThatThrownBy(() -> servicePolicyManagerUnderTest.getServicePolicyDetails(policyId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void testGetServicePolicyDetails_ThrowsPolicyNotFoundException() {
        // Setup
        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(null);
        // Run the test
        assertThatThrownBy(() -> servicePolicyManagerUnderTest.getServicePolicyDetails(policyId))
                .isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    void testDeleteServicePolicy() {
        // Setup
        // Configure DatabaseServicePolicyStorage.findPolicyById(...).
        final ServicePolicyEntity servicePolicyEntity = new ServicePolicyEntity();
        servicePolicyEntity.setId(policyId);
        servicePolicyEntity.setPolicy("policy");
        servicePolicyEntity.setEnabled(false);
        final ServiceTemplateEntity serviceTemplate = new ServiceTemplateEntity();
        serviceTemplate.setId(serviceTemplateId);
        serviceTemplate.setNamespace(namespace);
        serviceTemplate.setServicePolicyList(List.of(servicePolicyEntity));
        servicePolicyEntity.setServiceTemplate(serviceTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(servicePolicyEntity);
        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.of(namespace));

        // Run the test
        servicePolicyManagerUnderTest.deleteServicePolicy(policyId);

        // Verify the results
        verify(mockServicePolicyStorage).deletePolicyById(policyId);
    }

    @Test
    void testDeleteServicePolicy_ThrowsAccessDeniedException() {
        // Setup
        final ServicePolicy expectedResult = new ServicePolicy();
        expectedResult.setId(policyId);
        expectedResult.setPolicy("policy");
        expectedResult.setServiceTemplateId(serviceTemplateId);
        expectedResult.setEnabled(false);
        expectedResult.setCreateTime(createTime);
        // Configure DatabaseServicePolicyStorage.findPolicyById(...).
        final ServicePolicyEntity servicePolicyEntity = new ServicePolicyEntity();
        servicePolicyEntity.setId(policyId);
        servicePolicyEntity.setPolicy("policy");
        servicePolicyEntity.setCreateTime(createTime);
        servicePolicyEntity.setEnabled(false);
        final ServiceTemplateEntity serviceTemplate = new ServiceTemplateEntity();
        serviceTemplate.setId(serviceTemplateId);
        serviceTemplate.setNamespace(namespace);
        serviceTemplate.setServicePolicyList(List.of(servicePolicyEntity));
        servicePolicyEntity.setServiceTemplate(serviceTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(servicePolicyEntity);
        when(mockIdentityProviderManager.getUserNamespace()).thenReturn(Optional.empty());

        // Run the test
        assertThatThrownBy(() -> servicePolicyManagerUnderTest.deleteServicePolicy(policyId))
                .isInstanceOf(AccessDeniedException.class);
    }


    @Test
    void testDeleteServicePolicy_ThrowsPolicyNotFoundException() {
        // Setup
        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(null);
        // Run the test
        assertThatThrownBy(() -> servicePolicyManagerUnderTest.deleteServicePolicy(policyId))
                .isInstanceOf(PolicyNotFoundException.class);
    }
}
