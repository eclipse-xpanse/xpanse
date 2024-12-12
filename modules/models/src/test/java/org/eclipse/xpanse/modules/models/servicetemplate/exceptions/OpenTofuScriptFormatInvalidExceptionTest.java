package org.eclipse.xpanse.modules.models.servicetemplate.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpenTofuScriptFormatInvalidExceptionTest {

    @Mock private List<String> mockErrorReasons;

    private OpenTofuScriptFormatInvalidException openTofuScriptFormatInvalidExceptionUnderTest;

    @BeforeEach
    void setUp() {
        openTofuScriptFormatInvalidExceptionUnderTest =
                new OpenTofuScriptFormatInvalidException(mockErrorReasons);
    }

    @Test
    void testGetErrorReasons() {
        assertThat(openTofuScriptFormatInvalidExceptionUnderTest.getErrorReasons())
                .isEqualTo(mockErrorReasons);
    }

    @Test
    void testToString() {
        String result = "OpenTofuScriptFormatInvalidException(errorReasons=mockErrorReasons)";
        assertThat(openTofuScriptFormatInvalidExceptionUnderTest.toString()).isEqualTo(result);
    }

    @Test
    void testEquals() {
        assertThat(openTofuScriptFormatInvalidExceptionUnderTest.equals("o")).isFalse();
    }

    @Test
    void testCanEqual() {
        assertThat(openTofuScriptFormatInvalidExceptionUnderTest.canEqual("other")).isFalse();
    }

    @Test
    void testHashCode() {
        OpenTofuScriptFormatInvalidException test =
                new OpenTofuScriptFormatInvalidException(new ArrayList<>());
        assertThat(openTofuScriptFormatInvalidExceptionUnderTest.hashCode())
                .isNotEqualTo(test.hashCode());
    }
}
