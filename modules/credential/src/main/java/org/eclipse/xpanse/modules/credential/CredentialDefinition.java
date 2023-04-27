/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential;

import java.util.List;
import lombok.Getter;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;

/**
 * List credential definition that can be provided from end user.
 */
public class CredentialDefinition extends AbstractCredentialInfo {

    /**
     * The variables list for the CredentialDefinition.
     */
    @Getter
    List<CredentialVariable> variables;

    /**
     * The constructor.
     */
    public CredentialDefinition(String name, String description, CredentialType type,
            List<CredentialVariable> variables) {
        super(name, description, type);
        this.variables = variables;
    }
}
