/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential;

import org.eclipse.xpanse.modules.credential.enums.CredentialType;

/**
 * The Abstract class which defines the credential basic information required by a cloud provider.
 */
public abstract class AbstractCredentialInfo {

    /**
     * The name of the credential, this field is provided by the plugins.
     */
    String name;

    /**
     * The userId of the credential.
     */
    String userId = "default";

    /**
     * The description of the credential.
     */
    String description;

    /**
     * The type of the credential.
     */
    CredentialType type;
}
