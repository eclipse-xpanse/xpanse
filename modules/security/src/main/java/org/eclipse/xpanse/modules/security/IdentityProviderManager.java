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
import org.eclipse.xpanse.modules.models.security.model.CurrentUserInfo;
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

    /**
     * Instantiates active IdentityProviderService.
     */
    @Bean
    public void loadActiveIdentityProviderServices() {
        List<IdentityProviderService> identityProviderServices =
                applicationContext.getBeansOfType(IdentityProviderService.class)
                        .values().stream().toList();
        if (CollectionUtils.isEmpty(identityProviderServices)) {
            log.error("No active identity providers found.");
            activeIdentityProviderService = null;
        } else {
            if (identityProviderServices.size() > 1) {
                throw new IllegalStateException(
                        "More than one identity provider service is active.");
            }
            activeIdentityProviderService = identityProviderServices.get(0);
            log.error("Identity provider service with type:{} is active.",
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
            return activeIdentityProviderService.getCurrentUserInfo();
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

}
