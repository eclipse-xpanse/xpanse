/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of ServiceThreadPoolTaskExecutor.
 */
class ServiceThreadPoolTaskExecutorTest {

    private static ServiceThreadPoolTaskExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new ServiceThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.initialize();
    }

    @Test
    public void execute_shouldInvokeSuperExecuteWithWrappedTask() throws InterruptedException {
        Runnable task = () -> System.out.println("Executing task...");

        CountDownLatch latch = new CountDownLatch(1);

        executor.execute(() -> {
            task.run();
            latch.countDown();
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    @Test
    public void submitCallable_shouldInvokeSuperSubmitWithWrappedCallable()
            throws InterruptedException, ExecutionException {
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.initialize();

        Callable<String> task = () -> {
            System.out.println("Executing callable task...");
            return "Task Result";
        };

        CountDownLatch latch = new CountDownLatch(1);

        Future<String> future = executor.submit(() -> {
            String result = task.call();
            latch.countDown();
            return result;
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals("Task Result", future.get());
    }

    @Test
    public void submitRunnable_shouldInvokeSuperSubmitWithWrappedRunnable()
            throws InterruptedException, ExecutionException {
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.initialize();

        Runnable task = () -> System.out.println("Executing task...");

        CountDownLatch latch = new CountDownLatch(1);

        Future<?> future = executor.submit(() -> {
            task.run();
            latch.countDown();
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        future.get();
    }

}
