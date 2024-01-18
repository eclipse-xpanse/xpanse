/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.async;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Customize the thread pool. Define ThreadPoolTaskExecutor named taskExecutor to replace @Async's
 * default thread pool.
 */
@Configuration
public class TaskConfiguration {

    public static final String ASYNC_EXECUTOR_NAME = "xpanseAsyncTaskExecutor";
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * Define ThreadPoolTaskExecutor named taskExecutor.
     *
     * @return executor
     */
    @Bean(ASYNC_EXECUTOR_NAME)
    public Executor taskExecutor() {
        ServiceThreadPoolTaskExecutor executor = new ServiceThreadPoolTaskExecutor();
        executor.setCorePoolSize(CPU_COUNT * 2);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("thread-pool-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(300);
        executor.initialize();
        return executor;
    }
}
