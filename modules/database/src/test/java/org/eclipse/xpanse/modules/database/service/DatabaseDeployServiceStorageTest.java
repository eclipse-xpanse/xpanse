package org.eclipse.xpanse.modules.database.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseDeployServiceStorageTest {

    private final UUID id = UUID.fromString("9803512b-16b7-4eef-8aba-5e2495aa6fd2");
    private final String userId = "defaultUserId";
    @Mock
    private DeployServiceRepository mockDeployServiceRepository;

    @InjectMocks
    private DatabaseDeployServiceStorage databaseDeployServiceStorageUnderTest;

    @Test
    void testStoreAndFlush() {
        final DeployServiceEntity deployServiceEntity = new DeployServiceEntity();
        deployServiceEntity.setId(id);
        deployServiceEntity.setUserId(userId);
        deployServiceEntity.setCategory(Category.AI);
        deployServiceEntity.setName("name");
        deployServiceEntity.setCustomerServiceName("customerServiceName");
        databaseDeployServiceStorageUnderTest.storeAndFlush(deployServiceEntity);
        final DeployServiceEntity entity = new DeployServiceEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setCategory(Category.AI);
        entity.setName("name");
        entity.setCustomerServiceName("customerServiceName");
        verify(mockDeployServiceRepository).saveAndFlush(entity);
    }

    @Test
    void testFindDeployServiceById() {
        final DeployServiceEntity expectedResult = new DeployServiceEntity();
        expectedResult.setId(id);
        expectedResult.setUserId(userId);
        expectedResult.setCategory(Category.AI);
        expectedResult.setName("name");
        expectedResult.setCustomerServiceName("customerServiceName");
        final DeployServiceEntity deployServiceEntity1 = new DeployServiceEntity();
        deployServiceEntity1.setId(id);
        deployServiceEntity1.setUserId(userId);
        deployServiceEntity1.setCategory(Category.AI);
        deployServiceEntity1.setName("name");
        deployServiceEntity1.setCustomerServiceName("customerServiceName");
        final Optional<DeployServiceEntity> deployServiceEntity = Optional.of(deployServiceEntity1);
        when(mockDeployServiceRepository.findById(
                id))
                .thenReturn(deployServiceEntity);
        final DeployServiceEntity result =
                databaseDeployServiceStorageUnderTest.findDeployServiceById(
                        id);
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testFindDeployServiceById_DeployServiceRepositoryReturnsAbsent() {
        when(mockDeployServiceRepository.findById(
                id))
                .thenReturn(Optional.empty());
        final DeployServiceEntity result =
                databaseDeployServiceStorageUnderTest.findDeployServiceById(
                        id);
        assertThat(result).isNull();
    }

    @Test
    void testDeleteDeployService() {
        final DeployServiceEntity expectedResult = new DeployServiceEntity();
        doNothing().when(mockDeployServiceRepository).delete(expectedResult);
        databaseDeployServiceStorageUnderTest.deleteDeployService(
                expectedResult);

        verify(mockDeployServiceRepository, times(1)).delete(expectedResult);
    }
}
