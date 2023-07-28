/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.common.enums;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test of Csp.
 */
class CspTest {

    @Test
    void testGetByValue() {
        Assertions.assertEquals(Csp.HUAWEI, Csp.getByValue("huawei"));
        Assertions.assertEquals(Csp.FLEXIBLE_ENGINE, Csp.getByValue("flexibleEngine"));
        Assertions.assertEquals(Csp.OPENSTACK, Csp.getByValue("openstack"));
        Assertions.assertEquals(Csp.ALICLOUD, Csp.getByValue("alicloud"));
        Assertions.assertEquals(Csp.AWS, Csp.getByValue("aws"));
        Assertions.assertEquals(Csp.AZURE, Csp.getByValue("azure"));
        Assertions.assertEquals(Csp.GOOGLE, Csp.getByValue("google"));
        Assertions.assertThrows(UnsupportedEnumValueException.class, () -> Csp.getByValue("null"));
    }

    @Test
    void testToValue() {
        Assertions.assertEquals("huawei", Csp.HUAWEI.toValue());
        Assertions.assertEquals("flexibleEngine", Csp.FLEXIBLE_ENGINE.toValue());
        Assertions.assertEquals("openstack", Csp.OPENSTACK.toValue());
        Assertions.assertEquals("alicloud", Csp.ALICLOUD.toValue());
        Assertions.assertEquals("aws", Csp.AWS.toValue());
        Assertions.assertEquals("azure", Csp.AZURE.toValue());
        Assertions.assertEquals("google", Csp.GOOGLE.toValue());
    }

}
