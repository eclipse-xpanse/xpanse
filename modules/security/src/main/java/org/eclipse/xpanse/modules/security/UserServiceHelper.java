/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security;

import jakarta.annotation.Resource;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.common.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.modules.models.common.exceptions.UserNotLoggedInException;
import org.eclipse.xpanse.modules.security.common.CurrentUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/** The service helper provider methods to get all info of the current user. */
@Slf4j
@Component
public class UserServiceHelper {

    private static final String NO_AUTH_DEFAULT_USER_ID = "no-auth-user-id";

    @Value("${enable.web.security:true}")
    private Boolean webSecurityIsEnabled;

    @Value("${enable.role.protection:true}")
    private Boolean roleProtectionIsEnabled;

    @Resource private IdentityProviderManager identityProviderManager;

    /**
     * Check if the current user has the role.
     *
     * @param role the role.
     * @return true if the current user has the role, false otherwise.
     */
    public boolean currentUserHasRole(String role) {
        if (!webSecurityIsEnabled) {
            return true;
        }
        if (!roleProtectionIsEnabled) {
            return true;
        }
        CurrentUserInfo currentUserInfo = getCurrentUserInfo();
        return Objects.nonNull(currentUserInfo)
                && !CollectionUtils.isEmpty(currentUserInfo.getRoles())
                && currentUserInfo.getRoles().contains(role);
    }

    /**
     * Check if the current user is the owner user.
     *
     * @param ownerId the owner id.
     * @return true if the current user is the owner user, false otherwise.
     */
    public boolean currentUserIsOwner(String ownerId) {
        if (!webSecurityIsEnabled) {
            return true;
        }
        return StringUtils.equals(ownerId, getCurrentUserId());
    }

    /**
     * Check if the current user can manage the serviceVendor.
     *
     * @param ownerIsv the owner serviceVendor.
     * @return true if the current user has the owner serviceVendor, false otherwise.
     */
    public boolean currentUserCanManageIsv(String ownerIsv) {
        if (!webSecurityIsEnabled) {
            return true;
        }
        return StringUtils.equals(ownerIsv, getCurrentUserManageIsv());
    }

    /**
     * Check if the current user can manage the csp.
     *
     * @param ownerCsp the owner csp.
     * @return true if the current user can manage the csp, false otherwise.
     */
    public boolean currentUserCanManageCsp(Csp ownerCsp) {
        if (!webSecurityIsEnabled) {
            return true;
        }
        return Objects.equals(ownerCsp, getCurrentUserManageCsp());
    }

    /** Get the current user id. */
    public String getCurrentUserId() {
        if (!webSecurityIsEnabled) {
            return NO_AUTH_DEFAULT_USER_ID;
        }
        return getCurrentUserInfo().getUserId();
    }

    /**
     * Get id of current login user.
     *
     * @return current login user id.
     */
    private CurrentUserInfo getCurrentUserInfo() {
        CurrentUserInfo currentUserInfo = identityProviderManager.getCurrentUserInfo();
        if (Objects.isNull(currentUserInfo)) {
            throw new UserNotLoggedInException("Unable to get current login information");
        }
        return currentUserInfo;
    }

    /** Get the serviceVendor managed by the current user . */
    public String getCurrentUserManageIsv() {
        if (!webSecurityIsEnabled) {
            return null;
        }
        CurrentUserInfo userInfo = getCurrentUserInfo();
        if (StringUtils.isNotBlank(userInfo.getIsv())) {
            return userInfo.getIsv();
        }
        throw new AccessDeniedException("Current user's isv is null, please set it first.");
    }

    /** Get the auth enable result. */
    public boolean isAuthEnable() {
        return webSecurityIsEnabled;
    }

    /** Get the csp managed by the current user. */
    public Csp getCurrentUserManageCsp() {
        if (!webSecurityIsEnabled) {
            return null;
        }
        CurrentUserInfo currentUserInfo = getCurrentUserInfo();
        if (StringUtils.isNotBlank(currentUserInfo.getCsp())) {
            try {
                return Csp.getByValue(currentUserInfo.getCsp());
            } catch (UnsupportedEnumValueException e) {
                String errorMsg =
                        "Unsupported csp value: " + currentUserInfo.getCsp() + " of current user.";
                throw new AccessDeniedException(errorMsg);
            }
        }
        String errorMsg = "Current user's csp is null, please set it first.";
        throw new AccessDeniedException(errorMsg);
    }
}
