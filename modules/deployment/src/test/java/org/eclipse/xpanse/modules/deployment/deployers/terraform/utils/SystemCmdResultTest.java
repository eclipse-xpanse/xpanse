/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of SystemCmdResult.
 */
class SystemCmdResultTest {

    private static final String commandExecuted = "commandExecuted";
    private static final boolean isCommandSuccessful = true;
    private static final String commandStdOutput = "commandStdOutput";
    private static final String commandStdError = "commandStdError";
    private static SystemCmdResult systemCmdResult;

    @BeforeEach
    void setUp() {
        systemCmdResult = new SystemCmdResult();
        systemCmdResult.setCommandExecuted(commandExecuted);
        systemCmdResult.setCommandSuccessful(isCommandSuccessful);
        systemCmdResult.setCommandStdOutput(commandStdOutput);
        systemCmdResult.setCommandStdError(commandStdError);
    }

    @Test
    void testGetterAndSetter() {
        assertEquals(commandExecuted, systemCmdResult.getCommandExecuted());
        assertEquals(isCommandSuccessful, systemCmdResult.isCommandSuccessful());
        assertEquals(commandStdOutput, systemCmdResult.getCommandStdOutput());
        assertEquals(commandStdError, systemCmdResult.getCommandStdError());
    }

    @Test
    public void testEqualsAndHashCode() {
        assertEquals(systemCmdResult, systemCmdResult);
        assertEquals(systemCmdResult.hashCode(), systemCmdResult.hashCode());

        Object obj = new Object();
        assertNotEquals(systemCmdResult, obj);
        assertNotEquals(systemCmdResult, null);
        assertNotEquals(systemCmdResult.hashCode(), obj.hashCode());

        SystemCmdResult systemCmdResult1 = new SystemCmdResult();
        SystemCmdResult systemCmdResult2 = new SystemCmdResult();
        assertNotEquals(systemCmdResult, systemCmdResult1);
        assertNotEquals(systemCmdResult, systemCmdResult2);
        assertEquals(systemCmdResult1, systemCmdResult2);
        assertNotEquals(systemCmdResult.hashCode(), systemCmdResult1.hashCode());
        assertNotEquals(systemCmdResult.hashCode(), systemCmdResult2.hashCode());
        assertEquals(systemCmdResult1.hashCode(), systemCmdResult2.hashCode());

        systemCmdResult1.setCommandExecuted(commandExecuted);
        assertNotEquals(systemCmdResult, systemCmdResult1);
        assertNotEquals(systemCmdResult1, systemCmdResult2);
        assertNotEquals(systemCmdResult.hashCode(), systemCmdResult1.hashCode());
        assertNotEquals(systemCmdResult1.hashCode(), systemCmdResult2.hashCode());

        systemCmdResult1.setCommandSuccessful(isCommandSuccessful);
        assertNotEquals(systemCmdResult, systemCmdResult1);
        assertNotEquals(systemCmdResult1, systemCmdResult2);
        assertNotEquals(systemCmdResult.hashCode(), systemCmdResult1.hashCode());
        assertNotEquals(systemCmdResult1.hashCode(), systemCmdResult2.hashCode());

        systemCmdResult1.setCommandStdOutput(commandStdOutput);
        assertNotEquals(systemCmdResult, systemCmdResult1);
        assertNotEquals(systemCmdResult1, systemCmdResult2);
        assertNotEquals(systemCmdResult.hashCode(), systemCmdResult1.hashCode());
        assertNotEquals(systemCmdResult1.hashCode(), systemCmdResult2.hashCode());

        systemCmdResult1.setCommandStdError(commandStdError);
        assertEquals(systemCmdResult, systemCmdResult1);
        assertNotEquals(systemCmdResult1, systemCmdResult2);
        assertEquals(systemCmdResult.hashCode(), systemCmdResult1.hashCode());
        assertNotEquals(systemCmdResult1.hashCode(), systemCmdResult2.hashCode());
    }

    @Test
    void testToString() {
        String expectedToString = "SystemCmdResult(" +
                "commandExecuted=" + commandExecuted + ", " +
                "isCommandSuccessful=" + isCommandSuccessful + ", " +
                "commandStdOutput=" + commandStdOutput + ", " +
                "commandStdError=" + commandStdError + ")";
        assertEquals(expectedToString, systemCmdResult.toString());
    }

}
