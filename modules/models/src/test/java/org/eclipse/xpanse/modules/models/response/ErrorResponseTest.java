package org.eclipse.xpanse.modules.models.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

class ErrorResponseTest {

    private final ErrorType errorType = ErrorType.ACCESS_DENIED;
    private final List<String> details = List.of(ErrorType.ACCESS_DENIED.toValue());
    private final Boolean success = false;
    private ErrorResponse test;

    @BeforeEach
    void setUp() {
        test = new ErrorResponse();
        test.setErrorType(errorType);
        test.setDetails(details);
    }

    @Test
    void testErrorResponse() {
        // Run the test
        final ErrorResponse result = ErrorResponse.errorResponse(errorType, details);
        assertThat(result.getErrorType()).isEqualTo(errorType);
        assertThat(result.getDetails()).isEqualTo(details);
    }

    @Test
    void testGetters() {
        assertThat(test.getErrorType()).isEqualTo(errorType);
        assertThat(test.getDetails()).isEqualTo(details);
    }

    @Test
    void testEqualsAndHashCode() {
        Object o = new Object();
        assertThat(test.canEqual(o)).isFalse();
        assertThat(test.equals(o)).isFalse();
        assertThat(test.hashCode()).isNotEqualTo(o.hashCode());

        ErrorResponse test2 = new ErrorResponse();
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
                "ErrorResponse(errorType=ACCESS_DENIED, details=[Access Denied])";
        assertThat(test.toString()).isEqualTo(result);
    }
}
