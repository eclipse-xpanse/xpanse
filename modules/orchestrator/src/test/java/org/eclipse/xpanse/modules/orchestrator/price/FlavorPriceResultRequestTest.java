package org.eclipse.xpanse.modules.orchestrator.price;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.eclipse.xpanse.modules.models.billing.RatingMode;
import org.eclipse.xpanse.modules.models.billing.enums.BillingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class FlavorPriceResultRequestTest {

    private final String serviceTemplateId = UUID.randomUUID().toString();
    private final String flavorName = "flavorName";

    private final String regionName = "regionName";
    private final String userId = "userId";
    @Mock
    private RatingMode mockRatingMode;
    @Mock
    private BillingMode mockBillingMode;
    private ServiceFlavorPriceRequest test;

    @BeforeEach
    void setUp() {
        test = new ServiceFlavorPriceRequest();
        test.setServiceTemplateId(serviceTemplateId);
        test.setFlavorName(flavorName);
        test.setUserId(userId);
        test.setRegionName(regionName);
        test.setFlavorRatingMode(mockRatingMode);
        test.setBillingMode(mockBillingMode);
    }

    @Test
    void testGetters() {
        assertThat(test.getUserId()).isEqualTo(userId);
        assertThat(test.getRegionName()).isEqualTo(regionName);
        assertThat(test.getBillingMode()).isEqualTo(mockBillingMode);
    }

    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(test.canEqual(o)).isFalse();
        assertThat(test).isNotEqualTo(o);
        assertThat(test.hashCode()).isNotEqualTo(o.hashCode());

        ServiceFlavorPriceRequest test1 = new ServiceFlavorPriceRequest();
        assertThat(test.canEqual(test1)).isTrue();
        assertThat(test).isNotEqualTo(test1);
        assertThat(test.hashCode()).isNotEqualTo(test1.hashCode());

        BeanUtils.copyProperties(test, test1);
        assertThat(test.canEqual(test1)).isTrue();
        assertThat(test).isEqualTo(test1);
        assertThat(test.hashCode()).isEqualTo(test1.hashCode());

    }

    @Test
    void testToString() {
        String result = "ServiceFlavorPriceRequest(serviceTemplateId=" + serviceTemplateId
                + ", flavorName=" + flavorName + ", userId=" + userId + ", regionName=" + regionName
                + ", flavorRatingMode=" + mockRatingMode + ", billingMode=" + mockBillingMode + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
