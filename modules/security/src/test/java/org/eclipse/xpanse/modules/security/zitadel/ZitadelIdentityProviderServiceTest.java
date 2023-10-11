package org.eclipse.xpanse.modules.security.zitadel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.GRANTED_ROLES_SCOPE;
import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.METADATA_KEY;
import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.NAMESPACE_KEY;
import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.USERID_KEY;
import static org.eclipse.xpanse.modules.security.zitadel.config.ZitadelOauth2Constant.USERNAME_KEY;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.models.security.constant.RoleConstants;
import org.eclipse.xpanse.modules.models.security.model.CurrentUserInfo;
import org.eclipse.xpanse.modules.models.system.enums.IdentityProviderType;
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
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ZitadelIdentityProviderServiceTest {

    @InjectMocks
    private ZitadelIdentityProviderService zitadelIdentityProviderServiceUnderTest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(zitadelIdentityProviderServiceUnderTest, "authTokenType",
                "JWT");
        ReflectionTestUtils.setField(zitadelIdentityProviderServiceUnderTest, "iamServerEndpoint",
                "http://localhost:8081");
        ReflectionTestUtils.setField(zitadelIdentityProviderServiceUnderTest, "clientId",
                "clientId");
    }

    Map<String, Object> getAttributesMap(boolean putMetaData) {
        Map<String, Object> map = new HashMap<>();
        map.put(USERID_KEY, "userId");
        map.put("active", true);
        map.put(USERNAME_KEY, "userName");
        map.put(GRANTED_ROLES_SCOPE, new HashMap<>());
        if (putMetaData) {
            Map<String, String> metadataMap = new HashMap<>();
            metadataMap.put(NAMESPACE_KEY, "SVNWLUE=");
            map.put(METADATA_KEY, metadataMap);
        }
        return map;
    }

    Collection<GrantedAuthority> getGrantedAuthorityList(String roleName) {
        Collection<GrantedAuthority> roleSet = new HashSet<>();
        roleSet.add(new SimpleGrantedAuthority(roleName));
        return roleSet;
    }

    void mockBearerTokenAuthentication(Map<String, Object> attributes,
                                       Collection<GrantedAuthority> authorities) {
        ReflectionTestUtils.setField(zitadelIdentityProviderServiceUnderTest, "authTokenType",
                "OpaqueToken");
        BearerTokenAuthentication authentication = Mockito.mock(BearerTokenAuthentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getTokenAttributes()).thenReturn(attributes);
        Mockito.when(authentication.getAuthorities()).thenReturn(authorities);
        SecurityContextHolder.setContext(securityContext);
    }

    void mockJwtTokenAuthentication(Map<String, Object> attributes,
                                    Collection<GrantedAuthority> authorities) {
        JwtAuthenticationToken authentication = Mockito.mock(JwtAuthenticationToken.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getTokenAttributes()).thenReturn(attributes);
        Mockito.when(authentication.getAuthorities()).thenReturn(authorities);
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
        mockBearerTokenAuthentication(attributesMap,
                getGrantedAuthorityList(RoleConstants.ROLE_ISV));
        // Setup
        final CurrentUserInfo expectedResult = new CurrentUserInfo();
        expectedResult.setUserId("userId");
        expectedResult.setUserName("userName");
        expectedResult.setRoles(List.of("isv"));
        expectedResult.setMetadata(Map.ofEntries(Map.entry(NAMESPACE_KEY, "ISV-A")));
        expectedResult.setNamespace("ISV-A");

        // Run the test
        final CurrentUserInfo result = zitadelIdentityProviderServiceUnderTest.getCurrentUserInfo();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }


    @Test
    void testGetCurrentUserInfoWithNull() {

        // Run the test
        final CurrentUserInfo result = zitadelIdentityProviderServiceUnderTest.getCurrentUserInfo();

        // Verify the results
        assertNull(result);
    }

    @Test
    void testGetCurrentUserInfoWithNullNamespace() {
        Map<String, Object> attributesMap = getAttributesMap(false);
        mockBearerTokenAuthentication(attributesMap,
                getGrantedAuthorityList(RoleConstants.ROLE_USER));
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
        mockJwtTokenAuthentication(attributesMap,
                getGrantedAuthorityList(RoleConstants.ROLE_ISV));
        // Setup
        final CurrentUserInfo expectedResult = new CurrentUserInfo();
        expectedResult.setUserId("userId");
        expectedResult.setUserName("userName");
        expectedResult.setRoles(List.of("isv"));
        expectedResult.setMetadata(Map.ofEntries(Map.entry(NAMESPACE_KEY, "ISV-A")));
        expectedResult.setNamespace("ISV-A");

        // Run the test
        final CurrentUserInfo result = zitadelIdentityProviderServiceUnderTest.getCurrentUserInfo();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
}
