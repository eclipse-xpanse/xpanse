/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.service.deploy.enums.DeployResourceKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

/**
 * Test of DatabaseDeployResourceStorage.
 */
@ExtendWith(MockitoExtension.class)
class DatabaseDeployResourceStorageTest {

    @Mock
    private DeployResourceRepository mockDeployResourceRepository;

    private DatabaseDeployResourceStorage databaseDeployResourceStorageUnderTest;

    @BeforeEach
    void setUp() {
        databaseDeployResourceStorageUnderTest =
                new DatabaseDeployResourceStorage(mockDeployResourceRepository);
    }

    @Test
    void testDeleteByDeployServiceId() {
        databaseDeployResourceStorageUnderTest.deleteByDeployServiceId(
                UUID.fromString("533b5d23-fb46-45fc-a27f-d49c44983074"));
        verify(mockDeployResourceRepository).deleteByDeployServiceId(
                UUID.fromString("533b5d23-fb46-45fc-a27f-d49c44983074"));
    }

    @Test
    void testFindDeployResourceById() {
        final DeployResourceEntity expectedResult = new DeployResourceEntity();
        expectedResult.setId(UUID.fromString("f523205e-c10a-4060-a147-d94532ada0f2"));
        expectedResult.setResourceId("resourceId");
        expectedResult.setName("name");
        expectedResult.setKind(DeployResourceKind.VM);
        final DeployServiceEntity deployService = new DeployServiceEntity();
        expectedResult.setDeployService(deployService);

        final DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("f523205e-c10a-4060-a147-d94532ada0f2"));
        deployResourceEntity1.setResourceId("resourceId");
        deployResourceEntity1.setName("name");
        deployResourceEntity1.setKind(DeployResourceKind.VM);
        final DeployServiceEntity deployService1 = new DeployServiceEntity();
        deployResourceEntity1.setDeployService(deployService1);
        final Optional<DeployResourceEntity> deployResourceEntity =
                Optional.of(deployResourceEntity1);
        when(mockDeployResourceRepository.findById(
                UUID.fromString("f23db79e-fe0c-425e-97c6-1ebb64958cc5")))
                .thenReturn(deployResourceEntity);
        final DeployResourceEntity result =
                databaseDeployResourceStorageUnderTest.findDeployResourceById(
                        UUID.fromString("f23db79e-fe0c-425e-97c6-1ebb64958cc5"));
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testFindDeployResourceById_DeployResourceRepositoryReturnsAbsent() {
        when(mockDeployResourceRepository.findById(
                UUID.fromString("f23db79e-fe0c-425e-97c6-1ebb64958cc5")))
                .thenReturn(Optional.empty());
        final DeployResourceEntity result =
                databaseDeployResourceStorageUnderTest.findDeployResourceById(
                        UUID.fromString("f23db79e-fe0c-425e-97c6-1ebb64958cc5"));
        assertThat(result).isNull();
    }

    @Test
    void testFindDeployResourceByResourceId() {
        final DeployResourceEntity expectedResult = new DeployResourceEntity();
        expectedResult.setId(UUID.fromString("f523205e-c10a-4060-a147-d94532ada0f2"));
        expectedResult.setResourceId("resourceId");
        expectedResult.setName("name");
        expectedResult.setKind(DeployResourceKind.VM);
        final DeployServiceEntity deployService = new DeployServiceEntity();
        expectedResult.setDeployService(deployService);
        final DeployResourceEntity deployResourceEntity = new DeployResourceEntity();
        deployResourceEntity.setId(UUID.fromString("f523205e-c10a-4060-a147-d94532ada0f2"));
        deployResourceEntity.setResourceId("resourceId");
        deployResourceEntity.setName("name");
        deployResourceEntity.setKind(DeployResourceKind.VM);
        final DeployServiceEntity deployService1 = new DeployServiceEntity();
        deployResourceEntity.setDeployService(deployService1);
        final List<DeployResourceEntity> deployResourceEntities = List.of(deployResourceEntity);
        when(mockDeployResourceRepository.findAll(any(Specification.class)))
                .thenReturn(deployResourceEntities);
        final DeployResourceEntity result =
                databaseDeployResourceStorageUnderTest.findDeployResourceByResourceId("resourceId");
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testFindDeployResourceByResourceId_DeployResourceRepositoryReturnsNoItems() {
        when(mockDeployResourceRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList());
        final DeployResourceEntity result =
                databaseDeployResourceStorageUnderTest.findDeployResourceByResourceId("resourceId");
        assertThat(result).isNull();
    }
}
