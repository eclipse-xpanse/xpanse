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
class InvalidValueSchemaExceptionTest {

    @Mock
    private List<String> mockInvalidValueSchemaKeys;

    private InvalidValueSchemaException invalidValueSchemaExceptionUnderTest;

    @BeforeEach
    void setUp() {
        invalidValueSchemaExceptionUnderTest =
                new InvalidValueSchemaException(mockInvalidValueSchemaKeys);
    }

    @Test
    void testGetInvalidValueSchemaKeys() {
        assertThat(invalidValueSchemaExceptionUnderTest.getInvalidValueSchemaKeys())
                .isEqualTo(mockInvalidValueSchemaKeys);
    }

    @Test
    void testToString() {
        String result =
                "InvalidValueSchemaException(invalidValueSchemaKeys=mockInvalidValueSchemaKeys)";
        assertThat(invalidValueSchemaExceptionUnderTest.toString()).isEqualTo(result);
    }

    @Test
    void testEquals() {
        assertThat(invalidValueSchemaExceptionUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(invalidValueSchemaExceptionUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        InvalidValueSchemaException test = new InvalidValueSchemaException(new ArrayList<>());
        assertThat(invalidValueSchemaExceptionUnderTest.hashCode()).isNotEqualTo(test.hashCode());
    }
}
