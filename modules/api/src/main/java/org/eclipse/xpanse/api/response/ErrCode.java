/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.response;

/**
 * Error codes for the REST API.
 */
public enum ErrCode {
    RUNTIME_ERROR("Common.0001", "Runtime failed"),
    BAD_PARAMETERS("Common.0002", "Parameters invalid");

    private final String code;
    private final String errMsg;

    ErrCode(String code, String errMsg) {
        this.code = code;
        this.errMsg = errMsg;
    }

    /**
     * Get the error code.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Get the error message.
     */
    public String getErrMsg() {
        return this.errMsg;
    }
}

