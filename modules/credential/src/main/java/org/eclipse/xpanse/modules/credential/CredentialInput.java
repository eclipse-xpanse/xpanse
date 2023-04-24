/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.credential;

import java.util.List;

/**
 * The credentialInput is kind of credential needs get values from the input.
 */
public class CredentialInput extends Credential {

    /**
     * The variables list for the credentialInput.
     */
    List<InputVariable> inputs;
}
