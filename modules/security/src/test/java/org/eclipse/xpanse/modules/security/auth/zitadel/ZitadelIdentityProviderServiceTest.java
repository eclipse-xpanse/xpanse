package org.eclipse.xpanse.modules.security.auth.zitadel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.eclipse.xpanse.modules.models.system.enums.IdentityProviderType;
import org.eclipse.xpanse.modules.security.auth.common.CurrentUserInfo;
import org.eclipse.xpanse.modules.security.auth.common.RoleConstants;
import org.eclipse.xpanse.modules.security.auth.common.XpanseAuthentication;
import org.eclipse.xpanse.modules.security.config.SecurityProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@ContextConfiguration(
        classes = {
            ZitadelIdentityProviderService.class,
            SecurityProperties.class,
            RefreshAutoConfiguration.class
        })
@ExtendWith(SpringExtension.class)
@ActiveProfiles(value = {"oauth", "zitadel"})
class ZitadelIdentityProviderServiceTest {

    private static final String GRANTED_ROLES_SCOPE = "urn:zitadel:iam:org:project:roles";
    private static final String METADATA_KEY = "urn:zitadel:iam:user:metadata";
    private static final String ISV_KEY = "isv";
    private static final String USERID_KEY = "sub";
    private static final String USERNAME_KEY = "name";
    private static final String CSP_KEY = "csp";

    @MockitoBean
    @Qualifier("zitadelRestTemplate")
    RestTemplate restTemplate;

    @Autowired private ZitadelIdentityProviderService zitadelIdentityProviderServiceUnderTest;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        String yamlContent =
                """
                    xpanse:
                     security:
                       oauth:
                         swagger-ui:
                           auth-url: ${xpanse.security.oauth.auth-provider-endpoint}/oauth/v2/authorize
                           token:url: ${xpanse.security.oauth.auth-provider-endpoint}/oauth/v2/authorize
                         scopes:
                           openid: "openid"
                           profile: "profile"
                           metadata: "urn:zitadel:iam:user:metadata"
                           roles: "urn:zitadel:iam:org:project:roles"
                           required-scopes:  "openid profile urn:zitadel:iam:org:projects:roles urn:zitadel:iam:user:metadata"
                         # keys to parse the claims map.
                         claims:
                           username-key: "name"
                           meta-data-key: "urn:zitadel:iam:user:metadata"
                           granted-roles-key: "urn:zitadel:iam:user:metadata"
                           user-id-key: "sub"
                         meta-data:
                           isv-key: "isv"
                           csp-key: "csp"
                         default-role: user
                """;

        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ByteArrayResource(yamlContent.getBytes()));
        Properties properties = yaml.getObject();

        assert properties != null;
        properties.forEach((key, value) -> registry.add(key.toString(), () -> value));
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
