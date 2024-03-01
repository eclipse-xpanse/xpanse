package org.eclipse.xpanse.modules.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
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

    void mockCurrentUserInfoWithCspAndNamespace(String csp, String namespace) {
        when(mockActiveIdentityProviderService.getCurrentUserInfo()).thenReturn(
                getCurrentUserInfo(csp, namespace));
    }

    CurrentUserInfo getCurrentUserInfo(String csp, String namespace) {
        final CurrentUserInfo currentUserInfo = new CurrentUserInfo();
        currentUserInfo.setUserId("userId");
        currentUserInfo.setUserName("userName");
        currentUserInfo.setRoles(List.of("admin", "csp", "isv"));
        currentUserInfo.setMetadata(Map.of("namespace", namespace, "csp", csp));
        currentUserInfo.setNamespace(namespace);
        currentUserInfo.setCsp(csp);
        return currentUserInfo;
    }


    @Test
    void testGetCurrentUserInfo() {
        // Setup
        final CurrentUserInfo expectedResult = getCurrentUserInfo("huawei", "ISV-A");
        // Configure IdentityProviderService.getCurrentUserInfo(...).
        mockCurrentUserInfoWithCspAndNamespace("huawei", "ISV-A");
        // Run the test
        final CurrentUserInfo result = identityProviderManagerUnderTest.getCurrentUserInfo();
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);

        // Run the test
        final Optional<String> idResult = identityProviderManagerUnderTest.getCurrentLoginUserId();
        // Verify the results
        assertThat(idResult).isEqualTo(Optional.of("userId"));
    }

    @Test
    void testGetUserNamespace() {
        // Setup
        final String expectedResult = "ISV-A";
        // Configure IdentityProviderService.getCurrentUserInfo(...).
        mockCurrentUserInfoWithCspAndNamespace("huawei", "ISV-A");
        // Run the test
        final Optional<String> result = identityProviderManagerUnderTest.getUserNamespace();
        // Verify the results
        assertThat(result).isEqualTo(Optional.of(expectedResult));

        // Setup
        final String expectedResult2 = "userId";
        // Configure IdentityProviderService.getCurrentUserInfo(...).
        mockCurrentUserInfoWithCspAndNamespace("huawei", "");
        // Run the test
        final Optional<String> result2 = identityProviderManagerUnderTest.getUserNamespace();
        // Verify the results
        assertThat(result2).isEqualTo(Optional.of(expectedResult2));
    }

    @Test
    void testGetCspFromMetadata() {
        // Setup
        final Optional<Csp> expectedResult = Optional.of(Csp.HUAWEI);
        // Configure IdentityProviderService.getCurrentUserInfo(...).
        mockCurrentUserInfoWithCspAndNamespace("huawei", "ISV-A");
        // Run the test
        final Optional<Csp> result = identityProviderManagerUnderTest.getCspFromMetadata();
        // Verify the results
        assertThat(result).isEqualTo((expectedResult));


        // Setup
        final Optional<Csp> expectedResult2 = Optional.empty();
        // Configure IdentityProviderService.getCurrentUserInfo(...).
        mockCurrentUserInfoWithCspAndNamespace("errorValue", "ISV-A");

        // Run the test
        final Optional<Csp> result2 = identityProviderManagerUnderTest.getCspFromMetadata();
        // Verify the results
        assertThat(result2).isEqualTo(expectedResult2);
    }

    @Test
    void testGetActiveIdentityProviderService() {
        assertThat(identityProviderManagerUnderTest.getActiveIdentityProviderService())
                .isEqualTo(mockActiveIdentityProviderService);
    }
}
