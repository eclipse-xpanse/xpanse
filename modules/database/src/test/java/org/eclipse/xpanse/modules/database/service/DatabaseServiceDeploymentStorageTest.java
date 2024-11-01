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
class DatabaseServiceDeploymentStorageTest {

    private final UUID id = UUID.fromString("9803512b-16b7-4eef-8aba-5e2495aa6fd2");
    private final String userId = "defaultUserId";
    @Mock
    private ServiceDeploymentRepository mockServiceDeploymentRepository;

    @InjectMocks
    private DatabaseServiceDeploymentStorage databaseServiceDeploymentStorageUnderTest;

    @Test
    void testStoreAndFlush() {
        final ServiceDeploymentEntity serviceDeploymentEntity = new ServiceDeploymentEntity();
        serviceDeploymentEntity.setId(id);
        serviceDeploymentEntity.setUserId(userId);
        serviceDeploymentEntity.setCategory(Category.AI);
        serviceDeploymentEntity.setName("name");
        serviceDeploymentEntity.setCustomerServiceName("customerServiceName");
        databaseServiceDeploymentStorageUnderTest.storeAndFlush(serviceDeploymentEntity);
        final ServiceDeploymentEntity entity = new ServiceDeploymentEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setCategory(Category.AI);
        entity.setName("name");
        entity.setCustomerServiceName("customerServiceName");
        verify(mockServiceDeploymentRepository).saveAndFlush(entity);
    }

    @Test
    void testFindServiceDeploymentById() {
        final ServiceDeploymentEntity expectedResult = new ServiceDeploymentEntity();
        expectedResult.setId(id);
        expectedResult.setUserId(userId);
        expectedResult.setCategory(Category.AI);
        expectedResult.setName("name");
        expectedResult.setCustomerServiceName("customerServiceName");
        final ServiceDeploymentEntity serviceDeploymentEntity1 = new ServiceDeploymentEntity();
        serviceDeploymentEntity1.setId(id);
        serviceDeploymentEntity1.setUserId(userId);
        serviceDeploymentEntity1.setCategory(Category.AI);
        serviceDeploymentEntity1.setName("name");
        serviceDeploymentEntity1.setCustomerServiceName("customerServiceName");
        final Optional<ServiceDeploymentEntity> deployServiceEntity = Optional.of(serviceDeploymentEntity1);
        when(mockServiceDeploymentRepository.findById(
                id))
                .thenReturn(deployServiceEntity);
        final ServiceDeploymentEntity result =
                databaseServiceDeploymentStorageUnderTest.findServiceDeploymentById(
                        id);
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testFindDeployServiceById_ServiceDeploymentRepositoryReturnsAbsent() {
        when(mockServiceDeploymentRepository.findById(
                id))
                .thenReturn(Optional.empty());
        final ServiceDeploymentEntity result =
                databaseServiceDeploymentStorageUnderTest.findServiceDeploymentById(
                        id);
        assertThat(result).isNull();
    }

    @Test
    void testDeleteServiceDeployment() {
        final ServiceDeploymentEntity expectedResult = new ServiceDeploymentEntity();
        doNothing().when(mockServiceDeploymentRepository).delete(expectedResult);
        databaseServiceDeploymentStorageUnderTest.deleteServiceDeployment(
                expectedResult);

        verify(mockServiceDeploymentRepository, times(1)).delete(expectedResult);
    }
}
