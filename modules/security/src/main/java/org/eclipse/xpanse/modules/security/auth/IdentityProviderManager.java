/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.auth;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.security.auth.common.CurrentUserInfo;
import org.eclipse.xpanse.modules.security.auth.common.CurrentUserInfoHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** The instance to manage active identity provider service. */
@Slf4j
@Component
public class IdentityProviderManager implements ApplicationListener<ContextRefreshedEvent> {

    @Getter private IdentityProviderService activeIdentityProviderService;

    @Resource private ApplicationContext applicationContext;

    @Value("${enable.web.security:false}")
    private Boolean webSecurityIsEnabled;

    /** Instantiates active IdentityProviderService. */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!webSecurityIsEnabled) {
            log.info("Security is disabled, authentication and authorization are not required.");
            activeIdentityProviderService = null;
            return;
        }
        List<IdentityProviderService> identityProviderServices =
                applicationContext.getBeansOfType(IdentityProviderService.class).values().stream()
                        .toList();
        if (CollectionUtils.isEmpty(identityProviderServices)) {
            String errorMsg = "Security is enabled, but no identity provider service is active.";
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        if (identityProviderServices.size() > 1) {
            throw new IllegalStateException("More than one identity provider service is active.");
        }
        activeIdentityProviderService = identityProviderServices.getFirst();
        log.info(
                "Identity provider service:{} with type:{} is active.",
                activeIdentityProviderService.getClass().getName(),
                activeIdentityProviderService.getIdentityProviderType());
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
}
