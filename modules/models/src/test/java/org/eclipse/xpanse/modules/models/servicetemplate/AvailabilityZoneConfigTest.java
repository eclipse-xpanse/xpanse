package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AvailabilityZoneConfigTest {
    private final String displayName = "displayName";
    private final String varName = "varName";
    private final String description = "description";
    private final Boolean mandatory = true;
    private AvailabilityZoneConfig test;

    @BeforeEach
    void setUp() throws Exception {
        test = new AvailabilityZoneConfig();
        test.setDisplayName(displayName);
        test.setVarName(varName);
        test.setDescription(description);
        test.setMandatory(mandatory);
    }

    @Test
    void testGetters() {
        assertThat(test.getDisplayName()).isEqualTo(displayName);
        assertThat(test.getVarName()).isEqualTo(varName);
        assertThat(test.getDescription()).isEqualTo(description);
        assertThat(test.getMandatory()).isEqualTo(mandatory);
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertThat(test).isNotEqualTo(obj);
        assertThat(test.hashCode()).isNotEqualTo(obj.hashCode());
        AvailabilityZoneConfig test1 = new AvailabilityZoneConfig();
        assertThat(test).isNotEqualTo(test1);
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());
        AvailabilityZoneConfig test2 = new AvailabilityZoneConfig();
        test2.setDisplayName(displayName);
        test2.setVarName(varName);
        test2.setDescription(description);
        test2.setMandatory(mandatory);
        assertThat(test).isEqualTo(test2);
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testCanEqual() {
        assertThat(test.canEqual("other")).isFalse();
        assertThat(test.canEqual(new AvailabilityZoneConfig())).isTrue();
    }

    @Test
    void testToString() throws Exception {
        String result =
                "AvailabilityZoneConfig(displayName="
                        + displayName
                        + ", varName="
                        + varName
                        + ", mandatory="
                        + mandatory
                        + ", description="
                        + description
                        + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
