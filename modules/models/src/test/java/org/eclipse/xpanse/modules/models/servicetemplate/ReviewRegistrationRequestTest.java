package org.eclipse.xpanse.modules.models.servicetemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceReviewResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

@ExtendWith(MockitoExtension.class)
class ReviewRegistrationRequestTest {

    private final ServiceReviewResult reviewResult = ServiceReviewResult.APPROVED;
    private final String reviewComment = "reviewComment";
    private ReviewRegistrationRequest test;

    @BeforeEach
    void setUp() {
        test = new ReviewRegistrationRequest();
        test.setReviewResult(reviewResult);
        test.setReviewComment(reviewComment);
    }

    @Test
    void testGetters() {
        assertEquals(reviewComment, test.getReviewComment());
        assertEquals(reviewResult, test.getReviewResult());
    }


    @Test
    void testEquals() {
        ReviewRegistrationRequest test1 = new ReviewRegistrationRequest();
        assertNotEquals(test, test1);

        ReviewRegistrationRequest test2 = new ReviewRegistrationRequest();
        BeanUtils.copyProperties(test, test2);
        assertEquals(test, test2);
    }

    @Test
    void testCanEqual() {
        assertThat(test.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        ReviewRegistrationRequest test1 = new ReviewRegistrationRequest();
        assertNotEquals(test.hashCode(), test1.hashCode());

        ReviewRegistrationRequest test2 = new ReviewRegistrationRequest();
        BeanUtils.copyProperties(test, test2);
        assertEquals(test.hashCode(), test2.hashCode());

    }

    @Test
    void testToString() {
        String result = "ReviewRegistrationRequest(reviewResult=" + reviewResult
                + ", reviewComment=" + reviewComment
                + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
