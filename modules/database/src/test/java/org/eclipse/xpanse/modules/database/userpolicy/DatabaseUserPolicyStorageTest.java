package org.eclipse.xpanse.modules.database.userpolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.policy.userpolicy.UserPolicyQueryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class DatabaseUserPolicyStorageTest {

    @Mock private UserPolicyRepository mockUserPolicyRepository;

    private DatabaseUserPolicyStorage databasePolicyStorageUnderTest;

    @BeforeEach
    void setUp() {
        databasePolicyStorageUnderTest = new DatabaseUserPolicyStorage(mockUserPolicyRepository);
    }

    @Test
    void testStore() {
        // Setup
        final UserPolicyEntity userPolicyEntity = new UserPolicyEntity();
        userPolicyEntity.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        userPolicyEntity.setUserId("userId");
        userPolicyEntity.setPolicy("policy");
        userPolicyEntity.setCsp(Csp.HUAWEI_CLOUD);
        userPolicyEntity.setEnabled(false);

        final UserPolicyEntity expectedResult = new UserPolicyEntity();
        expectedResult.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        expectedResult.setUserId("userId");
        expectedResult.setPolicy("policy");
        expectedResult.setCsp(Csp.HUAWEI_CLOUD);
        expectedResult.setEnabled(false);

        // Configure UserPolicyRepository.save(...).
        final UserPolicyEntity userPolicyEntity1 = new UserPolicyEntity();
        userPolicyEntity1.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        userPolicyEntity1.setUserId("userId");
        userPolicyEntity1.setPolicy("policy");
        userPolicyEntity1.setCsp(Csp.HUAWEI_CLOUD);
        userPolicyEntity1.setEnabled(false);
        final UserPolicyEntity entity = new UserPolicyEntity();
        entity.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        entity.setUserId("userId");
        entity.setPolicy("policy");
        entity.setCsp(Csp.HUAWEI_CLOUD);
        entity.setEnabled(false);
        when(mockUserPolicyRepository.saveAndFlush(entity)).thenReturn(userPolicyEntity1);

        // Run the test
        final UserPolicyEntity result =
                databasePolicyStorageUnderTest.storeAndFlush(userPolicyEntity);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testListPolicies() {
        // Setup
        final UserPolicyQueryRequest queryModel = new UserPolicyQueryRequest();
        queryModel.setEnabled(false);
        queryModel.setUserId("userId");
        queryModel.setCsp(Csp.HUAWEI_CLOUD);
        queryModel.setPolicy("policy");

        final UserPolicyEntity userPolicyEntity = new UserPolicyEntity();
        userPolicyEntity.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        userPolicyEntity.setUserId("userId");
        userPolicyEntity.setPolicy("policy");
        userPolicyEntity.setCsp(Csp.HUAWEI_CLOUD);
        userPolicyEntity.setEnabled(false);
        final List<UserPolicyEntity> expectedResult = List.of(userPolicyEntity);

        // Configure UserPolicyRepository.findAll(...).
        final UserPolicyEntity userPolicyEntity1 = new UserPolicyEntity();
        userPolicyEntity1.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        userPolicyEntity1.setUserId("userId");
        userPolicyEntity1.setPolicy("policy");
        userPolicyEntity1.setCsp(Csp.HUAWEI_CLOUD);
        userPolicyEntity1.setEnabled(false);
        final List<UserPolicyEntity> policyEntities = List.of(userPolicyEntity1);
        when(mockUserPolicyRepository.findAll(any(Specification.class))).thenReturn(policyEntities);

        // Run the test
        final List<UserPolicyEntity> result =
                databasePolicyStorageUnderTest.listPolicies(queryModel);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testListPolicies_PolicyRepositoryReturnsNoItems() {
        // Setup
        final UserPolicyQueryRequest queryModel = new UserPolicyQueryRequest();
        queryModel.setEnabled(false);
        queryModel.setUserId("userId");
        queryModel.setCsp(Csp.HUAWEI_CLOUD);
        queryModel.setPolicy("policy");

        // Run the test
        final List<UserPolicyEntity> result =
                databasePolicyStorageUnderTest.listPolicies(queryModel);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testFindPolicyById() {
        // Setup
        final UserPolicyEntity expectedResult = new UserPolicyEntity();
        expectedResult.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        expectedResult.setUserId("userId");
        expectedResult.setPolicy("policy");
        expectedResult.setCsp(Csp.HUAWEI_CLOUD);
        expectedResult.setEnabled(false);

        // Configure UserPolicyRepository.findById(...).
        final UserPolicyEntity userPolicyEntity = new UserPolicyEntity();
        userPolicyEntity.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        userPolicyEntity.setUserId("userId");
        userPolicyEntity.setPolicy("policy");
        userPolicyEntity.setCsp(Csp.HUAWEI_CLOUD);
        userPolicyEntity.setEnabled(false);
        final Optional<UserPolicyEntity> policyEntityOptional = Optional.of(userPolicyEntity);
        when(mockUserPolicyRepository.findById(
                        UUID.fromString("3a680860-8484-428b-a39a-04cef6eaf983")))
                .thenReturn(policyEntityOptional);

        // Run the test
        final UserPolicyEntity result =
                databasePolicyStorageUnderTest.findPolicyById(
                        UUID.fromString("3a680860-8484-428b-a39a-04cef6eaf983"));

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testFindPolicyById_PolicyRepositoryReturnsAbsent() {
        // Setup
        when(mockUserPolicyRepository.findById(
                        UUID.fromString("3a680860-8484-428b-a39a-04cef6eaf983")))
                .thenReturn(Optional.empty());

        // Run the test
        final UserPolicyEntity result =
                databasePolicyStorageUnderTest.findPolicyById(
                        UUID.fromString("3a680860-8484-428b-a39a-04cef6eaf983"));

        // Verify the results
        assertThat(result).isNull();
    }

    @Test
    void testDeletePolicies() {
        // Setup
        final UserPolicyEntity userPolicyEntity = new UserPolicyEntity();
        userPolicyEntity.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        userPolicyEntity.setUserId("userId");
        userPolicyEntity.setPolicy("policy");
        userPolicyEntity.setCsp(Csp.HUAWEI_CLOUD);
        userPolicyEntity.setEnabled(false);

        // Run the test
        databasePolicyStorageUnderTest.deletePolicies(userPolicyEntity);

        // Verify the results
        // Confirm UserPolicyRepository.delete(...).
        final UserPolicyEntity entity = new UserPolicyEntity();
        entity.setId(UUID.fromString("118a5df2-6984-42c8-800b-b95736d089af"));
        entity.setUserId("userId");
        entity.setPolicy("policy");
        entity.setCsp(Csp.HUAWEI_CLOUD);
        entity.setEnabled(false);
        verify(mockUserPolicyRepository).delete(entity);
    }

    @Test
    void testDeletePolicyById() {
        // Setup
        // Run the test
        databasePolicyStorageUnderTest.deletePolicyById(
                UUID.fromString("c74096c4-710b-4767-be2b-77ff462d2cb3"));

        // Verify the results
        verify(mockUserPolicyRepository)
                .deleteById(UUID.fromString("c74096c4-710b-4767-be2b-77ff462d2cb3"));
    }
}
