package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.xpanse.modules.models.servicetemplate.enums.DeployerKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

class DeployerToolTest {

    private final DeployerKind kind = DeployerKind.TERRAFORM;

    private final String version = "=1.6.0";

    private DeployerTool test;

    @BeforeEach
    void setUp() throws Exception {
        test = new DeployerTool();
        test.setKind(kind);
        test.setVersion(version);
    }

    @Test
    void testGetters() {
        assertEquals(kind, test.getKind());
        assertEquals(version, test.getVersion());
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertThat(test).isNotEqualTo(obj);
        assertThat(test.hashCode()).isNotEqualTo(obj.hashCode());
        DeployerTool test1 = new DeployerTool();
        assertThat(test).isNotEqualTo(test1);
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());
        DeployerTool test2 = new DeployerTool();
        BeanUtils.copyProperties(test, test2);
        assertThat(test).isEqualTo(test2);
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "DeployerTool(kind=" + kind + ", version=" + version + ")";
        assertEquals(expectedString, test.toString());
    }
}
