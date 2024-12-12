/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TerraformScriptFormatInvalidExceptionTest {

    @Mock private List<String> mockErrorReasons;

    private TerraformScriptFormatInvalidException terraformScriptFormatInvalidExceptionUnderTest;

    @BeforeEach
    void setUp() {
        terraformScriptFormatInvalidExceptionUnderTest =
                new TerraformScriptFormatInvalidException(mockErrorReasons);
    }

    @Test
    void testGetErrorReasons() {
        assertThat(terraformScriptFormatInvalidExceptionUnderTest.getErrorReasons())
                .isEqualTo(mockErrorReasons);
    }

    @Test
    void testToString() {
        String result = "TerraformScriptFormatInvalidException(errorReasons=mockErrorReasons)";
        assertThat(terraformScriptFormatInvalidExceptionUnderTest.toString()).isEqualTo(result);
    }

    @Test
    void testEquals() {
        assertThat(terraformScriptFormatInvalidExceptionUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(terraformScriptFormatInvalidExceptionUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        TerraformScriptFormatInvalidException test =
                new TerraformScriptFormatInvalidException(new ArrayList<>());
        assertThat(terraformScriptFormatInvalidExceptionUnderTest.hashCode())
                .isNotEqualTo(test.hashCode());
    }
}
