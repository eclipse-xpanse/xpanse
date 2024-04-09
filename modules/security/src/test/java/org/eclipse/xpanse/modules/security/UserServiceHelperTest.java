package org.eclipse.xpanse.modules.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.UserNotLoggedInException;
import org.eclipse.xpanse.modules.security.common.CurrentUserInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceHelperTest {

    private final String userId = "userId";
    private final String namespace = "namespace";
    private final Csp csp = Csp.HUAWEI;
    private final List<String> roles = List.of("admin", "csp", "isv", "user");
    @Mock
    private IdentityProviderManager mockIdentityProviderManager;
    @InjectMocks
    private UserServiceHelper userServiceHelperUnderTest;

    void setUpSecurityConfig(boolean webSecurityIsEnabled, boolean roleProtectionIsEnabled) {
        ReflectionTestUtils.setField(userServiceHelperUnderTest, "webSecurityIsEnabled",
                webSecurityIsEnabled);
        ReflectionTestUtils.setField(userServiceHelperUnderTest, "roleProtectionIsEnabled",
                roleProtectionIsEnabled);
    }

    CurrentUserInfo getMockCurrentUserInfo() {
        final CurrentUserInfo currentUserInfo = new CurrentUserInfo();
        currentUserInfo.setUserId(userId);
        currentUserInfo.setRoles(roles);
        currentUserInfo.setMetadata(Map.of(namespace, namespace, "csp", csp.toValue()));
        currentUserInfo.setNamespace(namespace);
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
    void testCurrentUserCanManageNamespace() {
        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final boolean result =
                userServiceHelperUnderTest.currentUserCanManageNamespace(namespace);
        // Verify the results
        assertThat(result).isTrue();

        // Run the test
        final boolean result1 =
                userServiceHelperUnderTest.currentUserCanManageNamespace("namespace1");
        // Verify the results
        assertThat(result1).isTrue();


        // Setup
        setUpSecurityConfig(true, true);
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(getMockCurrentUserInfo());
        // Run the test
        final boolean result2 =
                userServiceHelperUnderTest.currentUserCanManageNamespace(namespace);
        // Verify the results
        assertThat(result2).isTrue();
        // Run the test
        final boolean result3 =
                userServiceHelperUnderTest.currentUserCanManageNamespace("namespace1");
        // Verify the results
        assertThat(result3).isFalse();
    }

    @Test
    void testCurrentUserCanManageNamespace_IdentityProviderManagerReturnsAbsent() {
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(null);
        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final boolean result = userServiceHelperUnderTest.currentUserCanManageNamespace(
                namespace);
        // Verify the results
        assertThat(result).isTrue();
        // Run the test
        final boolean result1 = userServiceHelperUnderTest.currentUserCanManageNamespace(
                "namespace1");
        // Verify the results
        assertThat(result1).isTrue();

        // Setup without auth
        setUpSecurityConfig(true, true);
        // Run the test
        assertThatThrownBy(() ->
                userServiceHelperUnderTest.currentUserCanManageNamespace(namespace))
                .isInstanceOf(UserNotLoggedInException.class);

    }

    @Test
    void testCurrentUserCanManageCsp() {
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(getMockCurrentUserInfo());
        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final boolean result = userServiceHelperUnderTest.currentUserCanManageCsp(Csp.HUAWEI);
        // Verify the results
        assertThat(result).isTrue();
        // Run the test
        final boolean result1 = userServiceHelperUnderTest.currentUserCanManageCsp(Csp.OPENSTACK);
        // Verify the results
        assertThat(result1).isTrue();

        // Setup without auth
        setUpSecurityConfig(true, true);
        // Run the test
        final boolean result2 = userServiceHelperUnderTest.currentUserCanManageCsp(Csp.HUAWEI);
        // Verify the results
        assertThat(result2).isTrue();
        // Run the test
        final boolean result3 = userServiceHelperUnderTest.currentUserCanManageCsp(Csp.OPENSTACK);
        // Verify the results
        assertThat(result3).isFalse();
    }

    @Test
    void testCurrentUserCanManageCsp_IdentityProviderManagerReturnsAbsent() {
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(null);
        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final boolean result = userServiceHelperUnderTest.currentUserCanManageCsp(Csp.HUAWEI);
        // Verify the results
        assertThat(result).isTrue();
        // Run the test
        final boolean result1 = userServiceHelperUnderTest.currentUserCanManageCsp(Csp.OPENSTACK);
        // Verify the results
        assertThat(result1).isTrue();

        // Setup without auth
        setUpSecurityConfig(true, true);
        // Run the test
        assertThatThrownBy(() -> userServiceHelperUnderTest.currentUserCanManageCsp(Csp.HUAWEI))
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
    void testGetNamespaceManagedByCurrentUser() {
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(getMockCurrentUserInfo());
        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final String result = userServiceHelperUnderTest.getCurrentUserManageNamespace();
        // Verify the results
        assertThat(result).isNull();

        // Setup
        setUpSecurityConfig(true, true);
        // Run the test
        final String result1 = userServiceHelperUnderTest.getCurrentUserManageNamespace();
        // Verify the results
        assertThat(result1).isNotNull();
        assertThat(result1).isEqualTo(namespace);
    }

    @Test
    void testGetNamespaceManagedByCurrentUser_IdentityProviderManagerReturnsAbsent() {
        // Setup
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(null);

        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final String result = userServiceHelperUnderTest.getCurrentUserManageNamespace();
        // Verify the results
        assertThat(result).isNull();


        // Setup
        setUpSecurityConfig(true, true);
        // Run the test
        assertThatThrownBy(() ->
                userServiceHelperUnderTest.getCurrentUserManageNamespace())
                .isInstanceOf(UserNotLoggedInException.class);
    }

    @Test
    void testGetCspManagedByCurrentUser() {
        Csp csp = Csp.HUAWEI;
        when(mockIdentityProviderManager.getCurrentUserInfo()).thenReturn(getMockCurrentUserInfo());
        // Setup without auth
        setUpSecurityConfig(false, true);
        // Run the test
        final Csp result = userServiceHelperUnderTest.getCurrentUserManageCsp();
        // Verify the results
        assertThat(result).isNull();

        // Setup
        setUpSecurityConfig(true, true);
        // Run the test
        final Csp result1 = userServiceHelperUnderTest.getCurrentUserManageCsp();
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
        final Csp result = userServiceHelperUnderTest.getCurrentUserManageCsp();
        // Verify the results
        assertThat(result).isNull();


        // Setup
        setUpSecurityConfig(true, true);
        // Run the test
        assertThatThrownBy(() -> userServiceHelperUnderTest.getCurrentUserManageCsp())
                .isInstanceOf(UserNotLoggedInException.class);
    }
}
