/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security;

import org.eclipse.xpanse.modules.models.security.TokenResponse;
import org.eclipse.xpanse.modules.models.system.BackendSystemStatus;
import org.eclipse.xpanse.modules.models.system.enums.IdentityProviderType;
import org.eclipse.xpanse.modules.security.common.CurrentUserInfo;

/** This interface describes identity provider service. */
public interface IdentityProviderService {

    /** Get the type of the identity provider service. */
    IdentityProviderType getIdentityProviderType();

    /** Get current user info by the identity provider service. */
    CurrentUserInfo getCurrentUserInfo();

    /** Get health status of the identity provider service. */
    BackendSystemStatus getIdentityProviderStatus();

    /** Get authorization url for user to authenticate. */
    String getAuthorizeUrl();

    /** Get token info by authorization code. */
    TokenResponse getAccessToken(String code);
}
