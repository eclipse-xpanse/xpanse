/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Date;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceState;
import org.eclipse.xpanse.modules.models.service.register.Ocl;
import org.junit.jupiter.api.Test;

/**
 * Test of RegisteredServiceVo.
 */
class RegisteredServiceVoTest {

    private static final UUID id = UUID.fromString("ed6248d4-2bcd-4e94-84b0-29e014c05137");
    private static final String name = "kafka";
    private static final String version = "1.0";
    private static final Csp csp = Csp.HUAWEI;
    private static final Ocl ocl = new Ocl();
    private static final ServiceState serviceState = ServiceState.REGISTERED;
    private static final Category category = Category.COMPUTE;
    private static final Date createTime = new Date();
    private static final Date lastModifiedTime = new Date();

    @Test
    public void testGetterAndSetter() {
        RegisteredServiceVo registeredServiceVo = new RegisteredServiceVo();
        registeredServiceVo.setId(id);
        registeredServiceVo.setName(name);
        registeredServiceVo.setVersion(version);
        registeredServiceVo.setCsp(csp);
        registeredServiceVo.setOcl(ocl);
        registeredServiceVo.setServiceState(serviceState);
        registeredServiceVo.setCategory(category);
        registeredServiceVo.setCreateTime(createTime);
        registeredServiceVo.setLastModifiedTime(lastModifiedTime);

        assertEquals(id, registeredServiceVo.getId());
        assertEquals(name, registeredServiceVo.getName());
        assertEquals(version, registeredServiceVo.getVersion());
        assertEquals(csp, registeredServiceVo.getCsp());
        assertEquals(ocl, registeredServiceVo.getOcl());
        assertEquals(serviceState, registeredServiceVo.getServiceState());
        assertEquals(category, registeredServiceVo.getCategory());
        assertEquals(createTime, registeredServiceVo.getCreateTime());
        assertEquals(lastModifiedTime, registeredServiceVo.getLastModifiedTime());
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
        registeredServiceVo3.setId(UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5"));
        registeredServiceVo3.setName("name");
        registeredServiceVo3.setVersion("v2.0");
        registeredServiceVo3.setCsp(Csp.FLEXIBLE_ENGINE);
        registeredServiceVo3.setOcl(ocl);
        registeredServiceVo3.setServiceState(ServiceState.UPDATED);

        assertEquals(registeredServiceVo1, registeredServiceVo1);
        assertEquals(registeredServiceVo1, registeredServiceVo2);
        assertNotEquals(registeredServiceVo1, registeredServiceVo3);

        assertEquals(registeredServiceVo1.hashCode(), registeredServiceVo1.hashCode());
        assertEquals(registeredServiceVo1.hashCode(), registeredServiceVo2.hashCode());
        assertNotEquals(registeredServiceVo1.hashCode(),
                registeredServiceVo3.hashCode());
    }

    @Test
    void testToString() {
        RegisteredServiceVo registeredServiceVo = new RegisteredServiceVo();
        registeredServiceVo.setId(id);
        registeredServiceVo.setName(name);
        registeredServiceVo.setVersion(version);
        registeredServiceVo.setCsp(csp);
        registeredServiceVo.setCategory(category);
        registeredServiceVo.setOcl(ocl);
        registeredServiceVo.setCreateTime(createTime);
        registeredServiceVo.setLastModifiedTime(lastModifiedTime);
        registeredServiceVo.setServiceState(serviceState);

        String expectedToString = "RegisteredServiceVo(id=" + id +
                ", name=" + name +
                ", version=" + version +
                ", csp=" + csp +
                ", category=" + category +
                ", ocl=" + ocl +
                ", createTime=" + createTime +
                ", lastModifiedTime=" + lastModifiedTime +
                ", serviceState=" + serviceState +
                ")";
        assertEquals(expectedToString, registeredServiceVo.toString());
    }

}