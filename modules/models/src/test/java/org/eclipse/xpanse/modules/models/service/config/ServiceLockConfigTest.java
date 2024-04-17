package org.eclipse.xpanse.modules.models.service.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServiceLockConfigTest {

    private ServiceLockConfig test;

    @BeforeEach
    void setUp() {
        test = new ServiceLockConfig();
        test.setDestroyLocked(true);
        test.setModifyLocked(true);
    }

    @Test
    void testGetters() {
        assertThat(test.isDestroyLocked()).isTrue();
        assertThat(test.isModifyLocked()).isTrue();

    }

    @Test
    void testCanEqual() {
        assertThat(test.canEqual("other")).isFalse();
        assertThat(test.canEqual(new ServiceLockConfig())).isTrue();
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertThat(test).isNotEqualTo(obj);
        assertThat(test.hashCode()).isNotEqualTo(obj.hashCode());

        ServiceLockConfig test1 = new ServiceLockConfig();
        assertThat(test).isNotEqualTo(test1);
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());

        test1.setDestroyLocked(true);
        test1.setModifyLocked(true);
        assertThat(test).isEqualTo(test1);
        assertThat(test.hashCode()).isEqualTo(test1.hashCode());
    }

    @Test
    void testToString() {
        String result = "ServiceLockConfig(isDestroyLocked=true, isModifyLocked=true)";
        assertThat(test.toString()).isEqualTo(result);
    }
}
