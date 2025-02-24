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
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.database.servicepolicy.ServicePolicyEntity;
import org.eclipse.xpanse.modules.database.servicepolicy.ServicePolicyStorage;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateStorage;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesValidationFailedException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyDuplicateException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyNotFoundException;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicy;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicyCreateRequest;
import org.eclipse.xpanse.modules.models.policy.servicepolicy.ServicePolicyUpdateRequest;
import org.eclipse.xpanse.modules.models.service.deployment.exceptions.FlavorInvalidException;
import org.eclipse.xpanse.modules.models.servicetemplate.FlavorsWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.ModificationImpact;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.ServiceFlavorWithPrice;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.ServiceTemplateNotRegistered;
import org.eclipse.xpanse.modules.policy.PolicyManager;
import org.eclipse.xpanse.modules.policy.ServicePolicyManager;
import org.eclipse.xpanse.modules.security.auth.UserServiceHelper;
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
    private final String serviceVendor = "userServiceVendor";
    private final OffsetDateTime createdTime =
            OffsetDateTime.of(LocalDateTime.of(2023, 1, 1, 0, 0, 0, 0), ZoneOffset.UTC);

    @Mock private PolicyManager mockPolicyManager;
    @Mock private UserServiceHelper mockUserServiceHelper;
    @Mock private ServicePolicyStorage mockServicePolicyStorage;
    @Mock private ServiceTemplateStorage mockServiceTemplateStorage;

    @InjectMocks private ServicePolicyManager test;

    @Test
    void testListServicePolicies() {
        // Setup
        final ServicePolicy servicePolicy = new ServicePolicy();
        servicePolicy.setServicePolicyId(policyId);
        servicePolicy.setPolicy("policy");
        servicePolicy.setServiceTemplateId(serviceTemplateId);
        servicePolicy.setEnabled(false);
        final List<ServicePolicy> expectedResult = List.of(servicePolicy);

        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(serviceTemplateId);
        serviceTemplateEntity.setServiceVendor(serviceVendor);
        final ServicePolicyEntity servicePolicyEntity = new ServicePolicyEntity();
        servicePolicyEntity.setId(policyId);
        servicePolicyEntity.setPolicy("policy");
        servicePolicyEntity.setEnabled(false);
        serviceTemplateEntity.setServicePolicyList(List.of(servicePolicyEntity));
        servicePolicyEntity.setServiceTemplate(serviceTemplateEntity);

        when(mockServiceTemplateStorage.getServiceTemplateById(serviceTemplateId))
                .thenReturn(serviceTemplateEntity);
        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(true);

        // Run the test
        final List<ServicePolicy> result = test.listServicePolicies(serviceTemplateId);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testListServicePolicies_ThrowsServiceTemplateNotRegisteredException() {
        // Setup
        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        when(mockServiceTemplateStorage.getServiceTemplateById(serviceTemplateId))
                .thenThrow(new ServiceTemplateNotRegistered("serviceTemplateId"));

        // Run the test
        assertThatThrownBy(() -> test.listServicePolicies(serviceTemplateId))
                .isInstanceOf(ServiceTemplateNotRegistered.class);
    }

    @Test
    void testListServicePolicies_ThrowsAccessDeniedException() {
        // Setup
        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(serviceTemplateId);
        serviceTemplateEntity.setServiceVendor(serviceVendor);
        when(mockServiceTemplateStorage.getServiceTemplateById(serviceTemplateId))
                .thenReturn(serviceTemplateEntity);

        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(false);

        // Run the test
        assertThatThrownBy(() -> test.listServicePolicies(serviceTemplateId))
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
        expectedResult.setServicePolicyId(newPolicyId);
        expectedResult.setPolicy(newPolicy);
        expectedResult.setServiceTemplateId(serviceTemplateId);
        expectedResult.setEnabled(false);
        expectedResult.setCreatedTime(createdTime);

        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(serviceTemplateId);
        serviceTemplateEntity.setServiceVendor(serviceVendor);
        final ServicePolicyEntity existingPolicy = new ServicePolicyEntity();
        existingPolicy.setId(policyId);
        existingPolicy.setPolicy("policy");
        existingPolicy.setEnabled(false);
        existingPolicy.setServiceTemplate(serviceTemplateEntity);
        serviceTemplateEntity.setServicePolicyList(List.of(existingPolicy));
        when(mockServiceTemplateStorage.getServiceTemplateById(serviceTemplateId))
                .thenReturn(serviceTemplateEntity);

        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(true);

        // Configure DatabaseServicePolicyStorage.storeAndFlush(...).
        final ServicePolicyEntity newServicePolicy = new ServicePolicyEntity();
        newServicePolicy.setId(newPolicyId);
        newServicePolicy.setPolicy(newPolicy);
        newServicePolicy.setEnabled(false);
        newServicePolicy.setCreatedTime(createdTime);
        newServicePolicy.setServiceTemplate(serviceTemplateEntity);

        when(mockServicePolicyStorage.storeAndFlush(any())).thenReturn(newServicePolicy);
        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(true);

        // Run the test
        final ServicePolicy result = test.addServicePolicy(createRequest);

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
        expectedResult.setServicePolicyId(newPolicyId);
        expectedResult.setPolicy(newPolicy);
        expectedResult.setFlavorNameList(flavorNameList);
        expectedResult.setServiceTemplateId(serviceTemplateId);
        expectedResult.setEnabled(false);
        expectedResult.setCreatedTime(createdTime);

        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(serviceTemplateId);
        serviceTemplateEntity.setServiceVendor(serviceVendor);
        FlavorsWithPrice flavors = new FlavorsWithPrice();
        ServiceFlavorWithPrice flavor1 = new ServiceFlavorWithPrice();
        flavor1.setName("flavor1");
        ServiceFlavorWithPrice flavor2 = new ServiceFlavorWithPrice();
        flavor2.setName("flavor2");
        ModificationImpact modificationImpact = new ModificationImpact();
        modificationImpact.setIsDataLost(false);
        modificationImpact.setIsServiceInterrupted(false);
        flavors.setServiceFlavors(List.of(flavor1, flavor2));
        flavors.setModificationImpact(modificationImpact);
        final Ocl ocl = new Ocl();
        ocl.setFlavors(flavors);
        serviceTemplateEntity.setOcl(ocl);
        final ServicePolicyEntity existingPolicy = new ServicePolicyEntity();
        existingPolicy.setId(policyId);
        existingPolicy.setPolicy("policy");
        existingPolicy.setEnabled(false);
        existingPolicy.setServiceTemplate(serviceTemplateEntity);
        serviceTemplateEntity.setServicePolicyList(List.of(existingPolicy));
        when(mockServiceTemplateStorage.getServiceTemplateById(serviceTemplateId))
                .thenReturn(serviceTemplateEntity);

        // Configure DatabaseServicePolicyStorage.storeAndFlush(...).
        final ServicePolicyEntity newServicePolicy = new ServicePolicyEntity();
        newServicePolicy.setId(newPolicyId);
        newServicePolicy.setFlavorNames(StringUtils.join(flavorNameList, ","));
        newServicePolicy.setPolicy(newPolicy);
        newServicePolicy.setEnabled(false);
        newServicePolicy.setCreatedTime(createdTime);
        newServicePolicy.setServiceTemplate(serviceTemplateEntity);

        when(mockServicePolicyStorage.storeAndFlush(any())).thenReturn(newServicePolicy);
        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(true);

        // Run the test
        final ServicePolicy result = test.addServicePolicy(createRequest);

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
        serviceTemplateEntity.setServiceVendor(serviceVendor);
        ServiceFlavorWithPrice flavor = new ServiceFlavorWithPrice();
        flavor.setName("flavor");
        FlavorsWithPrice flavors = new FlavorsWithPrice();
        ModificationImpact modificationImpact = new ModificationImpact();
        modificationImpact.setIsDataLost(false);
        modificationImpact.setIsServiceInterrupted(false);
        flavors.setServiceFlavors(List.of(flavor));
        flavors.setModificationImpact(modificationImpact);
        final Ocl ocl = new Ocl();
        ocl.setFlavors(flavors);
        serviceTemplateEntity.setOcl(ocl);

        when(mockServiceTemplateStorage.getServiceTemplateById(serviceTemplateId))
                .thenReturn(serviceTemplateEntity);

        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(true);

        assertThatThrownBy(() -> test.addServicePolicy(createRequest))
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
        when(mockServiceTemplateStorage.getServiceTemplateById(serviceTemplateId))
                .thenThrow(new ServiceTemplateNotRegistered(""));

        // Run the test
        assertThatThrownBy(() -> test.addServicePolicy(createRequest))
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
        serviceTemplateEntity.setServiceVendor(serviceVendor);
        when(mockServiceTemplateStorage.getServiceTemplateById(serviceTemplateId))
                .thenReturn(serviceTemplateEntity);

        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(false);

        // Run the test
        assertThatThrownBy(() -> test.addServicePolicy(createRequest))
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
        serviceTemplateEntity.setServiceVendor(serviceVendor);

        when(mockServiceTemplateStorage.getServiceTemplateById(serviceTemplateId))
                .thenReturn(serviceTemplateEntity);

        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(true);

        doThrow(new PoliciesValidationFailedException("test"))
                .when(mockPolicyManager)
                .validatePolicy("policy");

        // Run the test
        assertThatThrownBy(() -> test.addServicePolicy(createRequest))
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
        expectedResult.setServicePolicyId(newPolicyId);
        expectedResult.setPolicy(newPolicy);
        expectedResult.setServiceTemplateId(serviceTemplateId);
        expectedResult.setEnabled(false);
        expectedResult.setCreatedTime(createdTime);

        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(serviceTemplateId);
        serviceTemplateEntity.setServiceVendor(serviceVendor);
        final ServicePolicyEntity existingPolicy = new ServicePolicyEntity();
        existingPolicy.setId(policyId);
        existingPolicy.setPolicy("policy");
        existingPolicy.setEnabled(false);
        existingPolicy.setServiceTemplate(serviceTemplateEntity);
        serviceTemplateEntity.setServicePolicyList(List.of(existingPolicy));
        when(mockServiceTemplateStorage.getServiceTemplateById(serviceTemplateId))
                .thenReturn(serviceTemplateEntity);

        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(true);
        // Run the test
        assertThatThrownBy(() -> test.addServicePolicy(createRequest))
                .isInstanceOf(PolicyDuplicateException.class);
    }

    @Test
    void testUpdateServicePolicy() {
        final String updatePolicy = "updatePolicy";
        // Setup
        final ServicePolicyUpdateRequest updateRequest = new ServicePolicyUpdateRequest();
        updateRequest.setPolicy(updatePolicy);
        updateRequest.setEnabled(true);

        final ServicePolicy expectedResult = new ServicePolicy();
        expectedResult.setServicePolicyId(policyId);
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
        existingTemplate.setServiceVendor(serviceVendor);
        existingTemplate.setServicePolicyList(List.of(existingPolicy));
        existingPolicy.setServiceTemplate(existingTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(existingPolicy);
        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(true);

        // Configure DatabaseServicePolicyStorage.storeAndFlush(...).
        final ServicePolicyEntity servicePolicyEntity = new ServicePolicyEntity();
        servicePolicyEntity.setId(policyId);
        servicePolicyEntity.setPolicy(updatePolicy);
        servicePolicyEntity.setEnabled(true);
        final ServiceTemplateEntity serviceTemplate = new ServiceTemplateEntity();
        serviceTemplate.setId(serviceTemplateId);
        serviceTemplate.setServiceVendor(serviceVendor);
        serviceTemplate.setServicePolicyList(List.of(servicePolicyEntity));
        servicePolicyEntity.setServiceTemplate(serviceTemplate);

        when(mockServicePolicyStorage.storeAndFlush(any())).thenReturn(servicePolicyEntity);
        // Run the test
        final ServicePolicy result = test.updateServicePolicy(policyId, updateRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testUpdateServicePolicyWithFlavorNameList() {
        final String updatePolicy = "updatePolicy";
        final List<String> flavorNameList = List.of("flavor1", "flavor2");
        // Setup
        final ServicePolicyUpdateRequest updateRequest = new ServicePolicyUpdateRequest();
        updateRequest.setFlavorNames(flavorNameList);
        updateRequest.setPolicy(updatePolicy);
        updateRequest.setEnabled(true);

        final ServicePolicy expectedResult = new ServicePolicy();
        expectedResult.setServicePolicyId(policyId);
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
        existingTemplate.setServiceVendor(serviceVendor);
        ServiceFlavorWithPrice flavor1 = new ServiceFlavorWithPrice();
        flavor1.setName("flavor1");
        ServiceFlavorWithPrice flavor2 = new ServiceFlavorWithPrice();
        flavor2.setName("flavor2");
        FlavorsWithPrice flavors = new FlavorsWithPrice();
        ModificationImpact modificationImpact = new ModificationImpact();
        modificationImpact.setIsDataLost(false);
        modificationImpact.setIsServiceInterrupted(false);
        flavors.setServiceFlavors(List.of(flavor1, flavor2));
        flavors.setModificationImpact(modificationImpact);
        final Ocl ocl = new Ocl();
        ocl.setFlavors(flavors);
        existingTemplate.setOcl(ocl);
        existingTemplate.setServicePolicyList(List.of(existingPolicy));
        existingPolicy.setServiceTemplate(existingTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(existingPolicy);
        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(true);

        // Configure DatabaseServicePolicyStorage.storeAndFlush(...).
        final ServicePolicyEntity servicePolicyEntity = new ServicePolicyEntity();
        servicePolicyEntity.setId(policyId);
        servicePolicyEntity.setFlavorNames(StringUtils.join(flavorNameList, ","));
        servicePolicyEntity.setPolicy(updatePolicy);
        servicePolicyEntity.setEnabled(true);
        final ServiceTemplateEntity serviceTemplate = new ServiceTemplateEntity();
        serviceTemplate.setId(serviceTemplateId);
        serviceTemplate.setServiceVendor(serviceVendor);
        serviceTemplate.setOcl(ocl);
        serviceTemplate.setServicePolicyList(List.of(servicePolicyEntity));
        servicePolicyEntity.setServiceTemplate(serviceTemplate);

        when(mockServicePolicyStorage.storeAndFlush(any())).thenReturn(servicePolicyEntity);
        // Run the test
        final ServicePolicy result = test.updateServicePolicy(policyId, updateRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testUpdateServicePolicyWithFlavorName_ThrowsFlavorInvalidException() {
        final String updatePolicy = "updatePolicy";
        final List<String> flavorNameList = List.of("flavor1", "invalid_flavor");
        // Setup
        final ServicePolicyUpdateRequest updateRequest = new ServicePolicyUpdateRequest();
        updateRequest.setFlavorNames(flavorNameList);
        updateRequest.setPolicy(updatePolicy);
        updateRequest.setEnabled(true);

        final ServicePolicy expectedResult = new ServicePolicy();
        expectedResult.setServicePolicyId(policyId);
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
        existingTemplate.setServiceVendor(serviceVendor);
        ServiceFlavorWithPrice flavor1 = new ServiceFlavorWithPrice();
        flavor1.setName("flavor1");
        ServiceFlavorWithPrice flavor2 = new ServiceFlavorWithPrice();
        flavor2.setName("flavor1");
        FlavorsWithPrice flavors = new FlavorsWithPrice();
        ModificationImpact modificationImpact = new ModificationImpact();
        modificationImpact.setIsDataLost(false);
        modificationImpact.setIsServiceInterrupted(false);
        flavors.setServiceFlavors(List.of(flavor1, flavor2));
        flavors.setModificationImpact(modificationImpact);
        final Ocl ocl = new Ocl();
        ocl.setFlavors(flavors);
        existingTemplate.setOcl(ocl);
        existingTemplate.setServicePolicyList(List.of(existingPolicy));
        existingPolicy.setServiceTemplate(existingTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(existingPolicy);
        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(true);

        assertThatThrownBy(() -> test.updateServicePolicy(policyId, updateRequest))
                .isInstanceOf(FlavorInvalidException.class);
    }

    @Test
    void testUpdateServicePolicy_ThrowsPolicyNotFoundExceptionException() {
        final String updatePolicy = "updatePolicy";
        // Setup
        final ServicePolicyUpdateRequest updateRequest = new ServicePolicyUpdateRequest();
        updateRequest.setPolicy(updatePolicy);
        updateRequest.setEnabled(true);
        // Configure DatabaseServiceTemplateStorage.getServiceTemplateById(...).
        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(null);

        // Run the test
        assertThatThrownBy(() -> test.updateServicePolicy(policyId, updateRequest))
                .isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    void testUpdateServicePolicy_ThrowsAccessDeniedException() {
        final String updatePolicy = "updatePolicy";
        // Setup
        final ServicePolicyUpdateRequest updateRequest = new ServicePolicyUpdateRequest();
        updateRequest.setPolicy(updatePolicy);
        updateRequest.setEnabled(true);

        // Configure DatabaseServicePolicyStorage.findPolicyById(...).
        final ServicePolicyEntity existingPolicy = new ServicePolicyEntity();
        existingPolicy.setId(policyId);
        existingPolicy.setPolicy("policy");
        existingPolicy.setEnabled(false);
        final ServiceTemplateEntity existingTemplate = new ServiceTemplateEntity();
        existingTemplate.setId(serviceTemplateId);
        existingTemplate.setServiceVendor(serviceVendor);
        existingTemplate.setServicePolicyList(List.of(existingPolicy));
        existingPolicy.setServiceTemplate(existingTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(existingPolicy);
        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(false);

        // Run the test
        assertThatThrownBy(() -> test.updateServicePolicy(policyId, updateRequest))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void testUpdateServicePolicy_ThrowsPoliciesValidationFailedException() {
        // Setup
        final String updatePolicy = "updatePolicy";
        // Setup
        final ServicePolicyUpdateRequest updateRequest = new ServicePolicyUpdateRequest();
        updateRequest.setPolicy(updatePolicy);
        updateRequest.setEnabled(true);

        final ServicePolicy expectedResult = new ServicePolicy();
        expectedResult.setServicePolicyId(policyId);
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
        existingTemplate.setServiceVendor(serviceVendor);
        existingTemplate.setServicePolicyList(List.of(existingPolicy));
        existingPolicy.setServiceTemplate(existingTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(existingPolicy);
        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(true);
        doThrow(new PoliciesValidationFailedException("error"))
                .when(mockPolicyManager)
                .validatePolicy(updatePolicy);

        // Run the test
        assertThatThrownBy(() -> test.updateServicePolicy(policyId, updateRequest))
                .isInstanceOf(PoliciesValidationFailedException.class);
    }

    @Test
    void testUpdateServicePolicy_ThrowsPolicyDuplicateException() {
        final String updatePolicy = "updatePolicy";
        // Setup
        final ServicePolicyUpdateRequest updateRequest = new ServicePolicyUpdateRequest();
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
        existingTemplate.setServiceVendor(serviceVendor);
        existingTemplate.setServicePolicyList(List.of(existingPolicy1, existingPolicy2));
        existingPolicy1.setServiceTemplate(existingTemplate);
        existingPolicy2.setServiceTemplate(existingTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(existingPolicy1);
        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(true);

        // Run the test
        assertThatThrownBy(() -> test.updateServicePolicy(policyId, updateRequest))
                .isInstanceOf(PolicyDuplicateException.class);
    }

    @Test
    void testGetServicePolicyDetails() {
        // Setup
        final ServicePolicy expectedResult = new ServicePolicy();
        expectedResult.setServicePolicyId(policyId);
        expectedResult.setPolicy("policy");
        expectedResult.setServiceTemplateId(serviceTemplateId);
        expectedResult.setEnabled(false);
        expectedResult.setCreatedTime(createdTime);
        // Configure DatabaseServicePolicyStorage.findPolicyById(...).
        final ServicePolicyEntity servicePolicyEntity = new ServicePolicyEntity();
        servicePolicyEntity.setId(policyId);
        servicePolicyEntity.setPolicy("policy");
        servicePolicyEntity.setCreatedTime(createdTime);
        servicePolicyEntity.setEnabled(false);
        final ServiceTemplateEntity serviceTemplate = new ServiceTemplateEntity();
        serviceTemplate.setId(serviceTemplateId);
        serviceTemplate.setServiceVendor(serviceVendor);
        serviceTemplate.setServicePolicyList(List.of(servicePolicyEntity));
        servicePolicyEntity.setServiceTemplate(serviceTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(servicePolicyEntity);
        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(true);

        // Run the test
        final ServicePolicy result = test.getServicePolicyDetails(policyId);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetServicePolicyDetails_ThrowsAccessDeniedException() {
        // Setup
        final ServicePolicy expectedResult = new ServicePolicy();
        expectedResult.setServicePolicyId(policyId);
        expectedResult.setPolicy("policy");
        expectedResult.setServiceTemplateId(serviceTemplateId);
        expectedResult.setEnabled(false);
        expectedResult.setCreatedTime(createdTime);
        // Configure DatabaseServicePolicyStorage.findPolicyById(...).
        final ServicePolicyEntity servicePolicyEntity = new ServicePolicyEntity();
        servicePolicyEntity.setId(policyId);
        servicePolicyEntity.setPolicy("policy");
        servicePolicyEntity.setCreatedTime(createdTime);
        servicePolicyEntity.setEnabled(false);
        final ServiceTemplateEntity serviceTemplate = new ServiceTemplateEntity();
        serviceTemplate.setId(serviceTemplateId);
        serviceTemplate.setServiceVendor(serviceVendor);
        serviceTemplate.setServicePolicyList(List.of(servicePolicyEntity));
        servicePolicyEntity.setServiceTemplate(serviceTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(servicePolicyEntity);
        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(false);

        // Run the test
        assertThatThrownBy(() -> test.getServicePolicyDetails(policyId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void testGetServicePolicyDetails_ThrowsPolicyNotFoundException() {
        // Setup
        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(null);
        // Run the test
        assertThatThrownBy(() -> test.getServicePolicyDetails(policyId))
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
        serviceTemplate.setServiceVendor(serviceVendor);
        serviceTemplate.setServicePolicyList(List.of(servicePolicyEntity));
        servicePolicyEntity.setServiceTemplate(serviceTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(servicePolicyEntity);
        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(true);

        // Run the test
        test.deleteServicePolicy(policyId);

        // Verify the results
        verify(mockServicePolicyStorage).deletePolicyById(policyId);
    }

    @Test
    void testDeleteServicePolicy_ThrowsAccessDeniedException() {
        // Setup
        final ServicePolicy expectedResult = new ServicePolicy();
        expectedResult.setServicePolicyId(policyId);
        expectedResult.setPolicy("policy");
        expectedResult.setServiceTemplateId(serviceTemplateId);
        expectedResult.setEnabled(false);
        expectedResult.setCreatedTime(createdTime);
        // Configure DatabaseServicePolicyStorage.findPolicyById(...).
        final ServicePolicyEntity servicePolicyEntity = new ServicePolicyEntity();
        servicePolicyEntity.setId(policyId);
        servicePolicyEntity.setPolicy("policy");
        servicePolicyEntity.setCreatedTime(createdTime);
        servicePolicyEntity.setEnabled(false);
        final ServiceTemplateEntity serviceTemplate = new ServiceTemplateEntity();
        serviceTemplate.setId(serviceTemplateId);
        serviceTemplate.setServiceVendor(serviceVendor);
        serviceTemplate.setServicePolicyList(List.of(servicePolicyEntity));
        servicePolicyEntity.setServiceTemplate(serviceTemplate);

        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(servicePolicyEntity);
        when(mockUserServiceHelper.currentUserCanManageIsv(serviceVendor)).thenReturn(false);

        // Run the test
        assertThatThrownBy(() -> test.deleteServicePolicy(policyId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void testDeleteServicePolicy_ThrowsPolicyNotFoundException() {
        // Setup
        when(mockServicePolicyStorage.findPolicyById(policyId)).thenReturn(null);
        // Run the test
        assertThatThrownBy(() -> test.deleteServicePolicy(policyId))
                .isInstanceOf(PolicyNotFoundException.class);
    }
}
