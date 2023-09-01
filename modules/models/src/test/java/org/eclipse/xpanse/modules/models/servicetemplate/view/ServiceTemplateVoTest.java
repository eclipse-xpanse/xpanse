/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.servicetemplate.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Date;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;
import org.junit.jupiter.api.Test;

/**
 * Test of RegisteredServiceVo.
 */
class ServiceTemplateVoTest {

    private static final UUID id = UUID.fromString("ed6248d4-2bcd-4e94-84b0-29e014c05137");
    private static final String name = "kafka";
    private static final String version = "1.0";
    private static final String namespace = "huawei";
    private static final Csp csp = Csp.HUAWEI;
    private static final Ocl ocl = new Ocl();
    private static final ServiceRegistrationState serviceState =
            ServiceRegistrationState.REGISTERED;
    private static final Category category = Category.COMPUTE;
    private static final Date createTime = new Date();
    private static final Date lastModifiedTime = new Date();

    @Test
    void testGetterAndSetter() {
        ServiceTemplateVo serviceTemplateVo = new ServiceTemplateVo();
        serviceTemplateVo.setId(id);
        serviceTemplateVo.setName(name);
        serviceTemplateVo.setVersion(version);
        serviceTemplateVo.setNamespace(namespace);
        serviceTemplateVo.setCsp(csp);
        serviceTemplateVo.setOcl(ocl);
        serviceTemplateVo.setServiceRegistrationState(serviceState);
        serviceTemplateVo.setCategory(category);
        serviceTemplateVo.setCreateTime(createTime);
        serviceTemplateVo.setLastModifiedTime(lastModifiedTime);

        assertEquals(id, serviceTemplateVo.getId());
        assertEquals(name, serviceTemplateVo.getName());
        assertEquals(version, serviceTemplateVo.getVersion());
        assertEquals(csp, serviceTemplateVo.getCsp());
        assertEquals(ocl, serviceTemplateVo.getOcl());
        assertEquals(namespace, serviceTemplateVo.getNamespace());
        assertEquals(serviceState, serviceTemplateVo.getServiceRegistrationState());
        assertEquals(category, serviceTemplateVo.getCategory());
        assertEquals(createTime, serviceTemplateVo.getCreateTime());
        assertEquals(lastModifiedTime, serviceTemplateVo.getLastModifiedTime());
    }

    @Test
    void testEqualsAndHashCode() {
        ServiceTemplateVo serviceTemplateVo1 = new ServiceTemplateVo();
        serviceTemplateVo1.setId(id);
        serviceTemplateVo1.setName(name);
        serviceTemplateVo1.setVersion(version);
        serviceTemplateVo1.setNamespace(namespace);
        serviceTemplateVo1.setCsp(csp);
        serviceTemplateVo1.setOcl(ocl);
        serviceTemplateVo1.setServiceRegistrationState(serviceState);


        ServiceTemplateVo serviceTemplateVo2 = new ServiceTemplateVo();
        serviceTemplateVo2.setId(id);
        serviceTemplateVo2.setName(name);
        serviceTemplateVo2.setVersion(version);
        serviceTemplateVo2.setNamespace(namespace);
        serviceTemplateVo2.setCsp(csp);
        serviceTemplateVo2.setOcl(ocl);
        serviceTemplateVo2.setServiceRegistrationState(serviceState);

        ServiceTemplateVo serviceTemplateVo3 = new ServiceTemplateVo();
        serviceTemplateVo3.setId(UUID.fromString("20424910-5f64-4984-84f0-6013c63c64f5"));
        serviceTemplateVo3.setName("name");
        serviceTemplateVo3.setVersion("v2.0");
        serviceTemplateVo3.setCsp(Csp.FLEXIBLE_ENGINE);
        serviceTemplateVo3.setOcl(ocl);
        serviceTemplateVo3.setServiceRegistrationState(ServiceRegistrationState.UPDATED);

        assertEquals(serviceTemplateVo1, serviceTemplateVo1);
        assertEquals(serviceTemplateVo1, serviceTemplateVo2);
        assertNotEquals(serviceTemplateVo1, serviceTemplateVo3);

        assertEquals(serviceTemplateVo1.hashCode(), serviceTemplateVo1.hashCode());
        assertEquals(serviceTemplateVo1.hashCode(), serviceTemplateVo2.hashCode());
        assertNotEquals(serviceTemplateVo1.hashCode(),
                serviceTemplateVo3.hashCode());
    }

    @Test
    void testToString() {
        ServiceTemplateVo serviceTemplateVo = new ServiceTemplateVo();
        serviceTemplateVo.setId(id);
        serviceTemplateVo.setName(name);
        serviceTemplateVo.setVersion(version);
        serviceTemplateVo.setCsp(csp);
        serviceTemplateVo.setCategory(category);
        serviceTemplateVo.setNamespace(namespace);
        serviceTemplateVo.setOcl(ocl);
        serviceTemplateVo.setCreateTime(createTime);
        serviceTemplateVo.setLastModifiedTime(lastModifiedTime);
        serviceTemplateVo.setServiceRegistrationState(serviceState);

        String expectedToString = "ServiceTemplateVo(id=" + id +
                ", name=" + name +
                ", version=" + version +
                ", csp=" + csp +
                ", category=" + category +
                ", namespace=" + namespace +
                ", ocl=" + ocl +
                ", createTime=" + createTime +
                ", lastModifiedTime=" + lastModifiedTime +
                ", serviceRegistrationState=" + serviceState +
                ")";
        assertEquals(expectedToString, serviceTemplateVo.toString());
    }

}