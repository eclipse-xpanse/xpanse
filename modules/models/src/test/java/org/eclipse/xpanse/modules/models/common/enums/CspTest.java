/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.common.enums;

import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Test of Csp. */
class CspTest {

    @Test
    void testGetByValue() {
        Assertions.assertEquals(Csp.HUAWEI_CLOUD, Csp.getByValue("HuaweiCloud"));
        Assertions.assertEquals(Csp.FLEXIBLE_ENGINE, Csp.getByValue("FlexibleEngine"));
        Assertions.assertEquals(Csp.OPENSTACK_TESTLAB, Csp.getByValue("OpenstackTestlab"));
        Assertions.assertEquals(Csp.PLUS_SERVER, Csp.getByValue("PlusServer"));
        Assertions.assertEquals(Csp.REGIO_CLOUD, Csp.getByValue("RegioCloud"));
        Assertions.assertEquals(Csp.ALIBABA_CLOUD, Csp.getByValue("AlibabaCloud"));
        Assertions.assertEquals(Csp.AWS, Csp.getByValue("aws"));
        Assertions.assertEquals(Csp.AZURE, Csp.getByValue("Azure"));
        Assertions.assertEquals(Csp.GCP, Csp.getByValue("GoogleCloudPlatform"));
        Assertions.assertThrows(UnsupportedEnumValueException.class, () -> Csp.getByValue("null"));
    }

    @Test
    void testToValue() {
        Assertions.assertEquals("HuaweiCloud", Csp.HUAWEI_CLOUD.toValue());
        Assertions.assertEquals("FlexibleEngine", Csp.FLEXIBLE_ENGINE.toValue());
        Assertions.assertEquals("OpenstackTestlab", Csp.OPENSTACK_TESTLAB.toValue());
        Assertions.assertEquals("AlibabaCloud", Csp.ALIBABA_CLOUD.toValue());
        Assertions.assertEquals("aws", Csp.AWS.toValue());
        Assertions.assertEquals("azure", Csp.AZURE.toValue());
        Assertions.assertEquals("GoogleCloudPlatform", Csp.GCP.toValue());
    }
}
