/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security;

import jakarta.annotation.Resource;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.models.security.model.CurrentUserInfo;
import org.eclipse.xpanse.modules.models.security.model.TokenResponse;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.IdentityProviderType;
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
            log.info("Not found any identity provider service is active.");
            activeIdentityProviderService = new IdentityProviderService() {
                @Override
                public IdentityProviderType getIdentityProviderType() {
                    return null;
                }

                @Override
                public CurrentUserInfo getCurrentUserInfo() {
                    return null;
                }

                @Override
                public BackendSystemStatus getIdentityProviderStatus() {
                    return null;
                }

                @Override
                public String getAuthorizeUrl() {
                    return null;
                }

                @Override
                public TokenResponse getAccessToken(String code) {
                    return null;
                }
            };
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
        return activeIdentityProviderService.getCurrentUserInfo();
    }

}
