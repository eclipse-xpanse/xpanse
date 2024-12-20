package org.eclipse.xpanse.modules.security.zitadel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.models.system.enums.IdentityProviderType;
import org.eclipse.xpanse.modules.security.common.CurrentUserInfo;
import org.eclipse.xpanse.modules.security.common.RoleConstants;
import org.eclipse.xpanse.modules.security.common.XpanseAuthentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ZitadelIdentityProviderServiceTest {

    private static final String GRANTED_ROLES_SCOPE = "urn:zitadel:iam:org:project:roles";
    private static final String METADATA_KEY = "urn:zitadel:iam:user:metadata";
    private static final String ISV_KEY = "isv";
    private static final String USERID_KEY = "sub";
    private static final String USERNAME_KEY = "name";
    private final String REQUIRED_SCOPES =
            "openid profile urn:zitadel:iam:org:projects:roles urn:zitadel:iam:user:metadata";
    private static final String CSP_KEY = "csp";

    @InjectMocks private ZitadelIdentityProviderService zitadelIdentityProviderServiceUnderTest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                zitadelIdentityProviderServiceUnderTest,
                "iamServerEndpoint",
                "http://localhost:8081");
        ReflectionTestUtils.setField(
                zitadelIdentityProviderServiceUnderTest, "clientId", "clientId");
        ReflectionTestUtils.setField(
                zitadelIdentityProviderServiceUnderTest, "restTemplate", new RestTemplate());
        ReflectionTestUtils.setField(zitadelIdentityProviderServiceUnderTest, "cspKey", CSP_KEY);
        ReflectionTestUtils.setField(
                zitadelIdentityProviderServiceUnderTest, "requiredScopes", REQUIRED_SCOPES);
        ReflectionTestUtils.setField(
                zitadelIdentityProviderServiceUnderTest, "userIdKey", USERID_KEY);
        ReflectionTestUtils.setField(
                zitadelIdentityProviderServiceUnderTest, "usernameKey", USERNAME_KEY);

        ReflectionTestUtils.setField(
                zitadelIdentityProviderServiceUnderTest, "metadataKey", METADATA_KEY);
        ReflectionTestUtils.setField(zitadelIdentityProviderServiceUnderTest, "isvKey", ISV_KEY);
    }

    Map<String, Object> getAttributesMap(boolean putMetaData) {
        Map<String, Object> map = new HashMap<>();
        map.put(USERID_KEY, "userId");
        map.put("active", true);
        map.put(USERNAME_KEY, "userName");
        map.put(GRANTED_ROLES_SCOPE, new HashMap<>());
        if (putMetaData) {
            Map<String, String> metadataMap = new HashMap<>();
            metadataMap.put(ISV_KEY, "SVNWLUE=");
            map.put(METADATA_KEY, metadataMap);
        }
        return map;
    }

    Collection<GrantedAuthority> getGrantedAuthorityList(String roleName) {
        Collection<GrantedAuthority> roleSet = new HashSet<>();
        roleSet.add(new SimpleGrantedAuthority(roleName));
        return roleSet;
    }

    void mockBearerTokenAuthentication(
            Map<String, Object> attributes, Collection<GrantedAuthority> authorities) {
        XpanseAuthentication authentication = Mockito.mock(XpanseAuthentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(authentication.getClaims()).thenReturn(attributes);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getAuthorities()).thenReturn(authorities);
        SecurityContextHolder.setContext(securityContext);
    }

    void mockJwtTokenAuthentication(
            Map<String, Object> attributes, Collection<GrantedAuthority> authorities) {
        XpanseAuthentication authentication = Mockito.mock(XpanseAuthentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getAuthorities()).thenReturn(authorities);
        Mockito.when(authentication.getClaims()).thenReturn(attributes);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetIdentityProviderType() {
        assertThat(zitadelIdentityProviderServiceUnderTest.getIdentityProviderType())
                .isEqualTo(IdentityProviderType.ZITADEL);
    }

    @Test
    void testGetCurrentUserInfo() {

        Map<String, Object> attributesMap = getAttributesMap(true);
        mockBearerTokenAuthentication(
                attributesMap, getGrantedAuthorityList(RoleConstants.ROLE_ISV));
        // Setup
        final CurrentUserInfo expectedResult = new CurrentUserInfo();
        expectedResult.setUserId("userId");
        expectedResult.setUserName("userName");
        expectedResult.setRoles(List.of("isv"));
        expectedResult.setMetadata(Map.ofEntries(Map.entry(ISV_KEY, "ISV-A")));
        expectedResult.setIsv("ISV-A");

        // Run the test
        final CurrentUserInfo result = zitadelIdentityProviderServiceUnderTest.getCurrentUserInfo();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetCurrentUserInfoWithNull() {
        SecurityContextHolder.clearContext();

        // Run the test
        final CurrentUserInfo result = zitadelIdentityProviderServiceUnderTest.getCurrentUserInfo();

        // Verify the results
        assertNull(result);
    }

    @Test
    void testGetCurrentUserInfoWithNullIsv() {
        Map<String, Object> attributesMap = getAttributesMap(false);
        mockBearerTokenAuthentication(
                attributesMap, getGrantedAuthorityList(RoleConstants.ROLE_USER));
        // Setup
        final CurrentUserInfo expectedResult = new CurrentUserInfo();
        expectedResult.setUserId("userId");
        expectedResult.setUserName("userName");
        expectedResult.setRoles(List.of("user"));

        // Run the test
        final CurrentUserInfo result = zitadelIdentityProviderServiceUnderTest.getCurrentUserInfo();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetCurrentUserInfoWithJwtToken() {

        Map<String, Object> attributesMap = getAttributesMap(true);
        mockJwtTokenAuthentication(attributesMap, getGrantedAuthorityList(RoleConstants.ROLE_ISV));
        // Setup
        final CurrentUserInfo expectedResult = new CurrentUserInfo();
        expectedResult.setUserId("userId");
        expectedResult.setUserName("userName");
        expectedResult.setRoles(List.of("isv"));
        expectedResult.setMetadata(Map.ofEntries(Map.entry(ISV_KEY, "ISV-A")));
        expectedResult.setIsv("ISV-A");

        // Run the test
        final CurrentUserInfo result = zitadelIdentityProviderServiceUnderTest.getCurrentUserInfo();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
}
