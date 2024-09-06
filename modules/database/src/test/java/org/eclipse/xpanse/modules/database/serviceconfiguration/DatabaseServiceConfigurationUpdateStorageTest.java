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
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.DatabaseServiceConfigurationUpdateStorage;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceConfigurationUpdateRepository;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceConfigurationUpdateRequest;
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
public class DatabaseServiceConfigurationUpdateStorageTest {

    @Mock
    private ServiceConfigurationUpdateRepository serviceConfigurationUpdateRepository;

    @InjectMocks
    private DatabaseServiceConfigurationUpdateStorage databaseServiceConfigurationUpdateStorage;

    private ServiceConfigurationUpdateRequest request;

    @BeforeEach
    public void setUp() {
        request = new ServiceConfigurationUpdateRequest();
        request.setId(UUID.randomUUID());
        request.setResourceName("TestResource");
        request.setConfigManager("TestConfigManager");
        request.setResultMessage("TestResult");
        request.setProperties(Map.of("key", "value"));
        request.setStatus(ServiceConfigurationStatus.PENDING);
    }

    @Test
    public void testStoreAndFlush() {
        when(serviceConfigurationUpdateRepository.saveAndFlush(any(ServiceConfigurationUpdateRequest.class)))
                .thenReturn(request);
        ServiceConfigurationUpdateRequest result = databaseServiceConfigurationUpdateStorage.storeAndFlush(request);
        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals("TestResource", result.getResourceName());
        verify(serviceConfigurationUpdateRepository, times(1)).saveAndFlush(request);
    }

    @Test
    public void testSaveAll_withNonEmptyList() {
        ServiceConfigurationUpdateRequest entity1 = new ServiceConfigurationUpdateRequest();
        entity1.setId(UUID.randomUUID());
        entity1.setResourceName("Resource1");
        ServiceConfigurationUpdateRequest entity2 = new ServiceConfigurationUpdateRequest();
        entity2.setId(UUID.randomUUID());
        entity2.setResourceName("Resource2");
        List<ServiceConfigurationUpdateRequest> entities = Arrays.asList(entity1, entity2);
        when(serviceConfigurationUpdateRepository.saveAll(entities)).thenReturn(entities);
        List<ServiceConfigurationUpdateRequest> result = serviceConfigurationUpdateRepository.saveAll(entities);
        assertEquals(2, result.size());
        assertEquals("Resource1", result.get(0).getResourceName());
        assertEquals("Resource2", result.get(1).getResourceName());
        verify(serviceConfigurationUpdateRepository, times(1)).saveAll(entities);
    }


    @Test
    public void testSaveAll_withEmptyList() {
        databaseServiceConfigurationUpdateStorage.saveAll(Collections.emptyList());
        verify(serviceConfigurationUpdateRepository, never())
                .saveAndFlush(any(ServiceConfigurationUpdateRequest.class));
    }
}
