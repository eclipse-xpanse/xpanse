/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.policy.policyman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.policy.DatabasePolicyStorage;
import org.eclipse.xpanse.modules.database.policy.PolicyEntity;
import org.eclipse.xpanse.modules.models.policy.PolicyCreateRequest;
import org.eclipse.xpanse.modules.models.policy.PolicyQueryRequest;
import org.eclipse.xpanse.modules.models.policy.PolicyUpdateRequest;
import org.eclipse.xpanse.modules.models.policy.PolicyVo;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesEvaluationFailedException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesValidationFailedException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyDuplicateException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyNotFoundException;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.AdminApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.PoliciesEvaluationApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.api.PoliciesValidateApi;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalCmdList;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalResult;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.SystemStatus;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.ValidatePolicyList;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.ValidateResponse;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class PolicyManagerTest {

    private final UUID id = UUID.randomUUID();

    private final String userId = "userId";

    @Mock
    private DatabasePolicyStorage mockPolicyStorage;
    @Mock
    private AdminApi mockAdminApi;
    @Mock
    private PoliciesValidateApi mockPoliciesValidateApi;
    @Mock
    private PoliciesEvaluationApi mockPoliciesEvaluationApi;
    @Mock
    private IdentityProviderManager mockIdentityProviderManager;

    @InjectMocks
    private PolicyManager policyManagerUnderTest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(policyManagerUnderTest, "policyManBaseUrl", "endpoint");
    }

    @Test
    void testGetPolicyManStatus() {
        // Setup
        final BackendSystemStatus expectedResult = new BackendSystemStatus();
        expectedResult.setBackendSystemType(BackendSystemType.POLICY_MAN);
        expectedResult.setName(BackendSystemType.POLICY_MAN.toValue());
        expectedResult.setHealthStatus(HealthStatus.OK);
        expectedResult.setEndpoint("endpoint");

        // Configure AdminApi.healthGet(...).
        final SystemStatus systemStatus = new SystemStatus();
        systemStatus.setHealthStatus(
                org.eclipse.xpanse.modules.policy.policyman.generated.model.HealthStatus.healthOK);
        when(mockAdminApi.healthGet()).thenReturn(systemStatus);

        // Run the test
        final BackendSystemStatus result = policyManagerUnderTest.getPolicyManStatus();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetPolicyManStatus_AdminApiThrowsRestClientException() {
        // Setup
        final BackendSystemStatus expectedResult = new BackendSystemStatus();
        expectedResult.setBackendSystemType(BackendSystemType.POLICY_MAN);
        expectedResult.setName(BackendSystemType.POLICY_MAN.toValue());
        expectedResult.setHealthStatus(HealthStatus.NOK);
        expectedResult.setEndpoint("endpoint");

        when(mockAdminApi.healthGet()).thenThrow(RestClientException.class);

        // Run the test
        final BackendSystemStatus result = policyManagerUnderTest.getPolicyManStatus();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testListPolicies() {
        // Setup
        final PolicyQueryRequest queryModel = new PolicyQueryRequest();

        queryModel.setUserId(userId);
        queryModel.setCsp(Csp.HUAWEI);
        queryModel.setPolicy("policy");

        final PolicyVo policyVo = new PolicyVo();
        policyVo.setId(id);
        policyVo.setPolicy("policy");
        policyVo.setCsp(Csp.HUAWEI);
        policyVo.setEnabled(true);
        final List<PolicyVo> expectedResult = List.of(policyVo);
        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        // Configure DatabasePolicyStorage.listPolicies(...).
        final PolicyEntity policyEntity = new PolicyEntity();
        policyEntity.setId(id);
        policyEntity.setUserId(userId);
        policyEntity.setPolicy("policy");
        policyEntity.setCsp(Csp.HUAWEI);
        policyEntity.setEnabled(true);
        final List<PolicyEntity> policyEntities = List.of(policyEntity);
        final PolicyQueryRequest queryModel1 = new PolicyQueryRequest();
        queryModel1.setUserId(userId);
        queryModel1.setCsp(Csp.HUAWEI);
        queryModel1.setPolicy("policy");
        when(mockPolicyStorage.listPolicies(queryModel1)).thenReturn(policyEntities);

        // Run the test
        final List<PolicyVo> result = policyManagerUnderTest.listPolicies(queryModel);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testListPolicies_ReturnsNoItems() {
        // Setup
        final PolicyQueryRequest queryModel = new PolicyQueryRequest();

        queryModel.setUserId(userId);
        queryModel.setCsp(Csp.HUAWEI);
        queryModel.setPolicy("policy");

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        // Configure DatabasePolicyStorage.listPolicies(...).
        final PolicyQueryRequest queryModel1 = new PolicyQueryRequest();

        queryModel1.setUserId(userId);
        queryModel1.setCsp(Csp.HUAWEI);
        queryModel1.setPolicy("policy");
        when(mockPolicyStorage.listPolicies(queryModel1)).thenReturn(Collections.emptyList());

        // Run the test
        final List<PolicyVo> result = policyManagerUnderTest.listPolicies(queryModel);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testAddPolicy() {
        // Setup
        final PolicyCreateRequest createRequest = new PolicyCreateRequest();
        createRequest.setCsp(Csp.HUAWEI);
        createRequest.setPolicy("policy");

        final PolicyVo expectedResult = new PolicyVo();
        expectedResult.setId(id);
        expectedResult.setPolicy("policy");
        expectedResult.setCsp(Csp.HUAWEI);
        expectedResult.setEnabled(true);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        // Configure DatabasePolicyStorage.listPolicies(...).
        final PolicyQueryRequest queryModel = new PolicyQueryRequest();
        queryModel.setUserId(userId);
        queryModel.setCsp(Csp.HUAWEI);
        queryModel.setPolicy("policy");
        when(mockPolicyStorage.listPolicies(queryModel)).thenReturn(Collections.emptyList());

        // Configure PoliciesValidateApi.validatePoliciesPost(...).
        final ValidateResponse validateResponse = new ValidateResponse();
        validateResponse.setIsSuccessful(true);

        final ValidatePolicyList policyList = new ValidatePolicyList();
        policyList.setPolicyList(List.of("policy"));
        when(mockPoliciesValidateApi.validatePoliciesPost(policyList))
                .thenReturn(validateResponse);


        // Configure DatabasePolicyStorage.store(...).
        final PolicyEntity policyEntity1 = new PolicyEntity();
        policyEntity1.setId(id);
        policyEntity1.setUserId(userId);
        policyEntity1.setPolicy("policy");
        policyEntity1.setCsp(Csp.HUAWEI);
        policyEntity1.setEnabled(true);

        when(mockPolicyStorage.store(any(PolicyEntity.class))).thenReturn(policyEntity1);

        // Run the test
        final PolicyVo result = policyManagerUnderTest.addPolicy(createRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);

    }

    @Test
    void testAddPolicy_ReturnsNull() {
        // Setup
        final PolicyCreateRequest createRequest = new PolicyCreateRequest();
        createRequest.setCsp(Csp.HUAWEI);
        createRequest.setPolicy("policy");

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        // Configure DatabasePolicyStorage.listPolicies(...).
        final PolicyQueryRequest queryModel = new PolicyQueryRequest();
        queryModel.setUserId(userId);
        queryModel.setCsp(Csp.HUAWEI);
        queryModel.setPolicy("policy");
        when(mockPolicyStorage.listPolicies(queryModel)).thenReturn(Collections.emptyList());

        // Configure PoliciesValidateApi.validatePoliciesPost(...).
        final ValidateResponse validateResponse = new ValidateResponse();
        validateResponse.setIsSuccessful(true);

        final ValidatePolicyList policyList = new ValidatePolicyList();
        policyList.setPolicyList(List.of("policy"));
        when(mockPoliciesValidateApi.validatePoliciesPost(policyList))
                .thenReturn(validateResponse);

        // Configure DatabasePolicyStorage.store(...).

        when(mockPolicyStorage.store(any(PolicyEntity.class))).thenReturn(null);

        // Run the test
        final PolicyVo result = policyManagerUnderTest.addPolicy(createRequest);

        // Verify the results
        assertThat(result).isEqualTo(null);

    }

    @Test
    void testAddPolicy_ThrowsPolicyDuplicateException() {
        // Setup
        final PolicyCreateRequest createRequest = new PolicyCreateRequest();
        createRequest.setCsp(Csp.HUAWEI);
        createRequest.setPolicy("policy");
        createRequest.setEnabled(true);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        // Configure DatabasePolicyStorage.listPolicies(...).
        final PolicyEntity policyEntity = new PolicyEntity();
        policyEntity.setId(id);
        policyEntity.setUserId(userId);
        policyEntity.setPolicy("policy");
        policyEntity.setCsp(Csp.HUAWEI);
        policyEntity.setEnabled(true);
        final List<PolicyEntity> policyEntities = List.of(policyEntity);
        final PolicyQueryRequest queryModel = new PolicyQueryRequest();

        queryModel.setUserId(userId);
        queryModel.setCsp(Csp.HUAWEI);
        queryModel.setPolicy("policy");
        when(mockPolicyStorage.listPolicies(queryModel)).thenReturn(policyEntities);

        final ValidateResponse validateResponse = new ValidateResponse();
        validateResponse.setIsSuccessful(true);

        final ValidatePolicyList policyList = new ValidatePolicyList();
        policyList.setPolicyList(List.of("policy"));
        when(mockPoliciesValidateApi.validatePoliciesPost(policyList))
                .thenReturn(validateResponse);

        // Run the test
        assertThatThrownBy(() -> policyManagerUnderTest.addPolicy(createRequest))
                .isInstanceOf(PolicyDuplicateException.class);
    }

    @Test
    void testAddPolicy_ThrowsPoliciesValidationFailedException() {
        // Setup
        final PolicyCreateRequest createRequest = new PolicyCreateRequest();
        createRequest.setCsp(Csp.HUAWEI);
        createRequest.setPolicy("policy");
        createRequest.setEnabled(true);

        final PolicyVo expectedResult = new PolicyVo();
        expectedResult.setId(id);
        expectedResult.setPolicy("policy");
        expectedResult.setCsp(Csp.HUAWEI);
        expectedResult.setEnabled(true);

        // Configure PoliciesValidateApi.validatePoliciesPost(...).
        final ValidatePolicyList policyList = new ValidatePolicyList();
        policyList.setPolicyList(List.of("policy"));
        when(mockPoliciesValidateApi.validatePoliciesPost(policyList))
                .thenThrow(RestClientException.class);

        // Run the test
        assertThatThrownBy(() -> policyManagerUnderTest.addPolicy(createRequest))
                .isInstanceOf(PoliciesValidationFailedException.class);
    }

    @Test
    void testUpdatePolicy_UpdatePolicyText() {
        // Setup
        final PolicyUpdateRequest updateRequest = new PolicyUpdateRequest();
        updateRequest.setId(id);
        updateRequest.setPolicy("policy_update");
        updateRequest.setEnabled(false);

        final PolicyVo expectedResult = new PolicyVo();
        expectedResult.setId(id);
        expectedResult.setPolicy("policy_update");
        expectedResult.setCsp(Csp.HUAWEI);
        expectedResult.setEnabled(false);

        // Configure DatabasePolicyStorage.findPolicyById(...).
        final PolicyEntity policyEntity = new PolicyEntity();
        policyEntity.setId(id);
        policyEntity.setUserId(userId);
        policyEntity.setPolicy("policy");
        policyEntity.setCsp(Csp.HUAWEI);
        policyEntity.setEnabled(true);
        when(mockPolicyStorage.findPolicyById(id)).thenReturn(policyEntity);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        // Configure DatabasePolicyStorage.listPolicies(...).
        final PolicyQueryRequest queryModel = new PolicyQueryRequest();

        queryModel.setUserId(userId);
        queryModel.setCsp(Csp.HUAWEI);
        queryModel.setPolicy("policy_update");

        // Configure DatabasePolicyStorage.store(...).
        final PolicyEntity updatedPolicyEntity = new PolicyEntity();
        updatedPolicyEntity.setId(id);
        updatedPolicyEntity.setUserId(userId);
        updatedPolicyEntity.setPolicy("policy_update");
        updatedPolicyEntity.setCsp(Csp.HUAWEI);
        updatedPolicyEntity.setEnabled(false);
        when(mockPolicyStorage.store(updatedPolicyEntity)).thenReturn(updatedPolicyEntity);

        // Configure PoliciesValidateApi.validatePoliciesPost(...).
        final ValidateResponse validateResponse = new ValidateResponse();
        validateResponse.setIsSuccessful(true);
        final ValidatePolicyList policyList = new ValidatePolicyList();
        policyList.setPolicyList(List.of("policy_update"));
        when(mockPoliciesValidateApi.validatePoliciesPost(policyList))
                .thenReturn(validateResponse);

        // Run the test
        final PolicyVo result = policyManagerUnderTest.updatePolicy(updateRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testUpdatePolicy_UpdateCsp() {
        // Setup
        final PolicyUpdateRequest updateRequest = new PolicyUpdateRequest();
        updateRequest.setId(id);
        updateRequest.setCsp(Csp.OPENSTACK);
        updateRequest.setEnabled(false);

        final PolicyVo expectedResult = new PolicyVo();
        expectedResult.setId(id);
        expectedResult.setPolicy("policy");
        expectedResult.setCsp(Csp.OPENSTACK);
        expectedResult.setEnabled(false);

        // Configure DatabasePolicyStorage.findPolicyById(...).
        final PolicyEntity policyEntity = new PolicyEntity();
        policyEntity.setId(id);
        policyEntity.setUserId(userId);
        policyEntity.setPolicy("policy");
        policyEntity.setCsp(Csp.HUAWEI);
        policyEntity.setEnabled(true);
        when(mockPolicyStorage.findPolicyById(id)).thenReturn(policyEntity);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        // Configure DatabasePolicyStorage.listPolicies(...).
        final PolicyQueryRequest queryModel = new PolicyQueryRequest();

        queryModel.setUserId(userId);
        queryModel.setCsp(Csp.HUAWEI);
        queryModel.setPolicy("policy");

        // Configure DatabasePolicyStorage.store(...).
        final PolicyEntity updatedPolicyEntity = new PolicyEntity();
        updatedPolicyEntity.setId(id);
        updatedPolicyEntity.setUserId(userId);
        updatedPolicyEntity.setPolicy("policy");
        updatedPolicyEntity.setCsp(Csp.OPENSTACK);
        updatedPolicyEntity.setEnabled(false);
        when(mockPolicyStorage.store(updatedPolicyEntity)).thenReturn(updatedPolicyEntity);

        // Run the test
        final PolicyVo result = policyManagerUnderTest.updatePolicy(updateRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testUpdatePolicy_ThrowsPolicyNotFoundException() {
        // Setup
        final PolicyUpdateRequest updateRequest = new PolicyUpdateRequest();
        updateRequest.setId(id);
        updateRequest.setCsp(Csp.HUAWEI);
        updateRequest.setPolicy("policy");
        // Configure DatabasePolicyStorage.findPolicyById(...).
        when(mockPolicyStorage.findPolicyById(id)).thenReturn(null);
        // Run the test
        assertThatThrownBy(() -> policyManagerUnderTest.updatePolicy(updateRequest))
                .isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    void testUpdatePolicy_ThrowsAccessDeniedException() {
        // Setup
        final PolicyUpdateRequest updateRequest = new PolicyUpdateRequest();
        updateRequest.setId(id);
        updateRequest.setCsp(Csp.HUAWEI);
        updateRequest.setPolicy("policy");

        // Configure DatabasePolicyStorage.findPolicyById(...).
        final PolicyEntity policyEntity = new PolicyEntity();
        policyEntity.setId(id);
        policyEntity.setUserId(userId);
        policyEntity.setPolicy("policy");
        policyEntity.setCsp(Csp.HUAWEI);
        policyEntity.setEnabled(true);
        when(mockPolicyStorage.findPolicyById(id)).thenReturn(policyEntity);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(
                Optional.of("userId2"));

        // Run the test
        assertThatThrownBy(() -> policyManagerUnderTest.updatePolicy(updateRequest))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void testUpdatePolicy_ThrowsPolicyDuplicateException() {
        // Setup
        final PolicyUpdateRequest updateRequest = new PolicyUpdateRequest();
        UUID id1 = UUID.randomUUID();
        updateRequest.setId(id1);
        updateRequest.setCsp(Csp.OPENSTACK);
        updateRequest.setPolicy("policy2");
        updateRequest.setEnabled(false);

        // Configure DatabasePolicyStorage.findPolicyById(...).
        final PolicyEntity policyEntity = new PolicyEntity();
        policyEntity.setId(id1);
        policyEntity.setUserId(userId);
        policyEntity.setPolicy("policy");
        policyEntity.setCsp(Csp.HUAWEI);
        policyEntity.setEnabled(true);

        final PolicyEntity policyEntity2 = new PolicyEntity();
        policyEntity2.setId(UUID.randomUUID());
        policyEntity2.setUserId(userId);
        policyEntity2.setPolicy("policy2");
        policyEntity2.setCsp(Csp.OPENSTACK);
        policyEntity2.setEnabled(true);

        when(mockPolicyStorage.findPolicyById(id1)).thenReturn(policyEntity);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        final PolicyQueryRequest queryModel = new PolicyQueryRequest();

        queryModel.setUserId(userId);
        queryModel.setCsp(Csp.OPENSTACK);
        queryModel.setPolicy("policy2");

        when(mockPolicyStorage.listPolicies(queryModel)).thenReturn(List.of(policyEntity2));

        // Configure PoliciesValidateApi.validatePoliciesPost(...).
        final ValidateResponse validateResponse = new ValidateResponse();
        validateResponse.setIsSuccessful(true);
        final ValidatePolicyList policyList = new ValidatePolicyList();
        policyList.setPolicyList(List.of("policy2"));
        when(mockPoliciesValidateApi.validatePoliciesPost(policyList))
                .thenReturn(validateResponse);

        // Run the test
        assertThatThrownBy(() -> policyManagerUnderTest.updatePolicy(updateRequest))
                .isInstanceOf(PolicyDuplicateException.class);


    }

    @Test
    void testGetPolicyDetails() {
        // Setup
        final PolicyVo expectedResult = new PolicyVo();
        expectedResult.setId(id);
        expectedResult.setPolicy("policy");
        expectedResult.setCsp(Csp.HUAWEI);
        expectedResult.setEnabled(true);

        // Configure DatabasePolicyStorage.findPolicyById(...).
        final PolicyEntity policyEntity = new PolicyEntity();
        policyEntity.setId(id);
        policyEntity.setUserId(userId);
        policyEntity.setPolicy("policy");
        policyEntity.setCsp(Csp.HUAWEI);
        policyEntity.setEnabled(true);
        when(mockPolicyStorage.findPolicyById(id)).thenReturn(policyEntity);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        // Run the test
        final PolicyVo result = policyManagerUnderTest.getPolicyDetails(id);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetPolicyDetails_ThrowsPolicyNotFoundException() {
        // Setup
        final PolicyVo expectedResult = new PolicyVo();
        expectedResult.setId(id);
        expectedResult.setPolicy("policy");
        expectedResult.setCsp(Csp.HUAWEI);
        expectedResult.setEnabled(true);

        // Configure DatabasePolicyStorage.findPolicyById(...).
        when(mockPolicyStorage.findPolicyById(id)).thenReturn(null);

        // Run the test
        assertThatThrownBy(() -> policyManagerUnderTest.getPolicyDetails(id))
                .isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    void testGetPolicyDetails_ThrowsAccessDeniedException() {
        // Setup
        final PolicyVo expectedResult = new PolicyVo();
        expectedResult.setId(id);
        expectedResult.setPolicy("policy");
        expectedResult.setCsp(Csp.HUAWEI);
        expectedResult.setEnabled(true);

        // Configure DatabasePolicyStorage.findPolicyById(...).
        final PolicyEntity policyEntity = new PolicyEntity();
        policyEntity.setId(id);
        policyEntity.setUserId(userId);
        policyEntity.setPolicy("policy");
        policyEntity.setCsp(Csp.HUAWEI);
        policyEntity.setEnabled(true);
        when(mockPolicyStorage.findPolicyById(id)).thenReturn(policyEntity);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(
                Optional.of("userId2"));

        // Run the test
        assertThatThrownBy(() -> policyManagerUnderTest.getPolicyDetails(id))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void testDeletePolicy() {
        // Setup
        // Configure DatabasePolicyStorage.findPolicyById(...).
        final PolicyEntity policyEntity = new PolicyEntity();
        policyEntity.setId(id);
        policyEntity.setUserId(userId);
        policyEntity.setPolicy("policy");
        policyEntity.setCsp(Csp.HUAWEI);
        policyEntity.setEnabled(true);
        when(mockPolicyStorage.findPolicyById(id)).thenReturn(policyEntity);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        // Run the test
        policyManagerUnderTest.deletePolicy(id);

        // Verify the results
        verify(mockPolicyStorage).deletePolicyById(id);
    }

    @Test
    void testDeletePolicy_ThrowsAccessDeniedException() {
        // Setup
        // Configure DatabasePolicyStorage.findPolicyById(...).
        final PolicyEntity policyEntity = new PolicyEntity();
        policyEntity.setId(id);
        policyEntity.setUserId(userId);
        policyEntity.setPolicy("policy");
        policyEntity.setCsp(Csp.HUAWEI);
        policyEntity.setEnabled(true);
        when(mockPolicyStorage.findPolicyById(id)).thenReturn(policyEntity);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(
                Optional.of("userId2"));

        // Run the test
        assertThatThrownBy(() -> policyManagerUnderTest.deletePolicy(id))
                .isInstanceOf(AccessDeniedException.class);
    }


    @Test
    void testDeletePolicy_ThrowsPolicyNotFoundException() {
        // Setup
        // Configure DatabasePolicyStorage.findPolicyById(...).
        when(mockPolicyStorage.findPolicyById(id)).thenReturn(null);

        // Run the test
        assertThatThrownBy(() -> policyManagerUnderTest.deletePolicy(id))
                .isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    void testEvaluatePolicies() {
        // Setup
        // Configure PoliciesEvaluationApi.evaluatePoliciesPost(...).
        final EvalResult evalResult = new EvalResult();
        evalResult.setIsSuccessful(true);
        final EvalCmdList cmdList = new EvalCmdList();
        cmdList.setInput("input");
        cmdList.setPolicyList(List.of("policy"));
        when(mockPoliciesEvaluationApi.evaluatePoliciesPost(cmdList)).thenReturn(evalResult);

        // Run the test
        policyManagerUnderTest.evaluatePolicies(cmdList.getPolicyList(), cmdList.getInput());

    }

    @Test
    void testEvaluatePolicies_PoliciesEvaluationFailed() {
        // Setup
        final EvalResult evalResult = new EvalResult();
        evalResult.setIsSuccessful(false);
        evalResult.setPolicy("policy");
        evalResult.setInput("input");

        // Configure PoliciesEvaluationApi.evaluatePoliciesPost(...).
        final EvalCmdList cmdList = new EvalCmdList();
        cmdList.setInput("input");
        cmdList.setPolicyList(List.of("value"));
        when(mockPoliciesEvaluationApi.evaluatePoliciesPost(cmdList))
                .thenReturn(evalResult);

        // Run the test
        Assertions.assertThrows(PoliciesEvaluationFailedException.class,
                () -> policyManagerUnderTest.evaluatePolicies(List.of("value"), "input"));
    }

    @Test
    void testEvaluatePolicies_PoliciesEvaluationApiThrowsRestClientException() {
        // Setup
        // Configure PoliciesEvaluationApi.evaluatePoliciesPost(...).
        final EvalCmdList cmdList = new EvalCmdList();
        cmdList.setInput("input");
        cmdList.setPolicyList(List.of("value"));
        when(mockPoliciesEvaluationApi.evaluatePoliciesPost(cmdList))
                .thenThrow(new RestClientException("error"));

        // Run the test
        Assertions.assertThrows(PoliciesEvaluationFailedException.class,
                () -> policyManagerUnderTest.evaluatePolicies(List.of("value"), "input"));
    }
}
