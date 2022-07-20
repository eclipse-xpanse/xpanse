package org.eclipse.osc.core.ocl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

public class OclTest {

    @Test
    public void loading() throws Exception {
        Ocl ocl = Ocl.load(new File("target/test-classes/test.yaml").toURI().toURL());

        Assertions.assertNotNull(ocl);

        Assertions.assertEquals(">=0.1.0", ocl.getOsc().getOsc());
        Assertions.assertEquals("my-test-name", ocl.getOsc().getName());
        Assertions.assertEquals("1.0.0", ocl.getOsc().getVersion());
        Assertions.assertEquals("foo-bar", ocl.getOsc().getNamespace());
        Assertions.assertEquals("test-region", ocl.getOsc().getRegion());

        Assertions.assertEquals("renting", ocl.getBilling().getModel());
        Assertions.assertEquals("euro", ocl.getBilling().getCurrency());
        Assertions.assertEquals("monthly", ocl.getBilling().getPeriod());
        Assertions.assertEquals("instance", ocl.getBilling().getVariableItem());
        Assertions.assertEquals(20.0, ocl.getBilling().getFixedPrice());
        Assertions.assertEquals(10.0, ocl.getBilling().getVariablePrice());

        Assertions.assertEquals("my-vpc", ocl.getNetwork().getVpc().getName());
        Assertions.assertEquals("192.168.1.0/24", ocl.getNetwork().getVpc().getCidr());
        Assertions.assertEquals("my-vpc", ocl.getNetwork().getSubnet().getVpc());
        Assertions.assertEquals("mysubnet", ocl.getNetwork().getSubnet().getName());
        Assertions.assertEquals("192.168.1.0/26", ocl.getNetwork().getSubnet().getCidr());
        Assertions.assertEquals("192.168.1.1", ocl.getNetwork().getSubnet().getGateway());

        Assertions.assertEquals("a", ocl.getNetwork().getDns().get(0).getEntry());
        Assertions.assertEquals("my-domain", ocl.getNetwork().getDns().get(0).getDomain());
        Assertions.assertEquals("", ocl.getNetwork().getDns().get(0).getNodes());
    }

}
