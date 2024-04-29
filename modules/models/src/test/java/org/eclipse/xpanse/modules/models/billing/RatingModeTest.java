package org.eclipse.xpanse.modules.models.billing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class RatingModeTest {

    private final boolean isPriceOnlyForManagementLayer = true;
    @Mock
    private ResourceUsage mockResourceUsage;
    @Mock
    private Price mockFixedPrice;
    private RatingMode ratingModeUnderTest;

    @BeforeEach
    void setUp() {
        ratingModeUnderTest = new RatingMode();
        ratingModeUnderTest.setResourceUsage(mockResourceUsage);
        ratingModeUnderTest.setFixedPrice(mockFixedPrice);
        ratingModeUnderTest.setIsPriceOnlyForManagementLayer(isPriceOnlyForManagementLayer);
    }

    @Test
    void testGetters() {
        assertThat(ratingModeUnderTest.getResourceUsage()).isEqualTo(mockResourceUsage);
        assertThat(ratingModeUnderTest.getFixedPrice()).isEqualTo(mockFixedPrice);
        assertThat(ratingModeUnderTest.getIsPriceOnlyForManagementLayer()).isTrue();
    }


    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(ratingModeUnderTest.equals(o)).isFalse();
        assertThat(ratingModeUnderTest.canEqual(o)).isFalse();
        assertThat(ratingModeUnderTest.hashCode()).isNotEqualTo(o.hashCode());

        RatingMode ratingMode = new RatingMode();
        assertThat(ratingModeUnderTest.equals(ratingMode)).isFalse();
        assertThat(ratingModeUnderTest.canEqual(ratingMode)).isTrue();
        assertThat(ratingModeUnderTest.hashCode()).isNotEqualTo(ratingMode.hashCode());

        BeanUtils.copyProperties(ratingModeUnderTest, ratingMode);
        assertThat(ratingModeUnderTest.equals(ratingMode)).isTrue();
        assertThat(ratingModeUnderTest.canEqual(ratingMode)).isTrue();
        assertThat(ratingModeUnderTest.hashCode()).isEqualTo(ratingMode.hashCode());
    }


    @Test
    void testToString() {
        String result =
                "RatingMode(fixedPrice=" + mockFixedPrice + ", resourceUsage=" + mockResourceUsage
                        + ", isPriceOnlyForManagementLayer=" + isPriceOnlyForManagementLayer + ")";
        assertThat(ratingModeUnderTest.toString()).isEqualTo(result);
    }
}
