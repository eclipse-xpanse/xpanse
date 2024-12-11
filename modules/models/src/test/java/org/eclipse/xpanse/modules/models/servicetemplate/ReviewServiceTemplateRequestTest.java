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
class ReviewServiceTemplateRequestTest {

    private final ServiceReviewResult reviewResult = ServiceReviewResult.APPROVED;
    private final String reviewComment = "reviewComment";
    private ReviewServiceTemplateRequest test;

    @BeforeEach
    void setUp() {
        test = new ReviewServiceTemplateRequest();
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
        ReviewServiceTemplateRequest test1 = new ReviewServiceTemplateRequest();
        assertNotEquals(test, test1);

        ReviewServiceTemplateRequest test2 = new ReviewServiceTemplateRequest();
        BeanUtils.copyProperties(test, test2);
        assertEquals(test, test2);
    }

    @Test
    void testCanEqual() {
        assertThat(test.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        ReviewServiceTemplateRequest test1 = new ReviewServiceTemplateRequest();
        assertNotEquals(test.hashCode(), test1.hashCode());

        ReviewServiceTemplateRequest test2 = new ReviewServiceTemplateRequest();
        BeanUtils.copyProperties(test, test2);
        assertEquals(test.hashCode(), test2.hashCode());
    }

    @Test
    void testToString() {
        String result =
                "ReviewServiceTemplateRequest(reviewResult="
                        + reviewResult
                        + ", reviewComment="
                        + reviewComment
                        + ")";
        assertThat(test.toString()).isEqualTo(result);
    }
}
