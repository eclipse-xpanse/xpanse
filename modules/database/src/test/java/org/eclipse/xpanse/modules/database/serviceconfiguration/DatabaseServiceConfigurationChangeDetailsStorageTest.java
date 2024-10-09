/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.DatabaseServiceConfigurationChangeDetailsStorage;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceConfigurationChangeDetailsRepository;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceConfigurationChangeDetailsEntity;
import org.eclipse.xpanse.modules.models.serviceconfiguration.enums.ServiceConfigurationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for DatabaseServiceConfigurationUpdateStorage.
 */
@ExtendWith(MockitoExtension.class)
public class DatabaseServiceConfigurationChangeDetailsStorageTest {

    @Mock
    private ServiceConfigurationChangeDetailsRepository serviceConfigurationChangeDetailsRepository;

    @InjectMocks
    private DatabaseServiceConfigurationChangeDetailsStorage databaseServiceConfigurationChangeDetailsStorage;

    private ServiceConfigurationChangeDetailsEntity request;

    @BeforeEach
    public void setUp() {
        request = new ServiceConfigurationChangeDetailsEntity();
        request.setId(UUID.randomUUID());
        request.setResourceName("TestResource");
        request.setConfigManager("TestConfigManager");
        request.setResultMessage("TestResult");
        request.setProperties(Map.of("key", "value"));
        request.setStatus(ServiceConfigurationStatus.PENDING);
    }

    @Test
    public void testStoreAndFlush() {
        when(serviceConfigurationChangeDetailsRepository.saveAndFlush(any(ServiceConfigurationChangeDetailsEntity.class)))
                .thenReturn(request);
        ServiceConfigurationChangeDetailsEntity result = databaseServiceConfigurationChangeDetailsStorage.storeAndFlush(request);
        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals("TestResource", result.getResourceName());
        verify(serviceConfigurationChangeDetailsRepository, times(1)).saveAndFlush(request);
    }

    @Test
    public void testSaveAll_withNonEmptyList() {
        ServiceConfigurationChangeDetailsEntity entity1 = new ServiceConfigurationChangeDetailsEntity();
        entity1.setId(UUID.randomUUID());
        entity1.setResourceName("Resource1");
        ServiceConfigurationChangeDetailsEntity entity2 = new ServiceConfigurationChangeDetailsEntity();
        entity2.setId(UUID.randomUUID());
        entity2.setResourceName("Resource2");
        List<ServiceConfigurationChangeDetailsEntity> entities = Arrays.asList(entity1, entity2);
        when(serviceConfigurationChangeDetailsRepository.saveAll(entities)).thenReturn(entities);
        List<ServiceConfigurationChangeDetailsEntity> result = serviceConfigurationChangeDetailsRepository.saveAll(entities);
        assertEquals(2, result.size());
        assertEquals("Resource1", result.get(0).getResourceName());
        assertEquals("Resource2", result.get(1).getResourceName());
        verify(serviceConfigurationChangeDetailsRepository, times(1)).saveAll(entities);
    }


    @Test
    public void testSaveAll_withEmptyList() {
        databaseServiceConfigurationChangeDetailsStorage.saveAll(Collections.emptyList());
        verify(serviceConfigurationChangeDetailsRepository, never())
                .saveAndFlush(any(ServiceConfigurationChangeDetailsEntity.class));
    }
}
