package org.eclipse.xpanse.modules.database.servicepolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.servicetemplate.ServiceTemplateEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseServicePolicyStorageTest {

    @Mock private ServicePolicyRepository mockServicePolicyRepository;

    private DatabaseServicePolicyStorage databaseServicePolicyStorageUnderTest;

    @BeforeEach
    void setUp() {
        databaseServicePolicyStorageUnderTest =
                new DatabaseServicePolicyStorage(mockServicePolicyRepository);
    }

    @Test
    void testStoreAndFlush() {
        // Setup
        final ServicePolicyEntity servicePolicyEntity = new ServicePolicyEntity();
        servicePolicyEntity.setId(UUID.fromString("f80019fd-b557-4f55-ac1c-3a6e7683b27c"));
        final ServiceTemplateEntity serviceTemplate = new ServiceTemplateEntity();
        serviceTemplate.setId(UUID.fromString("90123bab-d2fe-4e37-badd-4468afdac8ee"));
        serviceTemplate.setName("name");
        serviceTemplate.setVersion("version");
        servicePolicyEntity.setServiceTemplate(serviceTemplate);

        final ServicePolicyEntity expectedResult = new ServicePolicyEntity();
        expectedResult.setId(UUID.fromString("f80019fd-b557-4f55-ac1c-3a6e7683b27c"));
        final ServiceTemplateEntity serviceTemplate1 = new ServiceTemplateEntity();
        serviceTemplate1.setId(UUID.fromString("90123bab-d2fe-4e37-badd-4468afdac8ee"));
        serviceTemplate1.setName("name");
        serviceTemplate1.setVersion("version");
        expectedResult.setServiceTemplate(serviceTemplate1);

        // Configure ServicePolicyRepository.saveAndFlush(...).
        final ServicePolicyEntity servicePolicyEntity1 = new ServicePolicyEntity();
        servicePolicyEntity1.setId(UUID.fromString("f80019fd-b557-4f55-ac1c-3a6e7683b27c"));
        final ServiceTemplateEntity serviceTemplate2 = new ServiceTemplateEntity();
        serviceTemplate2.setId(UUID.fromString("90123bab-d2fe-4e37-badd-4468afdac8ee"));
        serviceTemplate2.setName("name");
        serviceTemplate2.setVersion("version");
        servicePolicyEntity1.setServiceTemplate(serviceTemplate2);
        final ServicePolicyEntity entity = new ServicePolicyEntity();
        entity.setId(UUID.fromString("f80019fd-b557-4f55-ac1c-3a6e7683b27c"));
        final ServiceTemplateEntity serviceTemplate3 = new ServiceTemplateEntity();
        serviceTemplate3.setId(UUID.fromString("90123bab-d2fe-4e37-badd-4468afdac8ee"));
        serviceTemplate3.setName("name");
        serviceTemplate3.setVersion("version");
        entity.setServiceTemplate(serviceTemplate3);
        when(mockServicePolicyRepository.saveAndFlush(entity)).thenReturn(servicePolicyEntity1);

        // Run the test
        final ServicePolicyEntity result =
                databaseServicePolicyStorageUnderTest.storeAndFlush(servicePolicyEntity);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testFindPolicyById() {
        // Setup
        final ServicePolicyEntity expectedResult = new ServicePolicyEntity();
        expectedResult.setId(UUID.fromString("f80019fd-b557-4f55-ac1c-3a6e7683b27c"));
        final ServiceTemplateEntity serviceTemplate = new ServiceTemplateEntity();
        serviceTemplate.setId(UUID.fromString("90123bab-d2fe-4e37-badd-4468afdac8ee"));
        serviceTemplate.setName("name");
        serviceTemplate.setVersion("version");
        expectedResult.setServiceTemplate(serviceTemplate);

        // Configure ServicePolicyRepository.findById(...).
        final ServicePolicyEntity servicePolicyEntity1 = new ServicePolicyEntity();
        servicePolicyEntity1.setId(UUID.fromString("f80019fd-b557-4f55-ac1c-3a6e7683b27c"));
        final ServiceTemplateEntity serviceTemplate1 = new ServiceTemplateEntity();
        serviceTemplate1.setId(UUID.fromString("90123bab-d2fe-4e37-badd-4468afdac8ee"));
        serviceTemplate1.setName("name");
        serviceTemplate1.setVersion("version");
        servicePolicyEntity1.setServiceTemplate(serviceTemplate1);
        final Optional<ServicePolicyEntity> servicePolicyEntity = Optional.of(servicePolicyEntity1);
        when(mockServicePolicyRepository.findById(
                        UUID.fromString("ff871a4b-0a08-43f7-8335-98d0217a0f4e")))
                .thenReturn(servicePolicyEntity);

        // Run the test
        final ServicePolicyEntity result =
                databaseServicePolicyStorageUnderTest.findPolicyById(
                        UUID.fromString("ff871a4b-0a08-43f7-8335-98d0217a0f4e"));

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testFindPolicyById_ServicePolicyRepositoryReturnsAbsent() {
        // Setup
        when(mockServicePolicyRepository.findById(
                        UUID.fromString("ff871a4b-0a08-43f7-8335-98d0217a0f4e")))
                .thenReturn(Optional.empty());

        // Run the test
        final ServicePolicyEntity result =
                databaseServicePolicyStorageUnderTest.findPolicyById(
                        UUID.fromString("ff871a4b-0a08-43f7-8335-98d0217a0f4e"));

        // Verify the results
        assertThat(result).isNull();
    }

    @Test
    void testDeletePolicies() {
        // Setup
        final ServicePolicyEntity servicePolicyEntity = new ServicePolicyEntity();
        servicePolicyEntity.setId(UUID.fromString("f80019fd-b557-4f55-ac1c-3a6e7683b27c"));
        final ServiceTemplateEntity serviceTemplate = new ServiceTemplateEntity();
        serviceTemplate.setId(UUID.fromString("90123bab-d2fe-4e37-badd-4468afdac8ee"));
        serviceTemplate.setName("name");
        serviceTemplate.setVersion("version");
        servicePolicyEntity.setServiceTemplate(serviceTemplate);

        // Run the test
        databaseServicePolicyStorageUnderTest.deletePolicies(servicePolicyEntity);

        // Verify the results
        // Confirm ServicePolicyRepository.delete(...).
        final ServicePolicyEntity entity = new ServicePolicyEntity();
        entity.setId(UUID.fromString("f80019fd-b557-4f55-ac1c-3a6e7683b27c"));
        final ServiceTemplateEntity serviceTemplate1 = new ServiceTemplateEntity();
        serviceTemplate1.setId(UUID.fromString("90123bab-d2fe-4e37-badd-4468afdac8ee"));
        serviceTemplate1.setName("name");
        serviceTemplate1.setVersion("version");
        entity.setServiceTemplate(serviceTemplate1);
        verify(mockServicePolicyRepository).delete(entity);
    }

    @Test
    void testDeletePolicyById() {
        // Setup
        // Run the test
        databaseServicePolicyStorageUnderTest.deletePolicyById(
                UUID.fromString("ced71ed8-aaa1-428a-b8c8-53c3c77d5934"));

        // Verify the results
        verify(mockServicePolicyRepository)
                .deleteById(UUID.fromString("ced71ed8-aaa1-428a-b8c8-53c3c77d5934"));
    }
}
