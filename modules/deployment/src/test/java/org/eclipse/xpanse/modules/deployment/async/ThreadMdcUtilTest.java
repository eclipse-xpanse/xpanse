/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/**
 * Test of ThreadMdcUtil.
 */
class ThreadMdcUtilTest {

    @Test
    void testConcurrent() {
        ThreadMdcUtil threadMdcUtil = new ThreadMdcUtil();
        assertNotNull(threadMdcUtil);
    }

    @Test
    public void testWrapCallableWithNullContext() throws Exception {
        Callable<String> callable = () -> {
            return MDC.get("key");
        };
        Callable<String> wrappedCallable = ThreadMdcUtil.wrap(callable, null);
        MDC.put("key", "value");
        String result = wrappedCallable.call();

        assertNull(result);
    }

    @Test
    public void testWrapCallableWithNonNullContext() throws Exception {
        Callable<String> callable = () -> {
            return MDC.get("key");
        };
        Map<String, String> context = new HashMap<>();
        context.put("key", "value");
        Callable<String> wrappedCallable = ThreadMdcUtil.wrap(callable, context);
        String result = wrappedCallable.call();

        assertEquals("value", result);
    }

    @Test
    public void testWrapRunnableWithNullContext() {
        Runnable runnable = () -> {
            String value = MDC.get("key");
            MDC.put("key", "modified");
        };
        Runnable wrappedRunnable = ThreadMdcUtil.wrap(runnable, null);
        MDC.put("key", "value");
        wrappedRunnable.run();

        assertNull(MDC.get("key"));
    }

    @Test
    public void testWrapRunnableWithNonNullContext() {
        Runnable runnable = () -> {
            String value = MDC.get("key");
            MDC.put("key", "modified");
        };
        Map<String, String> context = new HashMap<>();
        context.put("key", "value");
        Runnable wrappedRunnable = ThreadMdcUtil.wrap(runnable, context);
        InheritableThreadLocal<Map<String, String>> mdcContext = new InheritableThreadLocal<>();
        mdcContext.set(context);
        wrappedRunnable.run();

        assertEquals("value", mdcContext.get().get("key"));
    }

}
