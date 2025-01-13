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
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.DatabaseServiceChangeDetailsStorage;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceChangeDetailsEntity;
import org.eclipse.xpanse.modules.database.serviceconfiguration.update.ServiceChangeDetailsRepository;
import org.eclipse.xpanse.modules.models.serviceconfiguration.enums.ServiceChangeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Test for DatabaseServiceChangeDetailsStorage. */
@ExtendWith(MockitoExtension.class)
public class DatabaseServiceChangeDetailsStorageTest {

    @Mock private ServiceChangeDetailsRepository serviceChangeDetailsRepository;

    @InjectMocks private DatabaseServiceChangeDetailsStorage databaseServiceChangeDetailsStorage;

    private ServiceChangeDetailsEntity request;

    @BeforeEach
    public void setUp() {
        request = new ServiceChangeDetailsEntity();
        request.setId(UUID.randomUUID());
        request.setResourceName("TestResource");
        request.setChangeHandler("TestConfigManager");
        request.setResultMessage("TestResult");
        request.setProperties(Map.of("key", "value"));
        request.setStatus(ServiceChangeStatus.PENDING);
    }

    @Test
    public void testStoreAndFlush() {
        when(serviceChangeDetailsRepository.saveAndFlush(any(ServiceChangeDetailsEntity.class)))
                .thenReturn(request);
        ServiceChangeDetailsEntity result =
                databaseServiceChangeDetailsStorage.storeAndFlush(request);
        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals("TestResource", result.getResourceName());
        verify(serviceChangeDetailsRepository, times(1)).saveAndFlush(request);
    }

    @Test
    public void testSaveAll_withNonEmptyList() {
        ServiceChangeDetailsEntity entity1 = new ServiceChangeDetailsEntity();
        entity1.setId(UUID.randomUUID());
        entity1.setResourceName("Resource1");
        ServiceChangeDetailsEntity entity2 = new ServiceChangeDetailsEntity();
        entity2.setId(UUID.randomUUID());
        entity2.setResourceName("Resource2");
        List<ServiceChangeDetailsEntity> entities = Arrays.asList(entity1, entity2);
        when(serviceChangeDetailsRepository.saveAll(entities)).thenReturn(entities);
        List<ServiceChangeDetailsEntity> result = serviceChangeDetailsRepository.saveAll(entities);
        assertEquals(2, result.size());
        assertEquals("Resource1", result.get(0).getResourceName());
        assertEquals("Resource2", result.get(1).getResourceName());
        verify(serviceChangeDetailsRepository, times(1)).saveAll(entities);
    }

    @Test
    public void testSaveAll_withEmptyList() {
        databaseServiceChangeDetailsStorage.saveAll(Collections.emptyList());
        verify(serviceChangeDetailsRepository, never())
                .saveAndFlush(any(ServiceChangeDetailsEntity.class));
    }
}
