/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.register.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test of TerraformScriptFormatInvalidException.
 */
class TerraformScriptFormatInvalidExceptionTest {

    @Test
    public void testConstructorAndGetters() {
        List<String> errorReasons = Arrays.asList("Reason 1", "Reason 2");
        List<String> successReasons = Arrays.asList("Reason 1", "Reason 3");
        TerraformScriptFormatInvalidException exception = new TerraformScriptFormatInvalidException(errorReasons);
        exception.setErrorReasons(successReasons);

        assertEquals(successReasons, exception.getErrorReasons());
    }

    @Test
    public void testEqualsAndHashCode() {
        List<String> errorReasons1 = Arrays.asList("Reason 1", "Reason 2");
        List<String> errorReasons2 = Arrays.asList("Reason 1", "Reason 2");
        List<String> errorReasons3 = Arrays.asList("Reason 1", "Reason 3");

        TerraformScriptFormatInvalidException exception1 = new TerraformScriptFormatInvalidException(errorReasons1);
        TerraformScriptFormatInvalidException exception2 = new TerraformScriptFormatInvalidException(errorReasons2);
        TerraformScriptFormatInvalidException exception3 = new TerraformScriptFormatInvalidException(errorReasons3);


        assertTrue(exception1.getErrorReasons().equals(exception2.getErrorReasons()));
        assertFalse(exception1.getErrorReasons().equals(exception3.getErrorReasons()));
        assertEquals(exception1.getErrorReasons().hashCode(), exception2.getErrorReasons().hashCode());
        assertNotEquals(exception1.hashCode(), exception3.hashCode());
    }
}
