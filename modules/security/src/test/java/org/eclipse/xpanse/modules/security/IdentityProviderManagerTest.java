package org.eclipse.xpanse.modules.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.xpanse.modules.security.common.CurrentUserInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class IdentityProviderManagerTest {

    @Mock
    private ApplicationContext mockApplicationContext;
    @Mock
    private IdentityProviderService mockActiveIdentityProviderService;

    @InjectMocks
    private IdentityProviderManager identityProviderManagerUnderTest;

    @Test
    void testGetCurrentUserInfo() {
        // Setup
        final CurrentUserInfo expectedResult = new CurrentUserInfo();
        expectedResult.setUserId("userId");
        expectedResult.setUserName("userName");
        expectedResult.setRoles(List.of("value"));
        expectedResult.setMetadata(Map.ofEntries(Map.entry("value", "value")));
        expectedResult.setNamespace("namespace");

        // Configure IdentityProviderService.getCurrentUserInfo(...).
        final CurrentUserInfo currentUserInfo = new CurrentUserInfo();
        currentUserInfo.setUserId("userId");
        currentUserInfo.setUserName("userName");
        currentUserInfo.setRoles(List.of("value"));
        currentUserInfo.setMetadata(Map.ofEntries(Map.entry("value", "value")));
        currentUserInfo.setNamespace("namespace");
        when(mockActiveIdentityProviderService.getCurrentUserInfo()).thenReturn(currentUserInfo);

        // Run the test
        final CurrentUserInfo result = identityProviderManagerUnderTest.getCurrentUserInfo();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetCurrentLoginUserId() {
        // Setup
        // Configure IdentityProviderService.getCurrentUserInfo(...).
        final CurrentUserInfo currentUserInfo = new CurrentUserInfo();
        currentUserInfo.setUserId("userId");
        currentUserInfo.setUserName("userName");
        currentUserInfo.setRoles(List.of("value"));
        currentUserInfo.setMetadata(Map.ofEntries(Map.entry("value", "value")));
        currentUserInfo.setNamespace("namespace");
        when(mockActiveIdentityProviderService.getCurrentUserInfo()).thenReturn(currentUserInfo);

        // Run the test
        final Optional<String> result = identityProviderManagerUnderTest.getCurrentLoginUserId();

        // Verify the results
        assertThat(result).isEqualTo(Optional.of("userId"));
    }

    @Test
    void testGetActiveIdentityProviderService() {
        assertThat(identityProviderManagerUnderTest.getActiveIdentityProviderService())
                .isEqualTo(mockActiveIdentityProviderService);
    }
}
