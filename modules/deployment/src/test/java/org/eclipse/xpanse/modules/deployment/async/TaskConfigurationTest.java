/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of TaskConfiguration.
 */
class TaskConfigurationTest {

    private static TaskConfiguration taskConfiguration;

    @BeforeEach
    void setUp() {
        taskConfiguration = new TaskConfiguration();
    }

    @Test
    public void testTaskExecutor() {
        Executor result = taskConfiguration.taskExecutor();
        ServiceThreadPoolTaskExecutor executor = (ServiceThreadPoolTaskExecutor) result;

        assertEquals(Runtime.getRuntime().availableProcessors() * 2, executor.getCorePoolSize());
        assertEquals(20, executor.getMaxPoolSize());
        assertEquals(200, executor.getQueueCapacity());
        assertEquals(300, executor.getKeepAliveSeconds());
        assertEquals("thread-pool-", executor.getThreadNamePrefix());
        assertNotNull(result);
        assertEquals(ServiceThreadPoolTaskExecutor.class, result.getClass());
    }

}
