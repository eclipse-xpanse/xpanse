package org.eclipse.xpanse.modules.database.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.policy.PolicyQueryRequest;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class DatabasePolicyStorageTest {

    @Mock
    private PolicyRepository mockPolicyRepository;

    private DatabasePolicyStorage databasePolicyStorageUnderTest;

    @BeforeEach
    void setUp() {
        databasePolicyStorageUnderTest = new DatabasePolicyStorage(mockPolicyRepository);
    }

    @Test
    void testStore() {
        // Setup
        final PolicyEntity policyEntity = new PolicyEntity();
        policyEntity.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        policyEntity.setUserId("userId");
        policyEntity.setPolicy("policy");
        policyEntity.setCsp(Csp.HUAWEI);
        policyEntity.setEnabled(false);

        final PolicyEntity expectedResult = new PolicyEntity();
        expectedResult.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        expectedResult.setUserId("userId");
        expectedResult.setPolicy("policy");
        expectedResult.setCsp(Csp.HUAWEI);
        expectedResult.setEnabled(false);

        // Configure PolicyRepository.save(...).
        final PolicyEntity policyEntity1 = new PolicyEntity();
        policyEntity1.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        policyEntity1.setUserId("userId");
        policyEntity1.setPolicy("policy");
        policyEntity1.setCsp(Csp.HUAWEI);
        policyEntity1.setEnabled(false);
        final PolicyEntity entity = new PolicyEntity();
        entity.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        entity.setUserId("userId");
        entity.setPolicy("policy");
        entity.setCsp(Csp.HUAWEI);
        entity.setEnabled(false);
        when(mockPolicyRepository.save(entity)).thenReturn(policyEntity1);

        // Run the test
        final PolicyEntity result = databasePolicyStorageUnderTest.store(policyEntity);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testPolicies() {
        // Setup
        final PolicyEntity policyEntity = new PolicyEntity();
        policyEntity.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        policyEntity.setUserId("userId");
        policyEntity.setPolicy("policy");
        policyEntity.setCsp(Csp.HUAWEI);
        policyEntity.setEnabled(false);
        final List<PolicyEntity> expectedResult = List.of(policyEntity);

        // Configure PolicyRepository.findAll(...).
        final PolicyEntity policyEntity1 = new PolicyEntity();
        policyEntity1.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        policyEntity1.setUserId("userId");
        policyEntity1.setPolicy("policy");
        policyEntity1.setCsp(Csp.HUAWEI);
        policyEntity1.setEnabled(false);
        final List<PolicyEntity> policyEntities = List.of(policyEntity1);
        when(mockPolicyRepository.findAll()).thenReturn(policyEntities);

        // Run the test
        final List<PolicyEntity> result = databasePolicyStorageUnderTest.policies();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testPolicies_PolicyRepositoryReturnsNoItems() {
        // Setup
        when(mockPolicyRepository.findAll()).thenReturn(Collections.emptyList());

        // Run the test
        final List<PolicyEntity> result = databasePolicyStorageUnderTest.policies();

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testListPolicies() {
        // Setup
        final PolicyQueryRequest queryModel = new PolicyQueryRequest();
        queryModel.setEnabled(false);
        queryModel.setUserId("userId");
        queryModel.setCsp(Csp.HUAWEI);
        queryModel.setPolicy("policy");

        final PolicyEntity policyEntity = new PolicyEntity();
        policyEntity.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        policyEntity.setUserId("userId");
        policyEntity.setPolicy("policy");
        policyEntity.setCsp(Csp.HUAWEI);
        policyEntity.setEnabled(false);
        final List<PolicyEntity> expectedResult = List.of(policyEntity);

        // Configure PolicyRepository.findAll(...).
        final PolicyEntity policyEntity1 = new PolicyEntity();
        policyEntity1.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        policyEntity1.setUserId("userId");
        policyEntity1.setPolicy("policy");
        policyEntity1.setCsp(Csp.HUAWEI);
        policyEntity1.setEnabled(false);
        final List<PolicyEntity> policyEntities = List.of(policyEntity1);
        when(mockPolicyRepository.findAll(any(Specification.class))).thenReturn(policyEntities);

        // Run the test
        final List<PolicyEntity> result = databasePolicyStorageUnderTest.listPolicies(queryModel);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testListPolicies_PolicyRepositoryReturnsNoItems() {
        // Setup
        final PolicyQueryRequest queryModel = new PolicyQueryRequest();
        queryModel.setEnabled(false);
        queryModel.setUserId("userId");
        queryModel.setCsp(Csp.HUAWEI);
        queryModel.setPolicy("policy");

        // Run the test
        final List<PolicyEntity> result = databasePolicyStorageUnderTest.listPolicies(queryModel);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testFindPolicyById() {
        // Setup
        final PolicyEntity expectedResult = new PolicyEntity();
        expectedResult.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        expectedResult.setUserId("userId");
        expectedResult.setPolicy("policy");
        expectedResult.setCsp(Csp.HUAWEI);
        expectedResult.setEnabled(false);

        // Configure PolicyRepository.findById(...).
        final PolicyEntity policyEntity = new PolicyEntity();
        policyEntity.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        policyEntity.setUserId("userId");
        policyEntity.setPolicy("policy");
        policyEntity.setCsp(Csp.HUAWEI);
        policyEntity.setEnabled(false);
        final Optional<PolicyEntity> policyEntityOptional = Optional.of(policyEntity);
        when(mockPolicyRepository.findById(
                UUID.fromString("3a680860-8484-428b-a39a-04cef6eaf983")))
                .thenReturn(policyEntityOptional);

        // Run the test
        final PolicyEntity result = databasePolicyStorageUnderTest.findPolicyById(
                UUID.fromString("3a680860-8484-428b-a39a-04cef6eaf983"));

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testFindPolicyById_PolicyRepositoryReturnsAbsent() {
        // Setup
        when(mockPolicyRepository.findById(
                UUID.fromString("3a680860-8484-428b-a39a-04cef6eaf983")))
                .thenReturn(Optional.empty());

        // Run the test
        final PolicyEntity result = databasePolicyStorageUnderTest.findPolicyById(
                UUID.fromString("3a680860-8484-428b-a39a-04cef6eaf983"));

        // Verify the results
        assertThat(result).isNull();
    }

    @Test
    void testDeletePolicies() {
        // Setup
        final PolicyEntity policyEntity = new PolicyEntity();
        policyEntity.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        policyEntity.setUserId("userId");
        policyEntity.setPolicy("policy");
        policyEntity.setCsp(Csp.HUAWEI);
        policyEntity.setEnabled(false);

        // Run the test
        databasePolicyStorageUnderTest.deletePolicies(policyEntity);

        // Verify the results
        // Confirm PolicyRepository.delete(...).
        final PolicyEntity entity = new PolicyEntity();
        entity.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        entity.setUserId("userId");
        entity.setPolicy("policy");
        entity.setCsp(Csp.HUAWEI);
        entity.setEnabled(false);
        verify(mockPolicyRepository).delete(entity);
    }

    @Test
    void testDeletePolicyById() {
        // Setup
        // Run the test
        databasePolicyStorageUnderTest.deletePolicyById(
                UUID.fromString("c74096c4-710b-4767-be2b-77ff462d2cb3"));

        // Verify the results
        verify(mockPolicyRepository).deleteById(
                UUID.fromString("c74096c4-710b-4767-be2b-77ff462d2cb3"));
    }
}
