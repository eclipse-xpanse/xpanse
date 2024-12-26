/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.zitadel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
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
import org.eclipse.xpanse.modules.security.IdentityProviderService;
import org.eclipse.xpanse.modules.security.common.CurrentUserInfo;
import org.eclipse.xpanse.modules.security.common.XpanseAuthentication;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
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

    private static final Map<String, String> CODE_CHALLENGE_MAP = initCodeChallengeMap();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Qualifier("zitadelRestTemplate")
    @Resource
    private RestTemplate restTemplate;

    @Value("${authorization.server.endpoint}")
    private String iamServerEndpoint;

    @Value("${authorization.swagger.ui.client.id}")
    private String clientId;

    @Value("${authorization.csp.key}")
    private String cspKey;

    @Value("${authorization.required.scopes}")
    private String requiredScopes;

    @Value("${authorization.userid.key}")
    private String userIdKey;

    @Value("${authorization.username.key}")
    private String usernameKey;

    @Value("${authorization.metadata.key}")
    private String metadataKey;

    @Value("${authorization.isv.key}")
    private String isvKey;

    private static Map<String, String> initCodeChallengeMap() {
        Map<String, String> map = new HashMap<>(2);
        try {
            SecureRandom sr = new SecureRandom();
            byte[] code = new byte[32];
            sr.nextBytes(code);
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
            maxAttemptsExpression = "${http.request.retry.max.attempts}",
            backoff = @Backoff(delayExpression = "${http.request.retry.delay.milliseconds}"))
    public BackendSystemStatus getIdentityProviderStatus() {
        BackendSystemStatus status = new BackendSystemStatus();
        status.setBackendSystemType(BackendSystemType.IDENTITY_PROVIDER);
        status.setName(IdentityProviderType.ZITADEL.toValue());
        status.setEndpoint(iamServerEndpoint);
        status.setHealthStatus(HealthStatus.NOK);
        String healthCheckUrl = iamServerEndpoint + "/debug/healthz";
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
            if (claimsMap.containsKey(userIdKey)) {
                currentUserInfo.setUserId(String.valueOf(claimsMap.get(userIdKey)));
            }

            if (claimsMap.containsKey(usernameKey)) {
                currentUserInfo.setUserName(String.valueOf(claimsMap.get(usernameKey)));
            }

            List<String> roles =
                    authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList();
            currentUserInfo.setRoles(roles);

            if (claimsMap.containsKey(metadataKey)) {
                Object metadataObject = claimsMap.get(metadataKey);
                if (Objects.nonNull(metadataObject)) {
                    Map<String, String> metadataMap =
                            OBJECT_MAPPER.convertValue(metadataObject, new TypeReference<>() {});
                    if (!metadataMap.isEmpty()) {
                        Map<String, String> userMetadata = new HashMap<>();
                        for (String key : metadataMap.keySet()) {
                            String value =
                                    new String(
                                            Base64.getDecoder().decode(metadataMap.get(key)),
                                            StandardCharsets.UTF_8);
                            userMetadata.put(key, value);
                            if (StringUtils.equals(isvKey, key)) {
                                currentUserInfo.setIsv(value);
                            }
                            if (StringUtils.equals(cspKey, key)) {
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
                .append(iamServerEndpoint)
                .append("/oauth/v2/authorize")
                .append("?")
                .append("client_id=")
                .append(clientId)
                .append("&")
                .append("response_type=code")
                .append("&")
                .append("scope=")
                .append(requiredScopes)
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
        map.add("client_id", clientId);
        map.add("code_verifier", CODE_CHALLENGE_MAP.get("code_verifier"));
        String redirectUrl =
                ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
                        + "/auth/token";
        map.add("redirect_uri", redirectUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> param = new HttpEntity<>(map, headers);

        String tokenUrl = iamServerEndpoint + "/oauth/v2/token";
        try {
            ResponseEntity<TokenResponse> response =
                    restTemplate.postForEntity(tokenUrl, param, TokenResponse.class);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Get access token by code:{} form the IAM error.", code, e);
        }
        return null;
    }
}
