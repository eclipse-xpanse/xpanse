package org.eclipse.xpanse.modules.orchestrator.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeployTaskTest {

    UUID id = UUID.fromString("23cc529b-64d9-4875-a2f0-08b415705964");
    Ocl ocl = new Ocl();
    DeployRequest deployRequest = new DeployRequest();
    private DeployTask test;

    @BeforeEach
    void setUp() {
        test = new DeployTask();
        ocl.setServiceProviderContactDetails(null);
        test.setId(id);
        test.setOcl(ocl);
        test.setDeployRequest(deployRequest);
    }

    @Test
    void testGetters() {
        assertEquals(id, test.getId());
        assertNotNull(test.getOcl());
        assertNotNull(test.getDeployRequest());
    }

    @Test
    void testEqualsAndHashCode() {

        assertNotEquals(test.hashCode(), 0);

        Object object = new Object();
        assertNotEquals(test, object);
        assertNotEquals(test.hashCode(), object.hashCode());

        DeployTask test1 = new DeployTask();
        assertNotEquals(test, test1);
        assertNotEquals(test.hashCode(), test1.hashCode());

        DeployTask test2 = new DeployTask();
        DeployTask test3 = new DeployTask();
        test2.setId(id);
        test3.setId(UUID.randomUUID());
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());

        test2.setDeployRequest(deployRequest);
        DeployRequest deployRequest1 = new DeployRequest();
        deployRequest1.setCsp(Csp.HUAWEI);
        test3.setDeployRequest(deployRequest1);
        assertNotEquals(test, test1);
        assertNotEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());

        test2.setOcl(ocl);
        Ocl ocl1 = new Ocl();
        ocl1.setVersion("version");
        test3.setOcl(ocl1);
        assertNotEquals(test, test1);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());
        assertNotEquals(test, test1);
        assertEquals(test, test2);
        assertNotEquals(test, test3);
        assertNotEquals(test1, test2);
        assertNotEquals(test2, test3);
        assertNotEquals(test.hashCode(), test1.hashCode());
        assertNotEquals(test2.hashCode(), test3.hashCode());
    }

    @Test
    void testToString() {
        assertNotEquals(test.toString(), null);

        String exceptedString = "DeployTask(id=23cc529b-64d9-4875-a2f0-08b415705964, "
                + "namespace=null, "
                + "deployRequest=DeployRequest(super=DeployRequestBase(userId=null, category=null, "
                + "serviceName=null, customerServiceName=null, version=null, region=null, "
                + "csp=null, flavor=null, serviceHostingType=null, ocl=null, "
                + "serviceRequestProperties=null), id=null), ocl=Ocl(category=null, version=null, "
                + "name=null, serviceVersion=null, description=null, namespace=null, icon=null, "
                + "cloudServiceProvider=null, deployment=null, flavors=null, billing=null, "
                + "serviceHostingType=null, serviceProviderContactDetails=null), "
                + "serviceTemplateId=null)";
        assertEquals(exceptedString, test.toString());

    }
}
