/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential;

import java.util.List;

/**
 * List credential definition that can be provided from end user.
 */
public class CredentialDefinition extends AbstractCredentialInfo {

    /**
     * The variables list for the CredentialDefinition.
     */
    List<CredentialVariable> variables;
}
