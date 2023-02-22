/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.async;

import java.util.Map;
import java.util.concurrent.Callable;
import org.slf4j.MDC;

/**
 * Bean to get mdc logging info.
 */
public final class ThreadMdcUtil {

    /**
     * When the parent thread submits a callable task to the thread pool, it copies the data in its
     * own MDC to the child thread.
     *
     * @param callable callable task
     * @param context  context
     * @param <T>      return object type
     * @return T
     */
    public static <T> Callable<T> wrap(final Callable<T> callable,
            final Map<String, String> context) {
        return new Callable<>() {
            @Override
            public T call() throws Exception {
                if (context == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(context);
                }
                try {
                    return callable.call();
                } finally {
                    MDC.clear();
                }
            }
        };
    }

    /**
     * When the parent thread submits a runnable task to the thread pool, it copies the data in its
     * own MDC to the child thread.
     *
     * @param runnable runnable task
     * @param context  context
     * @return thread task
     */
    public static Runnable wrap(final Runnable runnable, final Map<String, String> context) {
        return () -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}