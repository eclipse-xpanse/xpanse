/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabaseDeployConfigurationStorageTest {

    private static final UUID id = UUID.fromString("9803512b-16b7-4eef-8aba-5e2495aa6fd2");
    private static final OffsetDateTime createTime = OffsetDateTime.now();

    @Mock
    private ServiceConfigurationRepository serviceConfigurationRepository;

    @InjectMocks
    private DatabaseServiceConfigurationStorage databaseServiceConfigurationStorageTest;

    @Test
    void testStoreAndFlush() {
        final ServiceConfigurationEntity serviceConfigurationEntity = new ServiceConfigurationEntity();
        serviceConfigurationEntity.setConfiguration(getConfiguration());
        serviceConfigurationEntity.setCreatedTime(createTime);
        databaseServiceConfigurationStorageTest.storeAndFlush(serviceConfigurationEntity);

        final ServiceConfigurationEntity entity = new ServiceConfigurationEntity();
        entity.setConfiguration(getConfiguration());
        entity.setCreatedTime(createTime);
        verify(serviceConfigurationRepository).saveAndFlush(entity);
    }

    @Test
    void testFindServiceConfigurationById() {
        final ServiceConfigurationEntity expectedResult = new ServiceConfigurationEntity();
        expectedResult.setConfiguration(getConfiguration());
        expectedResult.setCreatedTime(createTime);
        final ServiceConfigurationEntity serviceConfigurationEntity1 = new ServiceConfigurationEntity();
        serviceConfigurationEntity1.setConfiguration(getConfiguration());
        serviceConfigurationEntity1.setCreatedTime(createTime);
        final Optional<ServiceConfigurationEntity> entity = Optional.of(serviceConfigurationEntity1);
        when(serviceConfigurationRepository.findById(
                id))
                .thenReturn(entity);
        final ServiceConfigurationEntity result =
                databaseServiceConfigurationStorageTest.findServiceConfigurationById(
                        id);
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testFindServiceConfigurationById_ServiceConfigurationRepositoryReturnsAbsent() {
        when(serviceConfigurationRepository.findById(
                id))
                .thenReturn(Optional.empty());
        final ServiceConfigurationEntity result =
                databaseServiceConfigurationStorageTest.findServiceConfigurationById(
                        id);
        assertThat(result).isNull();
    }

    @Test
    void testDeleteServiceConfiguration() {
        final ServiceConfigurationEntity expectedResult = new ServiceConfigurationEntity();
        doNothing().when(serviceConfigurationRepository).delete(expectedResult);
        databaseServiceConfigurationStorageTest.deleteServiceConfiguration(
                expectedResult);

        verify(serviceConfigurationRepository, times(1)).delete(expectedResult);
    }

    private Map<String, String> getConfiguration(){
        Map<String, String> configuration = new HashMap<>();
        configuration.put("key1","value1");
        configuration.put("key2","value2");
        configuration.put("key3","value3");
        return configuration;
    }
}