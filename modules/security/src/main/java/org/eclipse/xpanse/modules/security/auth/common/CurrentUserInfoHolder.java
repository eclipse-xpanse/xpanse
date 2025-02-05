/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.auth.common;

import java.util.Objects;

/** User information holder. */
public class CurrentUserInfoHolder {

    private static final InheritableThreadLocal<CurrentUserInfo> USERINFO_CACHE =
            new InheritableThreadLocal<>();

    public static CurrentUserInfo getCurrentUserInfo() {
        return USERINFO_CACHE.get();
    }

    public static void setCurrentUserInfo(CurrentUserInfo currentUserInfo) {
        USERINFO_CACHE.set(currentUserInfo);
    }

    public static void clear() {
        USERINFO_CACHE.remove();
    }

    public static String getToken() {
        CurrentUserInfo currentUserInfo = getCurrentUserInfo();
        return Objects.nonNull(currentUserInfo) ? currentUserInfo.getToken() : null;
    }
}
