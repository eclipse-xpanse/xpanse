/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.auth;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.security.auth.common.CurrentUserInfo;
import org.eclipse.xpanse.modules.security.auth.common.CurrentUserInfoHolder;
import org.eclipse.xpanse.modules.security.config.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** The instance to manage active identity provider service. */
@Slf4j
@RefreshScope
@Component
public class IdentityProviderManager implements ApplicationListener<ContextRefreshedEvent> {

    @Getter private IdentityProviderService activeIdentityProviderService;

    private final ApplicationContext applicationContext;

    private final SecurityProperties securityProperties;

    /** Constructor for IdentityProviderManager. */
    @Autowired
    public IdentityProviderManager(
            @Nullable IdentityProviderService activeIdentityProviderService,
            ApplicationContext applicationContext,
            SecurityProperties securityProperties) {
        this.activeIdentityProviderService = activeIdentityProviderService;
        this.applicationContext = applicationContext;
        this.securityProperties = securityProperties;
    }

    /** Instantiates active IdentityProviderService. */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!securityProperties.isEnableWebSecurity()) {
            log.info("Security is disabled, authentication and authorization are not required.");
            activeIdentityProviderService = null;
            return;
        }
        // with RefreshScope, there is also a proxy bean which is of the same type.
        // Hence, it's necessary to filter and take onyl the actual bean which is used by other
        // beans.
        List<IdentityProviderService> identityProviderServices =
                applicationContext.getBeansOfType(IdentityProviderService.class).entrySet().stream()
                        .filter(e -> !e.getKey().startsWith("scopedTarget."))
                        .map(Map.Entry::getValue)
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
