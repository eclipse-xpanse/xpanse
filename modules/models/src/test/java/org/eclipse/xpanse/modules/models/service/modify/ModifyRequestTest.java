package org.eclipse.xpanse.modules.models.service.modify;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class ModifyRequestTest {

    private final String userId = "userId";
    private final String customerServiceName = "customerService";
    private final String flavor = "flavor";
    @Mock private Map<String, Object> mockServiceRequestProperties;

    private ModifyRequest test;

    @BeforeEach
    void setUp() {
        test = new ModifyRequest();
        test.setUserId(userId);
        test.setCustomerServiceName(customerServiceName);
        test.setFlavor(flavor);
        test.setServiceRequestProperties(mockServiceRequestProperties);
    }

    @Test
    void testGetters() {
        assertThat(test.getUserId()).isEqualTo(userId);
        assertThat(test.getFlavor()).isEqualTo(flavor);
        assertThat(test.getCustomerServiceName()).isEqualTo(customerServiceName);
        assertThat(test.getServiceRequestProperties()).isEqualTo(mockServiceRequestProperties);
    }

    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(test.canEqual(o)).isFalse();
        assertThat(test.equals(o)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(o.hashCode());

        ModifyRequest test2 = new ModifyRequest();
        assertThat(test.canEqual(test2)).isTrue();
        assertThat(test.equals(test2)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(test2.hashCode());

        BeanUtils.copyProperties(test, test2);
        assertThat(test.canEqual(test2)).isTrue();
        assertThat(test.equals(test2)).isTrue();
        assertThat(test.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testToString() {
        String result =
                "ModifyRequest(userId="
                        + userId
                        + ", customerServiceName="
                        + customerServiceName
                        + ", flavor="
                        + flavor
                        + ", serviceRequestProperties="
                        + mockServiceRequestProperties
                        + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
