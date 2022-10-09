package org.eclipse.osc.services.ocl.loader;

import org.apache.karaf.boot.Karaf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.stream.Stream;

public class OclLoaderTest {

    @Test
    public void loading() throws Exception {
        Karaf karaf = Karaf.builder().loader(() -> Stream.of(new OclLoader())).build().start();

        OclLoader oclLoader = karaf.getServiceRegistry().get(OclLoader.class);

        Ocl ocl = oclLoader.getOcl(new File("target/test-classes/test.json").toURI().toURL());

        Assertions.assertNotNull(ocl);

        Assertions.assertEquals("flat", ocl.getBilling().getModel());
        Assertions.assertEquals("euro", ocl.getBilling().getCurrency());
        Assertions.assertEquals("monthly", ocl.getBilling().getPeriod());
        Assertions.assertEquals("instance", ocl.getBilling().getVariableItem());
        Assertions.assertEquals(20.0, ocl.getBilling().getFixedPrice());
        Assertions.assertEquals(10.0, ocl.getBilling().getVariablePrice());
    }

}
