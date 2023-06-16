/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.credential.exceptions;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Exception thrown when check credential completeness.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("UnnecessarilyFullyQualified")
public class CredentialVariablesNotComplete extends RuntimeException {

    private Set<String> errorReasons;

    public CredentialVariablesNotComplete(Set<String> errorReasons) {
        super(String.format("Credential Variables Not Complete. Error reasons: %s", errorReasons));
        this.errorReasons = errorReasons;
    }


}
