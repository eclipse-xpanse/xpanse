/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Error response for the REST API.
 */
@Data
public class Response {

    @NotNull
    private String code;
    @NotNull
    private String message;
    @NotNull
    private Boolean success;

    public static Response errorResponse(ErrCode errCode, String errMsg) {
        Response response = new Response();
        response.success = false;
        response.code = errCode.getCode();
        response.message = errCode.getErrMsg() + ". -- " + errMsg;
        return response;
    }

    public static Response successResponse(String message) {
        Response response = new Response();
        response.success = true;
        response.code = "Success.0000";
        response.message = message;
        return response;
    }

}