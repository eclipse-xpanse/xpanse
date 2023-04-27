/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential;

import lombok.Getter;
import lombok.Setter;

/**
 * The class object for the CredentialVariable.
 */
public class CredentialVariable {

    /**
     * The name of the CredentialVariable.
     */
    @Getter
    String name;

    /**
     * The description of the CredentialVariable.
     */
    @Getter
    String description;

    /**
     * The value of the CredentialVariable.
     */
    @Getter
    @Setter
    String value;

    /**
     * The constructor.
     */
    public CredentialVariable(String name, String description) {
        this.name = name;
        this.description = description;
    }

}
