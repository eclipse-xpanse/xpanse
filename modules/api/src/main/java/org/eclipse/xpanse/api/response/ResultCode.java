/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.response;

/**
 * Result codes for the REST API.
 */
public enum ResultCode {
    SUCCESS("Success.0000", "success"),
    RUNTIME_ERROR("Common.0001", "Runtime failed"),
    BAD_PARAMETERS("Common.0002", "Parameters invalid");

    private final String code;
    private final String message;

    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Get the result code.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Get the result message.
     */
    public String getMessage() {
        return this.message;
    }
}

