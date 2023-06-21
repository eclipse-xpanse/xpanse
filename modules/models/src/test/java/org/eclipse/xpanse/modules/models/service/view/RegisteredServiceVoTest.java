/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceState;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.junit.jupiter.api.Test;

/**
 * Test of RegisteredServiceVo.
 */
class RegisteredServiceVoTest {

    private static final UUID id = UUID.randomUUID();
    private static final String name = "kafka";
    private static final String version = "1.0";
    private static final Csp csp = Csp.HUAWEI;
    private static final Ocl ocl = new Ocl();
    private static final ServiceState serviceState = ServiceState.REGISTERED;

    @Test
    public void testGetterAndSetter() {
        RegisteredServiceVo registeredServiceVo = new RegisteredServiceVo();
        registeredServiceVo.setId(id);
        registeredServiceVo.setName(name);
        registeredServiceVo.setVersion(version);
        registeredServiceVo.setCsp(csp);
        registeredServiceVo.setOcl(ocl);
        registeredServiceVo.setServiceState(serviceState);

        assertEquals(id, registeredServiceVo.getId());
        assertEquals(name, registeredServiceVo.getName());
        assertEquals(version, registeredServiceVo.getVersion());
        assertEquals(csp, registeredServiceVo.getCsp());
        assertEquals(ocl, registeredServiceVo.getOcl());
        assertEquals(serviceState, registeredServiceVo.getServiceState());
    }

    @Test
    public void testEqualsAndHashCode() {
        RegisteredServiceVo registeredServiceVo1 = new RegisteredServiceVo();
        registeredServiceVo1.setId(id);
        registeredServiceVo1.setName(name);
        registeredServiceVo1.setVersion(version);
        registeredServiceVo1.setCsp(csp);
        registeredServiceVo1.setOcl(ocl);
        registeredServiceVo1.setServiceState(serviceState);

        RegisteredServiceVo registeredServiceVo2 = new RegisteredServiceVo();
        registeredServiceVo2.setId(id);
        registeredServiceVo2.setName(name);
        registeredServiceVo2.setVersion(version);
        registeredServiceVo2.setCsp(csp);
        registeredServiceVo2.setOcl(ocl);
        registeredServiceVo2.setServiceState(serviceState);

        RegisteredServiceVo registeredServiceVo3 = new RegisteredServiceVo();
        registeredServiceVo3.setId(UUID.randomUUID());
        registeredServiceVo3.setName(name);
        registeredServiceVo3.setVersion(version);
        registeredServiceVo3.setCsp(csp);
        registeredServiceVo3.setOcl(ocl);
        registeredServiceVo3.setServiceState(serviceState);

        assertTrue(registeredServiceVo1.equals(registeredServiceVo2));
        assertEquals(registeredServiceVo1.hashCode(), registeredServiceVo2.hashCode());
        assertFalse(registeredServiceVo1.equals(registeredServiceVo3));
        assertNotEquals(registeredServiceVo1.hashCode(),
                registeredServiceVo3.hashCode());
    }
}