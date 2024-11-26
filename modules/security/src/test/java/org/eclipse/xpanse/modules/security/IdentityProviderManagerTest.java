package org.eclipse.xpanse.modules.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.security.common.CurrentUserInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class IdentityProviderManagerTest {

    @Mock
    private ApplicationContext mockApplicationContext;
    @Mock
    private IdentityProviderService mockActiveIdentityProviderService;

    @InjectMocks
    private IdentityProviderManager identityProviderManagerUnderTest;

    void setUpSecurityConfig(boolean webSecurityIsEnabled) {
        ReflectionTestUtils.setField(identityProviderManagerUnderTest, "webSecurityIsEnabled",
                webSecurityIsEnabled);
    }

    CurrentUserInfo getMockCurrentUserInfo() {
        final String userId = "userId";
        final String namespace = "namespace";
        final Csp csp = Csp.HUAWEI_CLOUD;
        final List<String> roles = List.of("admin", "csp", "isv", "user");
        final CurrentUserInfo currentUserInfo = new CurrentUserInfo();
        currentUserInfo.setUserId(userId);
        currentUserInfo.setRoles(roles);
        currentUserInfo.setMetadata(Map.of(namespace, namespace, "csp", csp.toValue()));
        currentUserInfo.setNamespace(namespace);
        currentUserInfo.setCsp(csp.toValue());
        return currentUserInfo;
    }

    @Test
    void testLoadActiveIdentityProviderServices() {
        setUpSecurityConfig(true);
        assertThat(identityProviderManagerUnderTest.getActiveIdentityProviderService())
                .isEqualTo(mockActiveIdentityProviderService);
    }

    @Test
    void testGetCurrentUserInfo() {
        // Setup
        final CurrentUserInfo expectedResult = getMockCurrentUserInfo();
        // Configure IdentityProviderService.getCurrentUserInfo(...).
        when(mockActiveIdentityProviderService.getCurrentUserInfo()).thenReturn(
                getMockCurrentUserInfo());
        // Run the test
        final CurrentUserInfo result = identityProviderManagerUnderTest.getCurrentUserInfo();
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetActiveIdentityProviderService() {
        assertThat(identityProviderManagerUnderTest.getActiveIdentityProviderService())
                .isEqualTo(mockActiveIdentityProviderService);
    }
}
