/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Response for the REST API.
 */
@Data
public class Response {

    @NotNull
    private String code;
    @NotNull
    private String message;
    @NotNull
    private Boolean success;

    /**
     * Create error response with errorCode and errorMessage.
     *
     * @param errCode error code
     * @param errMsg  error message
     * @return errorResponse
     */
    public static Response errorResponse(ErrCode errCode, String errMsg) {
        Response response = new Response();
        response.success = false;
        response.code = errCode.getCode();
        response.message = errCode.getErrMsg() + ". -- " + errMsg;
        return response;
    }

    /**
     * Create success response with message.
     *
     * @param message message
     * @return successResponse
     */
    public static Response successResponse(String message) {
        Response response = new Response();
        response.success = true;
        response.code = "Success.0000";
        response.message = message;
        return response;
    }

}