/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.models.service.enums.DeployResourceKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

/**
 * Test of DatabaseServiceResourceStorage.
 */
@ExtendWith(MockitoExtension.class)
class DatabaseServiceResourceStorageTest {

    @Mock
    private ServiceResourceRepository mockServiceResourceRepository;

    private DatabaseServiceResourceStorage databaseDeployResourceStorageUnderTest;

    @BeforeEach
    void setUp() {
        databaseDeployResourceStorageUnderTest =
                new DatabaseServiceResourceStorage(mockServiceResourceRepository);
    }

    @Test
    void testFindServiceResourceById() {
        final ServiceResourceEntity expectedResult = new ServiceResourceEntity();
        expectedResult.setId(UUID.fromString("f523205e-c10a-4060-a147-d94532ada0f2"));
        expectedResult.setResourceId("resourceId");
        expectedResult.setResourceName("name");
        expectedResult.setResourceKind(DeployResourceKind.VM);
        final ServiceDeploymentEntity deployService = new ServiceDeploymentEntity();
        expectedResult.setServiceDeploymentEntity(deployService);

        final ServiceResourceEntity serviceResourceEntity1 = new ServiceResourceEntity();
        serviceResourceEntity1.setId(UUID.fromString("f523205e-c10a-4060-a147-d94532ada0f2"));
        serviceResourceEntity1.setResourceId("resourceId");
        serviceResourceEntity1.setResourceName("name");
        serviceResourceEntity1.setResourceKind(DeployResourceKind.VM);
        final ServiceDeploymentEntity deployService1 = new ServiceDeploymentEntity();
        serviceResourceEntity1.setServiceDeploymentEntity(deployService1);
        final Optional<ServiceResourceEntity> deployResourceEntity =
                Optional.of(serviceResourceEntity1);
        when(mockServiceResourceRepository.findById(
                UUID.fromString("f23db79e-fe0c-425e-97c6-1ebb64958cc5")))
                .thenReturn(deployResourceEntity);
        final ServiceResourceEntity result =
                databaseDeployResourceStorageUnderTest.findServiceResourceById(
                        UUID.fromString("f23db79e-fe0c-425e-97c6-1ebb64958cc5"));
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testFindDeployResourceById_ServiceResourceRepositoryReturnsAbsent() {
        when(mockServiceResourceRepository.findById(
                UUID.fromString("f23db79e-fe0c-425e-97c6-1ebb64958cc5")))
                .thenReturn(Optional.empty());
        final ServiceResourceEntity result =
                databaseDeployResourceStorageUnderTest.findServiceResourceById(
                        UUID.fromString("f23db79e-fe0c-425e-97c6-1ebb64958cc5"));
        assertThat(result).isNull();
    }

    @Test
    void testFindServiceResourceByResourceId() {
        final ServiceResourceEntity expectedResult = new ServiceResourceEntity();
        expectedResult.setId(UUID.fromString("f523205e-c10a-4060-a147-d94532ada0f2"));
        expectedResult.setResourceId("resourceId");
        expectedResult.setResourceName("name");
        expectedResult.setResourceKind(DeployResourceKind.VM);
        final ServiceDeploymentEntity deployService = new ServiceDeploymentEntity();
        expectedResult.setServiceDeploymentEntity(deployService);
        final ServiceResourceEntity serviceResourceEntity = new ServiceResourceEntity();
        serviceResourceEntity.setId(UUID.fromString("f523205e-c10a-4060-a147-d94532ada0f2"));
        serviceResourceEntity.setResourceId("resourceId");
        serviceResourceEntity.setResourceName("name");
        serviceResourceEntity.setResourceKind(DeployResourceKind.VM);
        final ServiceDeploymentEntity deployService1 = new ServiceDeploymentEntity();
        serviceResourceEntity.setServiceDeploymentEntity(deployService1);
        final List<ServiceResourceEntity> deployResourceEntities = List.of(serviceResourceEntity);
        when(mockServiceResourceRepository.findAll(any(Specification.class)))
                .thenReturn(deployResourceEntities);
        final ServiceResourceEntity result =
                databaseDeployResourceStorageUnderTest.findServiceResourceByResourceId("resourceId");
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testFindDeployResourceByResourceId_ServiceResourceRepositoryReturnsNoItems() {
        when(mockServiceResourceRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList());
        final ServiceResourceEntity result =
                databaseDeployResourceStorageUnderTest.findServiceResourceByResourceId("resourceId");
        assertThat(result).isNull();
    }
}
