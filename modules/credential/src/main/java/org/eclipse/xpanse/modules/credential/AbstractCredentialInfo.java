/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;

/**
 * The Abstract class which defines the credential basic information required by a cloud provider.
 */
public abstract class AbstractCredentialInfo {

    /**
     * The name of the credential, this field is provided by the plugins.
     */
    @Getter
    String name;

    /**
     * The userId of the credential.
     */
    @Getter
    @Setter
    String userId = "default";

    /**
     * The description of the credential.
     */
    @Getter
    String description;

    /**
     * The type of the credential.
     */
    @Getter
    CredentialType type;

    /**
     * The constructor.
     */
    AbstractCredentialInfo(String name, String description, CredentialType type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }
}
