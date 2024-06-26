/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.credential;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;

/**
 * Test of CreateCredential.
 */
class CreateCredentialTest {

    private final String name = "credential";
    private final String userId = "userId";
    private final Csp csp = Csp.HUAWEI_CLOUD;
    private final String description = "Test credential";
    private final CredentialType type = CredentialType.VARIABLES;
    private final Integer timeToLive = 36000;
    @Mock
    private List<CredentialVariable> variables;
    private CreateCredential test;

    @BeforeEach
    void setUp() {
        test = new CreateCredential();
        test.setName(name);
        test.setUserId(userId);
        test.setCsp(csp);
        test.setDescription(description);
        test.setType(type);
        test.setVariables(variables);
        test.setTimeToLive(timeToLive);
    }

    @Test
    public void testConstructorAndGetters() {
        assertEquals(name, test.getName());
        assertEquals(userId, test.getUserId());
        assertEquals(csp, test.getCsp());
        assertEquals(description, test.getDescription());
        assertEquals(type, test.getType());
        assertEquals(variables, test.getVariables());
        assertEquals(timeToLive, test.getTimeToLive());
    }

    @Test
    public void testEqualsAndHashCode() {
        Object o = new Object();
        assertFalse(test.canEqual(o));
        assertNotEquals(test, o);
        assertNotEquals(test.hashCode(), o.hashCode());

        CreateCredential test1 = new CreateCredential();
        assertTrue(test.canEqual(test1));
        assertNotEquals(test, test1);
        assertNotEquals(test.hashCode(), test1.hashCode());

        BeanUtils.copyProperties(test, test1);
        assertTrue(test.canEqual(test1));
        assertEquals(test, test1);
        assertEquals(test.hashCode(), test1.hashCode());

    }

    @Test
    void testToString() {
        String expectedToString = "CreateCredential(name=" + name + ", "
                + "userId=" + userId + ", "
                + "csp=" + csp + ", "
                + "description=" + description + ", "
                + "type=" + type + ", "
                + "variables=" + variables + ", "
                + "timeToLive=" + timeToLive + ")";
        assertEquals(expectedToString, test.toString());
    }

}
