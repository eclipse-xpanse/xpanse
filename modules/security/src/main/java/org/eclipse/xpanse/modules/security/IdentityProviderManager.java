/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.security.common.CurrentUserInfo;
import org.eclipse.xpanse.modules.security.common.CurrentUserInfoHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * The instance to manage active identity provider service.
 */
@Slf4j
@Component
public class IdentityProviderManager {

    @Getter
    private IdentityProviderService activeIdentityProviderService;

    @Resource
    private ApplicationContext applicationContext;

    @Value("${enable.web.security:false}")
    private Boolean webSecurityIsEnabled;

    /**
     * Instantiates active IdentityProviderService.
     */
    @Bean
    public void loadActiveIdentityProviderServices() {
        if (!webSecurityIsEnabled) {
            log.info("Web security is disabled.");
            activeIdentityProviderService = null;
            return;
        }
        List<IdentityProviderService> identityProviderServices =
                applicationContext.getBeansOfType(IdentityProviderService.class)
                        .values().stream().toList();
        if (CollectionUtils.isEmpty(identityProviderServices)) {
            log.info("No active identity providers found.");
            activeIdentityProviderService = null;
        } else {
            if (identityProviderServices.size() > 1) {
                throw new IllegalStateException(
                        "More than one identity provider service is active.");
            }
            activeIdentityProviderService = identityProviderServices.getFirst();
            log.info("Identity provider service with type:{} is active.",
                    activeIdentityProviderService.getIdentityProviderType());
        }
    }

    /**
     * Get current login user info.
     *
     * @return current login user info.
     */
    public CurrentUserInfo getCurrentUserInfo() {
        if (Objects.nonNull(activeIdentityProviderService)) {
            CurrentUserInfo currentUserInfo = activeIdentityProviderService.getCurrentUserInfo();
            CurrentUserInfoHolder.setCurrentUserInfo(currentUserInfo);
            return currentUserInfo;
        }
        return null;
    }


    /**
     * Get current login user id.
     *
     * @return current login user id.
     */
    public Optional<String> getCurrentLoginUserId() {
        CurrentUserInfo currentUserInfo = getCurrentUserInfo();
        if (Objects.nonNull(currentUserInfo)) {
            return Optional.ofNullable(currentUserInfo.getUserId());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Get current login user namespace.
     *
     * @return current login user namespace.
     */
    public Optional<String> getUserNamespace() {
        CurrentUserInfo currentUserInfo = getCurrentUserInfo();
        if (Objects.isNull(currentUserInfo)) {
            return Optional.empty();
        }
        return StringUtils.isBlank(currentUserInfo.getNamespace())
                ? Optional.ofNullable(currentUserInfo.getUserId())
                : Optional.ofNullable(currentUserInfo.getNamespace());
    }

    /**
     * Get csp from metadata fo current login user.
     *
     * @return csp of current login user.
     */
    public Optional<Csp> getCspFromMetadata() {
        CurrentUserInfo currentUserInfo = getCurrentUserInfo();
        if (Objects.nonNull(currentUserInfo) && StringUtils.isNotBlank(currentUserInfo.getCsp())) {
            try {
                Csp csp = Csp.getByValue(currentUserInfo.getCsp());
                return Optional.of(csp);
            } catch (UnsupportedEnumValueException e) {
                log.error("Unsupported csp value:{}", currentUserInfo.getCsp());
            }
        }
        return Optional.empty();
    }
}
