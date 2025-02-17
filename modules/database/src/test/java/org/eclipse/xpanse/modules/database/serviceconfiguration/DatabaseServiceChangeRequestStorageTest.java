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
import org.eclipse.xpanse.modules.database.servicechange.DatabaseServiceChangeRequestStorage;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeRequestEntity;
import org.eclipse.xpanse.modules.database.servicechange.ServiceChangeRequestRepository;
import org.eclipse.xpanse.modules.models.servicechange.enums.ServiceChangeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Test for DatabaseServiceChangeRequestStorage. */
@ExtendWith(MockitoExtension.class)
public class DatabaseServiceChangeRequestStorageTest {

    @Mock private ServiceChangeRequestRepository serviceChangeRequestRepository;

    @InjectMocks private DatabaseServiceChangeRequestStorage databaseServiceChangeRequestStorage;

    private ServiceChangeRequestEntity request;

    @BeforeEach
    public void setUp() {
        request = new ServiceChangeRequestEntity();
        request.setId(UUID.randomUUID());
        request.setResourceName("TestResource");
        request.setChangeHandler("TestConfigManager");
        request.setResultMessage("TestResult");
        request.setProperties(Map.of("key", "value"));
        request.setStatus(ServiceChangeStatus.PENDING);
    }

    @Test
    public void testStoreAndFlush() {
        when(serviceChangeRequestRepository.saveAndFlush(any(ServiceChangeRequestEntity.class)))
                .thenReturn(request);
        ServiceChangeRequestEntity result =
                databaseServiceChangeRequestStorage.storeAndFlush(request);
        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        assertEquals("TestResource", result.getResourceName());
        verify(serviceChangeRequestRepository, times(1)).saveAndFlush(request);
    }

    @Test
    public void testSaveAll_withNonEmptyList() {
        ServiceChangeRequestEntity entity1 = new ServiceChangeRequestEntity();
        entity1.setId(UUID.randomUUID());
        entity1.setResourceName("Resource1");
        ServiceChangeRequestEntity entity2 = new ServiceChangeRequestEntity();
        entity2.setId(UUID.randomUUID());
        entity2.setResourceName("Resource2");
        List<ServiceChangeRequestEntity> entities = Arrays.asList(entity1, entity2);
        when(serviceChangeRequestRepository.saveAll(entities)).thenReturn(entities);
        List<ServiceChangeRequestEntity> result = serviceChangeRequestRepository.saveAll(entities);
        assertEquals(2, result.size());
        assertEquals("Resource1", result.get(0).getResourceName());
        assertEquals("Resource2", result.get(1).getResourceName());
        verify(serviceChangeRequestRepository, times(1)).saveAll(entities);
    }

    @Test
    public void testSaveAll_withEmptyList() {
        databaseServiceChangeRequestStorage.saveAll(Collections.emptyList());
        verify(serviceChangeRequestRepository, never())
                .saveAndFlush(any(ServiceChangeRequestEntity.class));
    }
}
