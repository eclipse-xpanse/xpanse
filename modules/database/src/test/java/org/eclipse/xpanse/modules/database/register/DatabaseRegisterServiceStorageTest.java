package org.eclipse.xpanse.modules.database.register;

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
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.register.exceptions.ServiceNotRegisteredException;
import org.eclipse.xpanse.modules.models.service.register.query.RegisteredServiceQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class DatabaseRegisterServiceStorageTest {

    @Mock
    private RegisterServiceRepository mockRegisterServiceRepository;

    private final UUID id = UUID.fromString("eef27308-92d6-4c7a-866b-a58966b94f2d");
    private DatabaseRegisterServiceStorage test;

    @BeforeEach
    void setUp() {
        test = new DatabaseRegisterServiceStorage(mockRegisterServiceRepository);
    }

    @Test
    void testStore() {
        // Setup
        final RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setId(id);
        registerServiceEntity.setName("name");
        registerServiceEntity.setVersion("version");
        registerServiceEntity.setCsp(Csp.HUAWEI);
        registerServiceEntity.setCategory(Category.AI);

        // Run the test
        test.store(registerServiceEntity);

        // Verify the results
        // Confirm RegisterServiceRepository.save(...).
        final RegisterServiceEntity entity = new RegisterServiceEntity();
        entity.setId(id);
        entity.setName("name");
        entity.setVersion("version");
        entity.setCsp(Csp.HUAWEI);
        entity.setCategory(Category.AI);
        verify(mockRegisterServiceRepository).save(entity);
    }

    @Test
    void testStore_RegisterServiceRepositoryThrowsOptimisticLockingFailureException() {
        // Setup
        final RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setId(id);
        registerServiceEntity.setName("name");
        registerServiceEntity.setVersion("version");
        registerServiceEntity.setCsp(Csp.HUAWEI);
        registerServiceEntity.setCategory(Category.AI);

        // Configure RegisterServiceRepository.save(...).
        final RegisterServiceEntity entity = new RegisterServiceEntity();
        entity.setId(id);
        entity.setName("name");
        entity.setVersion("version");
        entity.setCsp(Csp.HUAWEI);
        entity.setCategory(Category.AI);
        when(mockRegisterServiceRepository.save(entity))
                .thenThrow(OptimisticLockingFailureException.class);

        // Run the test
        assertThatThrownBy(() -> test.store(
                registerServiceEntity)).isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    void testFindRegisteredService() {
        // Setup
        final RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setId(id);
        registerServiceEntity.setName("name");
        registerServiceEntity.setVersion("version");
        registerServiceEntity.setCsp(Csp.HUAWEI);
        registerServiceEntity.setCategory(Category.AI);

        final RegisterServiceEntity expectedResult = new RegisterServiceEntity();
        expectedResult.setId(id);
        expectedResult.setName("name");
        expectedResult.setVersion("version");
        expectedResult.setCsp(Csp.HUAWEI);
        expectedResult.setCategory(Category.AI);

        // Configure RegisterServiceRepository.findOne(...).
        final RegisterServiceEntity registerServiceEntity2 = new RegisterServiceEntity();
        registerServiceEntity2.setId(id);
        registerServiceEntity2.setName("name");
        registerServiceEntity2.setVersion("version");
        registerServiceEntity2.setCsp(Csp.HUAWEI);
        registerServiceEntity2.setCategory(Category.AI);
        final Optional<RegisterServiceEntity> registerServiceEntity1 =
                Optional.of(registerServiceEntity2);
        when(mockRegisterServiceRepository.findOne(any(Specification.class)))
                .thenReturn(registerServiceEntity1);

        // Run the test
        final RegisterServiceEntity result =
                test.findRegisteredService(
                        registerServiceEntity);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testFindRegisteredService_RegisterServiceRepositoryReturnsAbsent() {
        // Setup
        final RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setId(id);
        registerServiceEntity.setName("name");
        registerServiceEntity.setVersion("version");
        registerServiceEntity.setCsp(Csp.HUAWEI);
        registerServiceEntity.setCategory(Category.AI);

        when(mockRegisterServiceRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());

        // Run the test
        final RegisterServiceEntity result =
                test.findRegisteredService(
                        registerServiceEntity);

        // Verify the results
        assertThat(result).isNull();
    }

    @Test
    void testFindRegisteredService_RegisterServiceRepositoryThrowsIncorrectResultSizeDataAccessException() {
        // Setup
        final RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setId(id);
        registerServiceEntity.setName("name");
        registerServiceEntity.setVersion("version");
        registerServiceEntity.setCsp(Csp.HUAWEI);
        registerServiceEntity.setCategory(Category.AI);

        when(mockRegisterServiceRepository.findOne(any(Specification.class)))
                .thenThrow(IncorrectResultSizeDataAccessException.class);

        // Run the test
        assertThatThrownBy(() -> test.findRegisteredService(
                registerServiceEntity)).isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @Test
    void testQueryRegisteredServices() {
        // Setup
        final RegisteredServiceQuery serviceQuery = new RegisteredServiceQuery();
        serviceQuery.setCsp(Csp.HUAWEI);
        serviceQuery.setCategory(Category.AI);
        serviceQuery.setServiceName("serviceName");
        serviceQuery.setServiceVersion("serviceVersion");

        final RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setId(id);
        registerServiceEntity.setName("name");
        registerServiceEntity.setVersion("version");
        registerServiceEntity.setCsp(Csp.HUAWEI);
        registerServiceEntity.setCategory(Category.AI);
        final List<RegisterServiceEntity> expectedResult = List.of(registerServiceEntity);

        // Configure RegisterServiceRepository.findAll(...).
        final RegisterServiceEntity registerServiceEntity1 = new RegisterServiceEntity();
        registerServiceEntity1.setId(id);
        registerServiceEntity1.setName("name");
        registerServiceEntity1.setVersion("version");
        registerServiceEntity1.setCsp(Csp.HUAWEI);
        registerServiceEntity1.setCategory(Category.AI);
        final List<RegisterServiceEntity> registerServiceEntities = List.of(registerServiceEntity1);
        when(mockRegisterServiceRepository.findAll(any(Specification.class)))
                .thenReturn(registerServiceEntities);

        // Run the test
        final List<RegisterServiceEntity> result =
                test.queryRegisteredServices(serviceQuery);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testQueryRegisteredServices_RegisterServiceRepositoryReturnsNoItems() {
        // Setup
        final RegisteredServiceQuery serviceQuery = new RegisteredServiceQuery();
        serviceQuery.setCsp(Csp.HUAWEI);
        serviceQuery.setCategory(Category.AI);
        serviceQuery.setServiceName("serviceName");
        serviceQuery.setServiceVersion("serviceVersion");

        when(mockRegisterServiceRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList());

        // Run the test
        final List<RegisterServiceEntity> result =
                test.queryRegisteredServices(serviceQuery);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetRegisterServiceById() {
        // Setup
        final RegisterServiceEntity expectedResult = new RegisterServiceEntity();
        expectedResult.setId(id);
        expectedResult.setName("name");
        expectedResult.setVersion("version");
        expectedResult.setCsp(Csp.HUAWEI);
        expectedResult.setCategory(Category.AI);

        // Configure RegisterServiceRepository.findById(...).
        final RegisterServiceEntity registerServiceEntity1 = new RegisterServiceEntity();
        registerServiceEntity1.setId(id);
        registerServiceEntity1.setName("name");
        registerServiceEntity1.setVersion("version");
        registerServiceEntity1.setCsp(Csp.HUAWEI);
        registerServiceEntity1.setCategory(Category.AI);
        final Optional<RegisterServiceEntity> registerServiceEntity =
                Optional.of(registerServiceEntity1);
        when(mockRegisterServiceRepository.findById(id))
                .thenReturn(registerServiceEntity);

        // Run the test
        final RegisterServiceEntity result =
                test.getRegisterServiceById(id);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testServices() {
        // Setup
        final RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setId(id);
        registerServiceEntity.setName("name");
        registerServiceEntity.setVersion("version");
        registerServiceEntity.setCsp(Csp.HUAWEI);
        registerServiceEntity.setCategory(Category.AI);
        final List<RegisterServiceEntity> expectedResult = List.of(registerServiceEntity);

        // Configure RegisterServiceRepository.findAll(...).
        final RegisterServiceEntity registerServiceEntity1 = new RegisterServiceEntity();
        registerServiceEntity1.setId(id);
        registerServiceEntity1.setName("name");
        registerServiceEntity1.setVersion("version");
        registerServiceEntity1.setCsp(Csp.HUAWEI);
        registerServiceEntity1.setCategory(Category.AI);
        final List<RegisterServiceEntity> registerServiceEntities = List.of(registerServiceEntity1);
        when(mockRegisterServiceRepository.findAll()).thenReturn(registerServiceEntities);

        // Run the test
        final List<RegisterServiceEntity> result = test.services();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testServices_RegisterServiceRepositoryReturnsNoItems() {
        // Setup
        when(mockRegisterServiceRepository.findAll()).thenReturn(Collections.emptyList());

        // Run the test
        final List<RegisterServiceEntity> result = test.services();

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testRemoveById() {
        // Setup
        when(mockRegisterServiceRepository.existsById(id)).thenReturn(true);

        // Run the test
        test.removeById(id);

        // Verify the results
        verify(mockRegisterServiceRepository).deleteById(id);
    }

    @Test
    void testRemoveById_RegisterServiceRepositoryExistsByIdReturnsFalse() {
        // Setup
        when(mockRegisterServiceRepository.existsById(id)).thenReturn(false);

        // Run the test
        assertThatThrownBy(() -> test.removeById(id))
                .isInstanceOf(ServiceNotRegisteredException.class);
    }

    @Test
    void testRemove() {
        // Setup
        final RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setId(id);
        registerServiceEntity.setName("name");
        registerServiceEntity.setVersion("version");
        registerServiceEntity.setCsp(Csp.HUAWEI);
        registerServiceEntity.setCategory(Category.AI);

        // Run the test
        test.remove(registerServiceEntity);

        // Verify the results
        // Confirm RegisterServiceRepository.delete(...).
        final RegisterServiceEntity entity = new RegisterServiceEntity();
        entity.setId(id);
        entity.setName("name");
        entity.setVersion("version");
        entity.setCsp(Csp.HUAWEI);
        entity.setCategory(Category.AI);
        verify(mockRegisterServiceRepository).delete(entity);
    }

    @Test
    void testRemove_RegisterServiceRepositoryThrowsOptimisticLockingFailureException() {
        // Setup
        final RegisterServiceEntity registerServiceEntity = new RegisterServiceEntity();
        registerServiceEntity.setId(id);
        registerServiceEntity.setName("name");
        registerServiceEntity.setVersion("version");
        registerServiceEntity.setCsp(Csp.HUAWEI);
        registerServiceEntity.setCategory(Category.AI);

        // Configure RegisterServiceRepository.delete(...).
        final RegisterServiceEntity entity = new RegisterServiceEntity();
        entity.setId(id);
        entity.setName("name");
        entity.setVersion("version");
        entity.setCsp(Csp.HUAWEI);
        entity.setCategory(Category.AI);
        doThrow(OptimisticLockingFailureException.class).when(mockRegisterServiceRepository)
                .delete(entity);

        // Run the test
        assertThatThrownBy(() -> test.remove(
                registerServiceEntity)).isInstanceOf(OptimisticLockingFailureException.class);
    }
}
