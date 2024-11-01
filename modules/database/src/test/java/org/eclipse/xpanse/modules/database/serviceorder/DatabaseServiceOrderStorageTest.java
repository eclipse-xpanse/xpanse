package org.eclipse.xpanse.modules.database.serviceorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentStorage;
import org.eclipse.xpanse.modules.models.service.enums.TaskStatus;
import org.eclipse.xpanse.modules.models.service.order.enums.ServiceOrderType;
import org.eclipse.xpanse.modules.models.service.order.exceptions.ServiceOrderNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class DatabaseServiceOrderStorageTest {

    private final UUID orderId = UUID.fromString("9803512b-16b7-4eef-8aba-5e2495aa6fd2");
    private final UUID serviceId = UUID.fromString("f80019fd-b557-4f55-ac1c-3a6e7683b27c");

    private final String userId = "userId";

    @Mock
    private ServiceOrderRepository mockRepository;

    @Mock
    private ServiceDeploymentStorage serviceDeploymentStorage;

    private DatabaseServiceOrderStorage test;

    @BeforeEach
    void setUp() {
        test = new DatabaseServiceOrderStorage(mockRepository);
    }

    ServiceOrderEntity getServiceOrderEntity() {
        ServiceOrderEntity serviceOrderEntity = new ServiceOrderEntity();
        serviceOrderEntity.setOrderId(orderId);
        serviceOrderEntity.setServiceDeploymentEntity(serviceDeploymentStorage.findServiceDeploymentById(serviceId));
        serviceOrderEntity.setTaskType(ServiceOrderType.DEPLOY);
        serviceOrderEntity.setUserId(userId);
        serviceOrderEntity.setTaskStatus(TaskStatus.CREATED);
        return serviceOrderEntity;
    }

    @Test
    void testStoreAndFlush() {
        // Setup
        final ServiceOrderEntity orderEntity = getServiceOrderEntity();

        final ServiceOrderEntity expectedResult = getServiceOrderEntity();

        // Configure ServiceOrderRepository.saveAndFlush(...).

        when(mockRepository.saveAndFlush(orderEntity)).thenReturn(expectedResult);

        // Run the test
        final ServiceOrderEntity result = test.storeAndFlush(expectedResult);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testQueryEntities() {
        // Setup
        final ServiceOrderEntity entity = getServiceOrderEntity();

        final List<ServiceOrderEntity> entities = List.of(entity);

        // Configure ServiceOrderRepository.findAll(...).
        final ServiceOrderEntity queryEntity = new ServiceOrderEntity();
        queryEntity.setServiceDeploymentEntity(serviceDeploymentStorage.findServiceDeploymentById(serviceId));
        queryEntity.setTaskType(ServiceOrderType.DEPLOY);
        queryEntity.setUserId(userId);
        queryEntity.setTaskStatus(TaskStatus.CREATED);
        when(mockRepository.findAll(any(Specification.class))).thenReturn(entities);

        // Run the test
        final List<ServiceOrderEntity> result = test.queryEntities(entity);

        // Verify the results
        assertThat(result).isEqualTo(entities);
    }

    @Test
    void testQueryEntities_ServiceOrderRepositoryReturnsNoItems() {
        // Setup
        final ServiceOrderEntity entity = new ServiceOrderEntity();
        entity.setOrderId(orderId);
        entity.setServiceDeploymentEntity(serviceDeploymentStorage.findServiceDeploymentById(serviceId));
        entity.setTaskType(ServiceOrderType.DESTROY);
        entity.setUserId(userId);
        entity.setTaskStatus(TaskStatus.CREATED);

        when(mockRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        // Run the test
        final List<ServiceOrderEntity> result = test.queryEntities(entity);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetEntityById() {
        // Setup
        final ServiceOrderEntity expectedResult = getServiceOrderEntity();

        // Configure ServiceOrderRepository.findById(...).
        final Optional<ServiceOrderEntity> serviceOrderEntity = Optional.of(expectedResult);
        when(mockRepository.findById(orderId)).thenReturn(serviceOrderEntity);

        // Run the test
        final ServiceOrderEntity result = test.getEntityById(orderId);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetEntityById_ServiceOrderRepositoryReturnsAbsent() {
        UUID uuid = UUID.randomUUID();
        // Setup
        when(mockRepository.findById(uuid)).thenReturn(Optional.empty());

        // Run the test
        assertThatThrownBy(() -> test.getEntityById(uuid))
                .isInstanceOf(ServiceOrderNotFound.class);
    }

    @Test
    void testDeleteBatch() {
        // Setup
        final ServiceOrderEntity serviceOrderEntity = getServiceOrderEntity();
        final List<ServiceOrderEntity> taskEntities = List.of(serviceOrderEntity);

        // Run the test
        test.deleteBatch(taskEntities);

        // Verify the results
        // Confirm ServiceOrderRepository.deleteAllInBatch(...).
        verify(mockRepository).deleteAllInBatch(taskEntities);
    }

    @Test
    void testDelete() {
        // Setup
        final ServiceOrderEntity taskEntity = getServiceOrderEntity();

        // Run the test
        test.delete(taskEntity);

        // Verify the results
        // Confirm ServiceOrderRepository.delete(...).
        verify(mockRepository).delete(taskEntity);
    }
}
