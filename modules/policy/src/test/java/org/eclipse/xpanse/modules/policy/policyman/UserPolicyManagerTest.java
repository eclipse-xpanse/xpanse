package org.eclipse.xpanse.modules.policy.policyman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.userpolicy.DatabaseUserPolicyStorage;
import org.eclipse.xpanse.modules.database.userpolicy.UserPolicyEntity;
import org.eclipse.xpanse.modules.models.policy.exceptions.PoliciesValidationFailedException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyDuplicateException;
import org.eclipse.xpanse.modules.models.policy.exceptions.PolicyNotFoundException;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicy;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyCreateRequest;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyQueryRequest;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyUpdateRequest;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class UserPolicyManagerTest {

    private final UUID policyId = UUID.randomUUID();
    private final String userId = "userId";
    @Mock
    private PolicyManager mockPolicyManager;
    @Mock
    private IdentityProviderManager mockIdentityProviderManager;
    @Mock
    private DatabaseUserPolicyStorage mockUserPolicyStorage;
    @InjectMocks
    private UserPolicyManager userPolicyManagerUnderTest;

    @Test
    void testListUserPolicies() {
        // Setup
        final UserPolicyQueryRequest queryModel = new UserPolicyQueryRequest();

        queryModel.setUserId(userId);
        queryModel.setCsp(Csp.HUAWEI);
        queryModel.setPolicy("userPolicy");

        final UserPolicy userPolicy = new UserPolicy();
        userPolicy.setId(policyId);
        userPolicy.setPolicy("userPolicy");
        userPolicy.setCsp(Csp.HUAWEI);
        userPolicy.setEnabled(true);
        final List<UserPolicy> expectedResult = List.of(userPolicy);
        // Configure DatabaseUserPolicyStorage.listPolicies(...).
        final UserPolicyEntity userPolicyEntity = new UserPolicyEntity();
        userPolicyEntity.setId(policyId);
        userPolicyEntity.setUserId(userId);
        userPolicyEntity.setPolicy("userPolicy");
        userPolicyEntity.setCsp(Csp.HUAWEI);
        userPolicyEntity.setEnabled(true);
        final List<UserPolicyEntity> policyEntities = List.of(userPolicyEntity);
        final UserPolicyQueryRequest queryModel1 = new UserPolicyQueryRequest();
        queryModel1.setUserId(userId);
        queryModel1.setCsp(Csp.HUAWEI);
        queryModel1.setPolicy("userPolicy");
        when(mockUserPolicyStorage.listPolicies(queryModel1)).thenReturn(policyEntities);

        // Run the test
        final List<UserPolicy> result = userPolicyManagerUnderTest.listUserPolicies(queryModel);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testListUserPolicies_ReturnsNoItems() {
        // Setup
        final UserPolicyQueryRequest queryModel = new UserPolicyQueryRequest();
        queryModel.setUserId(userId);
        queryModel.setCsp(Csp.HUAWEI);
        queryModel.setPolicy("policy");

        // Configure DatabaseUserPolicyStorage.listPolicies(...).
        final UserPolicyQueryRequest queryModel1 = new UserPolicyQueryRequest();

        queryModel1.setUserId(userId);
        queryModel1.setCsp(Csp.HUAWEI);
        queryModel1.setPolicy("policy");
        when(mockUserPolicyStorage.listPolicies(queryModel1)).thenReturn(Collections.emptyList());

        // Run the test
        final List<UserPolicy> result = userPolicyManagerUnderTest.listUserPolicies(queryModel);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testAddUserUserPolicy() {
        // Setup
        final UserPolicyCreateRequest createRequest = new UserPolicyCreateRequest();
        createRequest.setCsp(Csp.HUAWEI);
        createRequest.setPolicy("policy");

        final UserPolicy expectedResult = new UserPolicy();
        expectedResult.setId(policyId);
        expectedResult.setPolicy("policy");
        expectedResult.setCsp(Csp.HUAWEI);
        expectedResult.setEnabled(true);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        // Configure DatabaseUserPolicyStorage.listPolicies(...).
        final UserPolicyQueryRequest queryModel = new UserPolicyQueryRequest();
        queryModel.setUserId(userId);
        queryModel.setCsp(Csp.HUAWEI);
        queryModel.setPolicy("policy");

        when(mockUserPolicyStorage.listPolicies(queryModel)).thenReturn(Collections.emptyList());

        // Configure DatabaseUserPolicyStorage.store(...).
        final UserPolicyEntity userPolicyEntity1 = new UserPolicyEntity();
        userPolicyEntity1.setId(policyId);
        userPolicyEntity1.setUserId(userId);
        userPolicyEntity1.setPolicy("policy");
        userPolicyEntity1.setCsp(Csp.HUAWEI);
        userPolicyEntity1.setEnabled(true);

        when(mockUserPolicyStorage.store(any(UserPolicyEntity.class))).thenReturn(userPolicyEntity1);

        // Run the test
        final UserPolicy result = userPolicyManagerUnderTest.addUserPolicy(createRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);

    }

    @Test
    void testAddUserPolicy_ThrowsPolicyDuplicateException() {
        // Setup
        final UserPolicyCreateRequest createRequest = new UserPolicyCreateRequest();
        createRequest.setCsp(Csp.HUAWEI);
        createRequest.setPolicy("policy");
        createRequest.setEnabled(true);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        // Configure DatabaseUserPolicyStorage.listPolicies(...).
        final UserPolicyEntity userPolicyEntity = new UserPolicyEntity();
        userPolicyEntity.setId(policyId);
        userPolicyEntity.setUserId(userId);
        userPolicyEntity.setPolicy("policy");
        userPolicyEntity.setCsp(Csp.HUAWEI);
        userPolicyEntity.setEnabled(true);
        final List<UserPolicyEntity> policyEntities = List.of(userPolicyEntity);
        final UserPolicyQueryRequest queryModel = new UserPolicyQueryRequest();
        queryModel.setUserId(userId);
        queryModel.setCsp(Csp.HUAWEI);
        queryModel.setPolicy("policy");

        when(mockUserPolicyStorage.listPolicies(queryModel)).thenReturn(policyEntities);
        // Run the test
        assertThatThrownBy(() -> userPolicyManagerUnderTest.addUserPolicy(createRequest))
                .isInstanceOf(PolicyDuplicateException.class);
    }

    @Test
    void testAddUserPolicy_ThrowsPoliciesValidationFailedException() {
        // Setup
        final UserPolicyCreateRequest createRequest = new UserPolicyCreateRequest();
        createRequest.setCsp(Csp.HUAWEI);
        createRequest.setPolicy("policy");
        createRequest.setEnabled(true);

        final UserPolicy expectedResult = new UserPolicy();
        expectedResult.setId(policyId);
        expectedResult.setPolicy("policy");
        expectedResult.setCsp(Csp.HUAWEI);
        expectedResult.setEnabled(true);

        doThrow(new PoliciesValidationFailedException("error")).when(mockPolicyManager)
                .validatePolicy("policy");

        // Run the test
        assertThatThrownBy(() -> userPolicyManagerUnderTest.addUserPolicy(createRequest))
                .isInstanceOf(PoliciesValidationFailedException.class);
    }

    @Test
    void testUpdateUserPolicy_UpdatePolicyText() {
        // Setup
        final UserPolicyUpdateRequest updateRequest = new UserPolicyUpdateRequest();
        updateRequest.setId(policyId);
        updateRequest.setPolicy("policy_update");
        updateRequest.setEnabled(false);

        final UserPolicy expectedResult = new UserPolicy();
        expectedResult.setId(policyId);
        expectedResult.setPolicy("policy_update");
        expectedResult.setCsp(Csp.HUAWEI);
        expectedResult.setEnabled(false);

        // Configure DatabaseUserPolicyStorage.findPolicyById(...).
        final UserPolicyEntity userPolicyEntity = new UserPolicyEntity();
        userPolicyEntity.setId(policyId);
        userPolicyEntity.setUserId(userId);
        userPolicyEntity.setPolicy("policy");
        userPolicyEntity.setCsp(Csp.HUAWEI);
        userPolicyEntity.setEnabled(true);
        when(mockUserPolicyStorage.findPolicyById(policyId)).thenReturn(userPolicyEntity);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        // Configure DatabaseUserPolicyStorage.listPolicies(...).
        final UserPolicyQueryRequest queryModel = new UserPolicyQueryRequest();

        queryModel.setUserId(userId);
        queryModel.setCsp(Csp.HUAWEI);
        queryModel.setPolicy("policy_update");

        // Configure DatabaseUserPolicyStorage.store(...).
        final UserPolicyEntity updatedUserPolicyEntity = new UserPolicyEntity();
        updatedUserPolicyEntity.setId(policyId);
        updatedUserPolicyEntity.setUserId(userId);
        updatedUserPolicyEntity.setPolicy("policy_update");
        updatedUserPolicyEntity.setCsp(Csp.HUAWEI);
        updatedUserPolicyEntity.setEnabled(false);
        when(mockUserPolicyStorage.store(updatedUserPolicyEntity)).thenReturn(
                updatedUserPolicyEntity);
        // Run the test
        final UserPolicy result = userPolicyManagerUnderTest.updateUserPolicy(updateRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testUpdateUserPolicy_UpdateCsp() {
        // Setup
        final UserPolicyUpdateRequest updateRequest = new UserPolicyUpdateRequest();
        updateRequest.setId(policyId);
        updateRequest.setCsp(Csp.OPENSTACK);
        updateRequest.setEnabled(false);

        final UserPolicy expectedResult = new UserPolicy();
        expectedResult.setId(policyId);
        expectedResult.setPolicy("policy");
        expectedResult.setCsp(Csp.OPENSTACK);
        expectedResult.setEnabled(false);

        // Configure DatabaseUserPolicyStorage.findPolicyById(...).
        final UserPolicyEntity userPolicyEntity = new UserPolicyEntity();
        userPolicyEntity.setId(policyId);
        userPolicyEntity.setUserId(userId);
        userPolicyEntity.setPolicy("policy");
        userPolicyEntity.setCsp(Csp.HUAWEI);
        userPolicyEntity.setEnabled(true);
        when(mockUserPolicyStorage.findPolicyById(policyId)).thenReturn(userPolicyEntity);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        // Configure DatabaseUserPolicyStorage.listPolicies(...).
        final UserPolicyQueryRequest queryModel = new UserPolicyQueryRequest();

        queryModel.setUserId(userId);
        queryModel.setCsp(Csp.HUAWEI);
        queryModel.setPolicy("policy");

        // Configure DatabaseUserPolicyStorage.store(...).
        final UserPolicyEntity updatedUserPolicyEntity = new UserPolicyEntity();
        updatedUserPolicyEntity.setId(policyId);
        updatedUserPolicyEntity.setUserId(userId);
        updatedUserPolicyEntity.setPolicy("policy");
        updatedUserPolicyEntity.setCsp(Csp.OPENSTACK);
        updatedUserPolicyEntity.setEnabled(false);
        when(mockUserPolicyStorage.store(updatedUserPolicyEntity)).thenReturn(
                updatedUserPolicyEntity);

        // Run the test
        final UserPolicy result = userPolicyManagerUnderTest.updateUserPolicy(updateRequest);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testUpdateUserPolicy_ThrowsPolicyNotFoundException() {
        // Setup
        final UserPolicyUpdateRequest updateRequest = new UserPolicyUpdateRequest();
        updateRequest.setId(policyId);
        updateRequest.setCsp(Csp.HUAWEI);
        updateRequest.setPolicy("policy");
        // Configure DatabaseUserPolicyStorage.findPolicyById(...).
        when(mockUserPolicyStorage.findPolicyById(policyId)).thenReturn(null);
        // Run the test
        assertThatThrownBy(() -> userPolicyManagerUnderTest.updateUserPolicy(updateRequest))
                .isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    void testUpdateUserPolicy_ThrowsAccessDeniedException() {
        // Setup
        final UserPolicyUpdateRequest updateRequest = new UserPolicyUpdateRequest();
        updateRequest.setId(policyId);
        updateRequest.setCsp(Csp.HUAWEI);
        updateRequest.setPolicy("policy");

        // Configure DatabaseUserPolicyStorage.findPolicyById(...).
        final UserPolicyEntity userPolicyEntity = new UserPolicyEntity();
        userPolicyEntity.setId(policyId);
        userPolicyEntity.setUserId(userId);
        userPolicyEntity.setPolicy("policy");
        userPolicyEntity.setCsp(Csp.HUAWEI);
        userPolicyEntity.setEnabled(true);
        when(mockUserPolicyStorage.findPolicyById(policyId)).thenReturn(userPolicyEntity);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(
                Optional.of("userId2"));

        // Run the test
        assertThatThrownBy(() -> userPolicyManagerUnderTest.updateUserPolicy(updateRequest))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void testUpdateUserPolicy_ThrowsPolicyDuplicateException() {
        // Setup
        final UserPolicyUpdateRequest updateRequest = new UserPolicyUpdateRequest();
        UUID id1 = UUID.randomUUID();
        updateRequest.setId(id1);
        updateRequest.setCsp(Csp.OPENSTACK);
        updateRequest.setPolicy("policy2");
        updateRequest.setEnabled(false);

        // Configure DatabaseUserPolicyStorage.findPolicyById(...).
        final UserPolicyEntity userPolicyEntity = new UserPolicyEntity();
        userPolicyEntity.setId(id1);
        userPolicyEntity.setUserId(userId);
        userPolicyEntity.setPolicy("policy");
        userPolicyEntity.setCsp(Csp.HUAWEI);
        userPolicyEntity.setEnabled(true);

        final UserPolicyEntity userPolicyEntity2 = new UserPolicyEntity();
        userPolicyEntity2.setId(UUID.randomUUID());
        userPolicyEntity2.setUserId(userId);
        userPolicyEntity2.setPolicy("policy2");
        userPolicyEntity2.setCsp(Csp.OPENSTACK);
        userPolicyEntity2.setEnabled(true);

        when(mockUserPolicyStorage.findPolicyById(id1)).thenReturn(userPolicyEntity);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        final UserPolicyQueryRequest queryModel = new UserPolicyQueryRequest();

        queryModel.setUserId(userId);
        queryModel.setCsp(Csp.OPENSTACK);
        queryModel.setPolicy("policy2");

        when(mockUserPolicyStorage.listPolicies(queryModel)).thenReturn(List.of(userPolicyEntity2));

        // Run the test
        assertThatThrownBy(() -> userPolicyManagerUnderTest.updateUserPolicy(updateRequest))
                .isInstanceOf(PolicyDuplicateException.class);
    }

    @Test
    void testGetUserPolicyDetails() {
        // Setup
        final UserPolicy expectedResult = new UserPolicy();
        expectedResult.setId(policyId);
        expectedResult.setPolicy("policy");
        expectedResult.setCsp(Csp.HUAWEI);
        expectedResult.setEnabled(true);

        // Configure DatabaseUserPolicyStorage.findPolicyById(...).
        final UserPolicyEntity userPolicyEntity = new UserPolicyEntity();
        userPolicyEntity.setId(policyId);
        userPolicyEntity.setUserId(userId);
        userPolicyEntity.setPolicy("policy");
        userPolicyEntity.setCsp(Csp.HUAWEI);
        userPolicyEntity.setEnabled(true);
        when(mockUserPolicyStorage.findPolicyById(policyId)).thenReturn(userPolicyEntity);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        // Run the test
        final UserPolicy result = userPolicyManagerUnderTest.getUserPolicyDetails(policyId);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetUserPolicyDetails_ThrowsPolicyNotFoundException() {
        // Setup
        final UserPolicy expectedResult = new UserPolicy();
        expectedResult.setId(policyId);
        expectedResult.setPolicy("policy");
        expectedResult.setCsp(Csp.HUAWEI);
        expectedResult.setEnabled(true);

        // Configure DatabaseUserPolicyStorage.findPolicyById(...).
        when(mockUserPolicyStorage.findPolicyById(policyId)).thenReturn(null);

        // Run the test
        assertThatThrownBy(() -> userPolicyManagerUnderTest.getUserPolicyDetails(policyId))
                .isInstanceOf(PolicyNotFoundException.class);
    }

    @Test
    void testGetUserPolicyDetails_ThrowsAccessDeniedException() {
        // Setup
        final UserPolicy expectedResult = new UserPolicy();
        expectedResult.setId(policyId);
        expectedResult.setPolicy("policy");
        expectedResult.setCsp(Csp.HUAWEI);
        expectedResult.setEnabled(true);

        // Configure DatabaseUserPolicyStorage.findPolicyById(...).
        final UserPolicyEntity userPolicyEntity = new UserPolicyEntity();
        userPolicyEntity.setId(policyId);
        userPolicyEntity.setUserId(userId);
        userPolicyEntity.setPolicy("policy");
        userPolicyEntity.setCsp(Csp.HUAWEI);
        userPolicyEntity.setEnabled(true);
        when(mockUserPolicyStorage.findPolicyById(policyId)).thenReturn(userPolicyEntity);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(
                Optional.of("userId2"));

        // Run the test
        assertThatThrownBy(() -> userPolicyManagerUnderTest.getUserPolicyDetails(policyId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void testDeleteUserPolicy() {
        // Setup
        // Configure DatabaseUserPolicyStorage.findPolicyById(...).
        final UserPolicyEntity userPolicyEntity = new UserPolicyEntity();
        userPolicyEntity.setId(policyId);
        userPolicyEntity.setUserId(userId);
        userPolicyEntity.setPolicy("policy");
        userPolicyEntity.setCsp(Csp.HUAWEI);
        userPolicyEntity.setEnabled(true);
        when(mockUserPolicyStorage.findPolicyById(policyId)).thenReturn(userPolicyEntity);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(Optional.of(userId));

        // Run the test
        userPolicyManagerUnderTest.deleteUserPolicy(policyId);

        // Verify the results
        verify(mockUserPolicyStorage).deletePolicyById(policyId);
    }

    @Test
    void testDeleteUserPolicy_ThrowsAccessDeniedException() {
        // Setup
        // Configure DatabaseUserPolicyStorage.findPolicyById(...).
        final UserPolicyEntity userPolicyEntity = new UserPolicyEntity();
        userPolicyEntity.setId(policyId);
        userPolicyEntity.setUserId(userId);
        userPolicyEntity.setPolicy("policy");
        userPolicyEntity.setCsp(Csp.HUAWEI);
        userPolicyEntity.setEnabled(true);
        when(mockUserPolicyStorage.findPolicyById(policyId)).thenReturn(userPolicyEntity);

        when(mockIdentityProviderManager.getCurrentLoginUserId()).thenReturn(
                Optional.of("userId2"));

        // Run the test
        assertThatThrownBy(() -> userPolicyManagerUnderTest.deleteUserPolicy(policyId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void testDeleteUserPolicy_ThrowsPolicyNotFoundException() {
        // Setup
        // Configure DatabaseUserPolicyStorage.findPolicyById(...).
        when(mockUserPolicyStorage.findPolicyById(policyId)).thenReturn(null);

        // Run the test
        assertThatThrownBy(() -> userPolicyManagerUnderTest.deleteUserPolicy(policyId))
                .isInstanceOf(PolicyNotFoundException.class);
    }
}
