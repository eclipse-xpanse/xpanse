package org.eclipse.xpanse.modules.models.workflow.migrate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MigrateRequestTest {

    private MigrateRequest migrateRequestUnderTest;

    @BeforeEach
    void setUp() {
        migrateRequestUnderTest = new MigrateRequest();
    }

    @Test
    void testIdGetterAndSetter() {
        final UUID id = UUID.fromString("c490ed22-e795-4ef1-a92e-349b14982892");
        migrateRequestUnderTest.setId(id);
        assertThat(migrateRequestUnderTest.getId()).isEqualTo(id);
    }

    @Test
    void testEquals() {
        assertThat(migrateRequestUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(migrateRequestUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        MigrateRequest test = new MigrateRequest();
        assertThat(migrateRequestUnderTest.hashCode()).isEqualTo(test.hashCode());
    }

    @Test
    void testToString() {
        String result = "MigrateRequest(super=DeployRequestBase(userId=null, category=null, "
                + "serviceName=null, customerServiceName=null, version=null, region=null, csp=null,"
                + " flavor=null, serviceHostingType=null, ocl=null, serviceRequestProperties=null), id=null)";
        assertThat(migrateRequestUnderTest.toString()).isEqualTo(result);
    }
}
