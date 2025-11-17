package org.eclipse.xpanse.modules.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.security.auth.common.CurrentUserInfo;
import org.eclipse.xpanse.modules.security.auth.zitadel.ZitadelIdentityProviderService;
import org.eclipse.xpanse.modules.security.config.SecurityProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {IdentityProviderManager.class, SecurityProperties.class})
@TestPropertySource(properties = {"xpanse.security.enable-web-security=true"})
@ActiveProfiles(value = {"oauth", "zitadel"})
@Import(RefreshAutoConfiguration.class)
@ExtendWith(SpringExtension.class)
class IdentityProviderManagerTest {

    @MockitoBean private ZitadelIdentityProviderService mockActiveIdentityProviderService;

    @Autowired private IdentityProviderManager identityProviderManagerUnderTest;

    CurrentUserInfo getMockCurrentUserInfo() {
        final String userId = "userId";
        final String isv = "isv";
        final Csp csp = Csp.HUAWEI_CLOUD;
        final List<String> roles = List.of("admin", "csp", "isv", "user");
        final CurrentUserInfo currentUserInfo = new CurrentUserInfo();
        currentUserInfo.setUserId(userId);
        currentUserInfo.setRoles(roles);
        currentUserInfo.setMetadata(Map.of(isv, isv, "csp", csp.toValue()));
        currentUserInfo.setIsv(isv);
        currentUserInfo.setCsp(csp.toValue());
        return currentUserInfo;
    }

    @Test
    void testLoadActiveIdentityProviderServices() {
        assertThat(identityProviderManagerUnderTest.getActiveIdentityProviderService())
                .isEqualTo(mockActiveIdentityProviderService);
    }

    @Test
    void testGetCurrentUserInfo() {
        // Setup
        final CurrentUserInfo expectedResult = getMockCurrentUserInfo();
        // Configure IdentityProviderService.getCurrentUserInfo(...).
        when(mockActiveIdentityProviderService.getCurrentUserInfo())
                .thenReturn(getMockCurrentUserInfo());
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
