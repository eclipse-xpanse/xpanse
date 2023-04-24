/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential;

import org.eclipse.xpanse.modules.credential.enums.CredentialType;

/**
 * The credential object.
 */
public class Credential {

    /**
     * The name of the credential, this field is provided by the plugins.
     */
    String name;

    /**
     * The userId of the credential.
     */
    String userId;

    /**
     * The type of the credential.
     */
    CredentialType type;
}
