package org.eclipse.xpanse.modules.database.servicetemplate;

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
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class DatabaseServiceTemplateStorageTest {

    private final UUID id = UUID.fromString("eef27308-92d6-4c7a-866b-a58966b94f2d");
    @Mock private ServiceTemplateRepository mockServiceTemplateRepository;
    private DatabaseServiceTemplateStorage test;

    @BeforeEach
    void setUp() {
        test = new DatabaseServiceTemplateStorage(mockServiceTemplateRepository);
    }

    @Test
    void testStoreAndFlush() {
        // Setup
        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(id);
        serviceTemplateEntity.setName("name");
        serviceTemplateEntity.setVersion("version");
        serviceTemplateEntity.setCsp(Csp.HUAWEI_CLOUD);
        serviceTemplateEntity.setCategory(Category.AI);
        serviceTemplateEntity.setServiceHostingType(ServiceHostingType.SERVICE_VENDOR);

        // Run the test
        ServiceTemplateEntity result = test.storeAndFlush(serviceTemplateEntity);

        // Verify the results
        // Confirm ServiceTemplateRepository.save(...).
        final ServiceTemplateEntity entity = new ServiceTemplateEntity();
        entity.setId(id);
        entity.setName("name");
        entity.setVersion("version");
        entity.setCsp(Csp.HUAWEI_CLOUD);
        entity.setCategory(Category.AI);
        entity.setServiceHostingType(ServiceHostingType.SERVICE_VENDOR);
        verify(mockServiceTemplateRepository).saveAndFlush(entity);
    }

    @Test
    void testStoreAndFlush_ServiceTemplateRepositoryThrowsOptimisticLockingFailureException() {
        // Setup
        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(id);
        serviceTemplateEntity.setName("name");
        serviceTemplateEntity.setVersion("version");
        serviceTemplateEntity.setCsp(Csp.HUAWEI_CLOUD);
        serviceTemplateEntity.setCategory(Category.AI);

        // Configure ServiceTemplateRepository.save(...).
        final ServiceTemplateEntity entity = new ServiceTemplateEntity();
        entity.setId(id);
        entity.setName("name");
        entity.setVersion("version");
        entity.setCsp(Csp.HUAWEI_CLOUD);
        entity.setCategory(Category.AI);
        when(mockServiceTemplateRepository.saveAndFlush(entity))
                .thenThrow(OptimisticLockingFailureException.class);

        // Run the test
        assertThatThrownBy(() -> test.storeAndFlush(serviceTemplateEntity))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    void testQueryServiceTemplates() {
        // Setup
        final ServiceTemplateQueryModel serviceQuery =
                ServiceTemplateQueryModel.builder()
                        .category(Category.AI)
                        .csp(Csp.HUAWEI_CLOUD)
                        .checkServiceVendor(false)
                        .build();

        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(id);
        serviceTemplateEntity.setName("name");
        serviceTemplateEntity.setVersion("version");
        serviceTemplateEntity.setCsp(Csp.HUAWEI_CLOUD);
        serviceTemplateEntity.setCategory(Category.AI);
        final List<ServiceTemplateEntity> expectedResult = List.of(serviceTemplateEntity);

        // Configure ServiceTemplateRepository.findAll(...).
        final ServiceTemplateEntity serviceTemplateEntity1 = new ServiceTemplateEntity();
        serviceTemplateEntity1.setId(id);
        serviceTemplateEntity1.setName("name");
        serviceTemplateEntity1.setVersion("version");
        serviceTemplateEntity1.setCsp(Csp.HUAWEI_CLOUD);
        serviceTemplateEntity1.setCategory(Category.AI);
        final List<ServiceTemplateEntity> ServiceTemplateEntities = List.of(serviceTemplateEntity1);
        when(mockServiceTemplateRepository.findAll(any(Specification.class)))
                .thenReturn(ServiceTemplateEntities);

        // Run the test
        final List<ServiceTemplateEntity> result = test.listServiceTemplates(serviceQuery);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testQueryServiceTemplates_ServiceTemplateRepositoryReturnsNoItems() {
        // Setup
        final ServiceTemplateQueryModel serviceQuery =
                ServiceTemplateQueryModel.builder()
                        .category(Category.AI)
                        .csp(Csp.HUAWEI_CLOUD)
                        .serviceName("serviceName")
                        .serviceVersion("serviceVersion")
                        .serviceTemplateRegistrationState(ServiceTemplateRegistrationState.APPROVED)
                        .checkServiceVendor(false)
                        .build();
        when(mockServiceTemplateRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList());

        // Run the test
        final List<ServiceTemplateEntity> result = test.listServiceTemplates(serviceQuery);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetServiceTemplateById() {
        // Setup
        final ServiceTemplateEntity expectedResult = new ServiceTemplateEntity();
        expectedResult.setId(id);
        expectedResult.setName("name");
        expectedResult.setVersion("version");
        expectedResult.setCsp(Csp.HUAWEI_CLOUD);
        expectedResult.setCategory(Category.AI);

        // Configure ServiceTemplateRepository.findById(...).
        final ServiceTemplateEntity serviceTemplateEntity1 = new ServiceTemplateEntity();
        serviceTemplateEntity1.setId(id);
        serviceTemplateEntity1.setName("name");
        serviceTemplateEntity1.setVersion("version");
        serviceTemplateEntity1.setCsp(Csp.HUAWEI_CLOUD);
        serviceTemplateEntity1.setCategory(Category.AI);
        final Optional<ServiceTemplateEntity> ServiceTemplateEntity =
                Optional.of(serviceTemplateEntity1);
        when(mockServiceTemplateRepository.findById(id)).thenReturn(ServiceTemplateEntity);

        // Run the test
        final ServiceTemplateEntity result = test.getServiceTemplateById(id);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testDeleteServiceTemplate() {
        // Setup
        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(id);
        serviceTemplateEntity.setName("name");
        serviceTemplateEntity.setVersion("version");
        serviceTemplateEntity.setCsp(Csp.HUAWEI_CLOUD);
        serviceTemplateEntity.setCategory(Category.AI);

        // Run the test
        test.deleteServiceTemplate(serviceTemplateEntity);

        // Verify the results
        // Confirm ServiceTemplateRepository.delete(...).
        final ServiceTemplateEntity entity = new ServiceTemplateEntity();
        entity.setId(id);
        entity.setName("name");
        entity.setVersion("version");
        entity.setCsp(Csp.HUAWEI_CLOUD);
        entity.setCategory(Category.AI);
        verify(mockServiceTemplateRepository).delete(entity);
    }

    @Test
    void testRemove_ServiceTemplateRepositoryThrowsOptimisticLockingFailureException() {
        // Setup
        final ServiceTemplateEntity serviceTemplateEntity = new ServiceTemplateEntity();
        serviceTemplateEntity.setId(id);
        serviceTemplateEntity.setName("name");
        serviceTemplateEntity.setVersion("version");
        serviceTemplateEntity.setCsp(Csp.HUAWEI_CLOUD);
        serviceTemplateEntity.setCategory(Category.AI);

        // Configure ServiceTemplateRepository.delete(...).
        final ServiceTemplateEntity entity = new ServiceTemplateEntity();
        entity.setId(id);
        entity.setName("name");
        entity.setVersion("version");
        entity.setCsp(Csp.HUAWEI_CLOUD);
        entity.setCategory(Category.AI);
        doThrow(OptimisticLockingFailureException.class)
                .when(mockServiceTemplateRepository)
                .delete(entity);

        // Run the test
        assertThatThrownBy(() -> test.deleteServiceTemplate(serviceTemplateEntity))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }
}
