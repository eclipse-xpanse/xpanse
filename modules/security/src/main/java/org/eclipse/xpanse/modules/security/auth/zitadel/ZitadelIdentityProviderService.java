/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.auth.zitadel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.security.TokenResponse;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.BackendSystemType;
import org.eclipse.xpanse.modules.models.system.enums.HealthStatus;
import org.eclipse.xpanse.modules.models.system.enums.IdentityProviderType;
import org.eclipse.xpanse.modules.security.auth.IdentityProviderService;
import org.eclipse.xpanse.modules.security.auth.common.CurrentUserInfo;
import org.eclipse.xpanse.modules.security.auth.common.XpanseAuthentication;
import org.eclipse.xpanse.modules.security.config.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Service for authorization of IAM 'Zitadel'. */
@Slf4j
@Profile("zitadel")
@Service
public class ZitadelIdentityProviderService implements IdentityProviderService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Map<String, String> CODE_CHALLENGE_MAP = initCodeChallengeMap();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RestTemplate restTemplate;
    private final SecurityProperties securityProperties;

    @Autowired
    public ZitadelIdentityProviderService(
            SecurityProperties securityProperties,
            @Qualifier("zitadelRestTemplate") RestTemplate restTemplate) {
        this.securityProperties = securityProperties;
        this.restTemplate = restTemplate;
    }

    private static Map<String, String> initCodeChallengeMap() {
        Map<String, String> map = new HashMap<>(2);
        try {
            byte[] code = new byte[32];
            SECURE_RANDOM.nextBytes(code);
            String verifier = Base64.getEncoder().encodeToString(code);
            map.put("code_verifier", verifier);

            byte[] bytes = verifier.getBytes(StandardCharsets.US_ASCII);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes, 0, bytes.length);
            byte[] digest = md.digest();
            String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
            map.put("code_challenge", challenge);
        } catch (NoSuchAlgorithmException e) {
            log.error("initCodeChallengeMap error.", e);
        }
        log.debug("CODE_CHALLENGE_MAP:{}", map);
        return map;
    }

    @Override
    @Retryable(
            retryFor = RestClientException.class,
            maxAttemptsExpression = "${xpanse.http-client-request.retry-max-attempts}",
            backoff =
                    @Backoff(delayExpression = "${xpanse.http-client-request.delay-milliseconds}"))
    public BackendSystemStatus getIdentityProviderStatus() {
        BackendSystemStatus status = new BackendSystemStatus();
        status.setBackendSystemType(BackendSystemType.IDENTITY_PROVIDER);
        status.setName(IdentityProviderType.ZITADEL.toValue());
        status.setEndpoint(securityProperties.getOauth().getAuthProviderEndpoint());
        status.setHealthStatus(HealthStatus.NOK);
        String healthCheckUrl =
                securityProperties.getOauth().getAuthProviderEndpoint() + "/debug/healthz";
        try {
            ResponseEntity<String> response =
                    restTemplate.getForEntity(healthCheckUrl, String.class);
            if (Objects.equals(HttpStatus.OK, response.getStatusCode())) {
                status.setHealthStatus(HealthStatus.OK);
            } else {
                status.setDetails(response.getBody());
            }
        } catch (RestClientException e) {
            status.setDetails(e.getMessage());
            log.error("Get health status of the IAM error.", e);
        }
        return status;
    }

    @Override
    public IdentityProviderType getIdentityProviderType() {
        return IdentityProviderType.ZITADEL;
    }

    @Override
    public CurrentUserInfo getCurrentUserInfo() {
        XpanseAuthentication authentication =
                (XpanseAuthentication) SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return null;
        }
        Map<String, Object> claimsMap = authentication.getClaims();

        if (Objects.nonNull(claimsMap) && !claimsMap.isEmpty()) {
            CurrentUserInfo currentUserInfo = new CurrentUserInfo();
            if (claimsMap.containsKey(securityProperties.getOauth().getClaims().getUserIdKey())) {
                currentUserInfo.setUserId(
                        String.valueOf(
                                claimsMap.get(
                                        securityProperties.getOauth().getClaims().getUserIdKey())));
            }

            if (claimsMap.containsKey(securityProperties.getOauth().getClaims().getUsernameKey())) {
                currentUserInfo.setUserName(
                        String.valueOf(
                                claimsMap.get(
                                        securityProperties
                                                .getOauth()
                                                .getClaims()
                                                .getUsernameKey())));
            }

            List<String> roles =
                    authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList();
            currentUserInfo.setRoles(roles);

            if (claimsMap.containsKey(securityProperties.getOauth().getClaims().getMetaDataKey())) {
                Object metadataObject =
                        claimsMap.get(securityProperties.getOauth().getClaims().getMetaDataKey());
                if (Objects.nonNull(metadataObject)) {
                    Map<String, String> metadataMap =
                            OBJECT_MAPPER.convertValue(metadataObject, new TypeReference<>() {});
                    if (!metadataMap.isEmpty()) {
                        Map<String, String> userMetadata = new HashMap<>();
                        for (Map.Entry<String, String> entry : metadataMap.entrySet()) {
                            String value =
                                    new String(
                                            Base64.getDecoder().decode(entry.getValue()),
                                            StandardCharsets.UTF_8);
                            userMetadata.put(entry.getKey(), value);
                            if (StringUtils.equals(
                                    securityProperties.getOauth().getMetaData().getIsvKey(),
                                    entry.getKey())) {
                                currentUserInfo.setIsv(value);
                            }
                            if (StringUtils.equals(
                                    securityProperties.getOauth().getMetaData().getCspKey(),
                                    entry.getKey())) {
                                currentUserInfo.setCsp(value);
                            }
                        }
                        currentUserInfo.setMetadata(userMetadata);
                    }
                }
            }
            currentUserInfo.setToken(authentication.getToken());
            return currentUserInfo;
        }
        return null;
    }

    @Override
    public String getAuthorizeUrl() {
        StringBuilder stringBuilder = new StringBuilder();
        String redirectUrl =
                ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
                        + "/auth/token";
        stringBuilder
                .append(securityProperties.getOauth().getAuthProviderEndpoint())
                .append("/oauth/v2/authorize")
                .append("?")
                .append("client_id=")
                .append(securityProperties.getOauth().getSwaggerUi().getClientId())
                .append("&")
                .append("response_type=code")
                .append("&")
                .append("scope=")
                .append(securityProperties.getOauth().getScopes().getRequiredScopes())
                .append("&")
                .append("redirect_uri=")
                .append(redirectUrl)
                .append("&")
                .append("code_challenge_method=S256")
                .append("&")
                .append("code_challenge=")
                .append(CODE_CHALLENGE_MAP.get("code_challenge"));
        return stringBuilder.toString();
    }

    @Override
    public TokenResponse getAccessToken(String code) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("code", code);
        map.add("grant_type", "authorization_code");
        map.add("client_id", securityProperties.getOauth().getSwaggerUi().getClientId());
        map.add("code_verifier", CODE_CHALLENGE_MAP.get("code_verifier"));
        String redirectUrl =
                ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
                        + "/auth/token";
        map.add("redirect_uri", redirectUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> param = new HttpEntity<>(map, headers);

        String tokenUrl =
                securityProperties.getOauth().getAuthProviderEndpoint() + "/oauth/v2/token";
        try {
            ResponseEntity<TokenResponse> response =
                    restTemplate.postForEntity(tokenUrl, param, TokenResponse.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Get access token by code:{} form the IAM error.", code, e);
            throw new AccessDeniedException(e.getMessage());
        }
    }
}
