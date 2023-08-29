/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.zitadel.config;

/**
 * Constants for Zitadel Oauth2 Authorization.
 */
public class ZitadelOauth2Constant {

    /**
     * Auth token type: JWT.
     */
    public static final String AUTH_TYPE_JWT = "JWT";

    /**
     * Auth token type: OpaqueToken.
     */
    public static final String AUTH_TYPE_TOKEN = "OpaqueToken";

    /**
     * Default role granted to user without any roles.
     */
    public static final String DEFAULT_ROLE = "user";

    /**
     * Mandatory scope to request the profile of the user.
     */
    public static final String OPENID_SCOPE = "openid";

    /**
     * Optional scope to request putting the email of the user into the claims.
     */
    public static final String PROFILE_SCOPE = "profile";

    /**
     * Optional scope to request putting the granted roles of the user into the claims.
     */
    public static final String GRANTED_ROLES_SCOPE = "urn:zitadel:iam:org:project:roles";

    /**
     * Optional scope to request putting the metadata of the user into the claims.
     */
    public static final String METADATA_SCOPE = "urn:zitadel:iam:user:metadata";

    /**
     * All scopes to request putting all required info of the user into the claims.
     */
    public static final String REQUIRED_SCOPES =
            "openid profile urn:zitadel:iam:org:projects:roles";

    /**
     * Key in claims for getting the id of the user.
     */
    public static final String USERID_KEY = "sub";

    /**
     * Key in claims for getting the login account of the user.
     */
    public static final String LOGIN_ACCOUNT_KEY = "preferred_username";

    /**
     * Key in claims for getting the name of the user.
     */
    public static final String USERNAME_KEY = "name";

    /**
     * Key in claims for getting the granted roles of the user.
     */
    public static final String GRANTED_ROLES_KEY = "urn:zitadel:iam:org:project:roles";

    /**
     * Key in claims for getting the metadata of the user.
     */
    public static final String METADATA_KEY = "urn:zitadel:iam:user:metadata";

    /**
     * Key in claims for getting the phone of the user.
     */
    public static final String PHONE_KEY = "phone";

    /**
     * Key in claims for getting the email of the user.
     */
    public static final String EMAIL_KEY = "email";

    /**
     * Key in claims for getting the address of the user.
     */
    public static final String ADDRESS_KEY = "address";

}
