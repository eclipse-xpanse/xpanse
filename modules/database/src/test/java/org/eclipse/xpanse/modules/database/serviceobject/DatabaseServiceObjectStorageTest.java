/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Test for DatabaseServiceObjectStorage. */
@ExtendWith(MockitoExtension.class)
class DatabaseServiceObjectStorageTest {

    @Mock private ServiceObjectRepository repository;

    @InjectMocks private DatabaseServiceObjectStorage storage;

    private final UUID objectId = UUID.randomUUID();
    private final ServiceObjectEntity testEntity = new ServiceObjectEntity();

    @BeforeEach
    void setUp() {
        testEntity.setObjectId(objectId);
    }

    @Test
    void storeAndFlushShouldCallRepositorySaveAndFlush() {
        when(repository.saveAndFlush(any(ServiceObjectEntity.class))).thenReturn(testEntity);
        ServiceObjectEntity result = storage.storeAndFlush(testEntity);
        verify(repository, times(1)).saveAndFlush(testEntity);
        assertEquals(testEntity, result);
    }

    @Test
    void deleteShouldCallRepositoryDelete() {
        storage.delete(testEntity);
        verify(repository, times(1)).delete(testEntity);
    }

    @Test
    void getEntityByIdShouldReturnEntityWhenExists() {
        when(repository.findById(objectId)).thenReturn(Optional.of(testEntity));
        ServiceObjectEntity result = storage.getEntityById(objectId);
        verify(repository, times(1)).findById(objectId);
        assertEquals(testEntity, result);
    }

    @Test
    void getEntityByIdShouldReturnNullWhenNotExists() {
        when(repository.findById(objectId)).thenReturn(Optional.empty());
        ServiceObjectEntity result = storage.getEntityById(objectId);
        verify(repository, times(1)).findById(objectId);
        assertNull(result);
    }
}
