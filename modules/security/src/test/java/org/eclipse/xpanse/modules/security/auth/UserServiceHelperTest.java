package org.eclipse.xpanse.modules.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.UserNotLoggedInException;
import org.eclipse.xpanse.modules.security.auth.common.CurrentUserInfo;
import org.eclipse.xpanse.modules.security.config.SecurityProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ContextConfiguration(
        classes = {
            UserServiceHelper.class,
            SecurityProperties.class,
            RefreshAutoConfiguration.class,
            RefreshAutoConfiguration.class
        })
@ExtendWith(SpringExtension.class)
class UserServiceHelperTest {

    private final String userId = "userId";
    private final String isv = "isv";
    private final Csp csp = Csp.HUAWEI_CLOUD;
    private final List<String> roles = List.of("admin", "csp", "isv", "user");
    @MockitoBean private IdentityProviderManager mockIdentityProviderManager;
    @Autowired SecurityProperties securityProperties;
    @Autowired private UserServiceHelper userServiceHelperUnderTest;

    void setUpSecurityConfig(boolean webSecurityIsEnabled, boolean roleProtectionIsEnabled) {
        ReflectionTestUtils.setField(securityProperties, "enableWebSecurity", webSecurityIsEnabled);
        ReflectionTestUtils.setField(
                securityProperties, "enableRoleProtection", roleProtectionIsEnabled);
    }

    CurrentUserInfo getMockCurrentUserInfo() {
        final CurrentUserInfo currentUserInfo = new CurrentUserInfo();
        currentUserInfo.setUserId(userId);
        currentUserInfo.setRoles(roles);
        currentUserInfo.setMetadata(Map.of(isv, isv, "csp", csp.toValue()));
        currentUserInfo.setIsv(isv);
        currentUserInfo.setCsp(csp.toValue());
        return currentUserInfo;
    }

    @Test
    void testCurrentUserHasRole() {
        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final boolean result = userServiceHelperUnderTest.currentUserHasRole("user");
        // Verify the results
        assertThat(result).isTrue();

        // Setup with auth but no role protection
        setUpSecurityConfig(true, false);
        // Run the test
        final boolean result1 = userServiceHelperUnderTest.currentUserHasRole("user");
        // Verify the results
        assertThat(result1).isTrue();

        // Setup
        setUpSecurityConfig(true, true);
        final CurrentUserInfo currentUserInfo = new CurrentUserInfo();
        currentUserInfo.setUserId("userId");
        currentUserInfo.setRoles(List.of("user"));
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(currentUserInfo);
        // Run the test
        final boolean result2 = userServiceHelperUnderTest.currentUserHasRole("user");
        // Verify the results
        assertThat(result2).isTrue();
        // Run the test
        final boolean result3 = userServiceHelperUnderTest.currentUserHasRole("isv");
        // Verify the results
        assertThat(result3).isFalse();
    }

    @Test
    void testCurrentUserIsOwner() {
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(getMockCurrentUserInfo());
        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final boolean result = userServiceHelperUnderTest.currentUserIsOwner("userId");
        // Verify the results
        assertThat(result).isTrue();

        // Setup
        setUpSecurityConfig(true, true);
        // Run the test
        final boolean result1 = userServiceHelperUnderTest.currentUserIsOwner("userId");
        // Verify the results
        assertThat(result1).isTrue();
        // Run the test
        final boolean result2 = userServiceHelperUnderTest.currentUserIsOwner("userId2");
        // Verify the results
        assertThat(result2).isFalse();
    }

    @Test
    void testCurrentUserIsOwner_IdentityProviderManagerReturnsAbsent() {
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(null);
        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final boolean result = userServiceHelperUnderTest.currentUserIsOwner("ownerId");
        // Verify the results
        assertThat(result).isTrue();
        // Run the test
        final boolean result1 = userServiceHelperUnderTest.currentUserIsOwner("ownerId1");
        // Verify the results
        assertThat(result1).isTrue();

        // Setup without auth
        setUpSecurityConfig(true, true);
        // Run the test
        assertThatThrownBy(() -> userServiceHelperUnderTest.currentUserIsOwner("ownerId"))
                .isInstanceOf(UserNotLoggedInException.class);
    }

    @Test
    void testCurrentUserCanManageIsv() {
        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final boolean result = userServiceHelperUnderTest.currentUserCanManageIsv(isv);
        // Verify the results
        assertThat(result).isTrue();

        // Run the test
        final boolean result1 = userServiceHelperUnderTest.currentUserCanManageIsv("isv1");
        // Verify the results
        assertThat(result1).isTrue();

        // Setup
        setUpSecurityConfig(true, true);
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(getMockCurrentUserInfo());
        // Run the test
        final boolean result2 = userServiceHelperUnderTest.currentUserCanManageIsv(isv);
        // Verify the results
        assertThat(result2).isTrue();
        // Run the test
        final boolean result3 = userServiceHelperUnderTest.currentUserCanManageIsv("isv1");
        // Verify the results
        assertThat(result3).isFalse();
    }

    @Test
    void testCurrentUserCanManageIsv_IdentityProviderManagerReturnsAbsent() {
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(null);
        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final boolean result = userServiceHelperUnderTest.currentUserCanManageIsv(isv);
        // Verify the results
        assertThat(result).isTrue();
        // Run the test
        final boolean result1 = userServiceHelperUnderTest.currentUserCanManageIsv("isv1");
        // Verify the results
        assertThat(result1).isTrue();

        // Setup without auth
        setUpSecurityConfig(true, true);
        // Run the test
        assertThatThrownBy(() -> userServiceHelperUnderTest.currentUserCanManageIsv(isv))
                .isInstanceOf(UserNotLoggedInException.class);
    }

    @Test
    void testCurrentUserCanManageCsp() {
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(getMockCurrentUserInfo());
        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final boolean result = userServiceHelperUnderTest.currentUserCanManageCsp(Csp.HUAWEI_CLOUD);
        // Verify the results
        assertThat(result).isTrue();
        // Run the test
        final boolean result1 =
                userServiceHelperUnderTest.currentUserCanManageCsp(Csp.OPENSTACK_TESTLAB);
        // Verify the results
        assertThat(result1).isTrue();

        // Setup without auth
        setUpSecurityConfig(true, true);
        // Run the test
        final boolean result2 =
                userServiceHelperUnderTest.currentUserCanManageCsp(Csp.HUAWEI_CLOUD);
        // Verify the results
        assertThat(result2).isTrue();
        // Run the test
        final boolean result3 =
                userServiceHelperUnderTest.currentUserCanManageCsp(Csp.OPENSTACK_TESTLAB);
        // Verify the results
        assertThat(result3).isFalse();
    }

    @Test
    void testCurrentUserCanManageCsp_IdentityProviderManagerReturnsAbsent() {
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(null);
        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final boolean result = userServiceHelperUnderTest.currentUserCanManageCsp(Csp.HUAWEI_CLOUD);
        // Verify the results
        assertThat(result).isTrue();
        // Run the test
        final boolean result1 =
                userServiceHelperUnderTest.currentUserCanManageCsp(Csp.OPENSTACK_TESTLAB);
        // Verify the results
        assertThat(result1).isTrue();

        // Setup without auth
        setUpSecurityConfig(true, true);
        // Run the test
        assertThatThrownBy(
                        () -> userServiceHelperUnderTest.currentUserCanManageCsp(Csp.HUAWEI_CLOUD))
                .isInstanceOf(UserNotLoggedInException.class);
    }

    @Test
    void testGetCurrentUserId() {
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(getMockCurrentUserInfo());
        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final String result = userServiceHelperUnderTest.getCurrentUserId();
        // Verify the results
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("no-auth-user-id");

        // Setup
        setUpSecurityConfig(true, true);
        // Run the test
        final String result1 = userServiceHelperUnderTest.getCurrentUserId();
        // Verify the results
        assertThat(result1).isNotNull();
        assertThat(result1).isEqualTo(userId);
    }

    @Test
    void testGetCurrentUserId_IdentityProviderManagerReturnsAbsent() {
        // Setup
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(null);
        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final String result = userServiceHelperUnderTest.getCurrentUserId();
        // Verify the results
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("no-auth-user-id");

        // Setup
        setUpSecurityConfig(true, true);
        // Run the test
        assertThatThrownBy(() -> userServiceHelperUnderTest.getCurrentUserId())
                .isInstanceOf(UserNotLoggedInException.class);
    }

    @Test
    void testGetIsvManagedByCurrentUser() {
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(getMockCurrentUserInfo());
        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final String result = userServiceHelperUnderTest.getIsvManagedByCurrentUser();
        // Verify the results
        assertThat(result).isNull();

        // Setup
        setUpSecurityConfig(true, true);
        // Run the test
        final String result1 = userServiceHelperUnderTest.getIsvManagedByCurrentUser();
        // Verify the results
        assertThat(result1).isNotNull();
        assertThat(result1).isEqualTo(isv);
    }

    @Test
    void testGetIsvManagedByCurrentUser_IdentityProviderManagerReturnsAbsent() {
        // Setup
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(null);

        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final String result = userServiceHelperUnderTest.getIsvManagedByCurrentUser();
        // Verify the results
        assertThat(result).isNull();

        // Setup
        setUpSecurityConfig(true, true);
        // Run the test
        assertThatThrownBy(() -> userServiceHelperUnderTest.getIsvManagedByCurrentUser())
                .isInstanceOf(UserNotLoggedInException.class);
    }

    @Test
    void testGetCspManagedByCurrentUser() {
        Csp csp = Csp.HUAWEI_CLOUD;
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(getMockCurrentUserInfo());
        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final Csp result = userServiceHelperUnderTest.getCspManagedByCurrentUser();
        // Verify the results
        assertThat(result).isNull();

        // Setup
        setUpSecurityConfig(true, true);
        // Run the test
        final Csp result1 = userServiceHelperUnderTest.getCspManagedByCurrentUser();
        // Verify the results
        assertThat(result1).isNotNull();
        assertThat(result1).isEqualTo(csp);
    }

    @Test
    void testGetCspManagedByCurrentUser_IdentityProviderManagerReturnsAbsent() {
        // Setup
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(null);

        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final Csp result = userServiceHelperUnderTest.getCspManagedByCurrentUser();
        // Verify the results
        assertThat(result).isNull();

        // Setup
        setUpSecurityConfig(true, true);
        // Run the test
        assertThatThrownBy(() -> userServiceHelperUnderTest.getCspManagedByCurrentUser())
                .isInstanceOf(UserNotLoggedInException.class);
    }
}
