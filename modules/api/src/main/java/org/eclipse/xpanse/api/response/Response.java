/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * Response for the REST API.
 */
@Data
@JsonInclude(Include.NON_NULL)
public class Response {

    @NotNull
    @Schema(description = "The result code of response.")
    private ResultType resultType;
    @NotNull
    @Schema(description = "Details of the errors occurred")
    private List<String> details;
    @NotNull
    @Schema(description = "Describes if the request is successful")
    private Boolean success;

    /**
     * Create error response with resultCode and errorMessage.
     *
     * @param resultCode error result code
     * @param errMsg     error message
     * @return errorResponse
     */
    public static Response errorResponse(ResultType resultCode, List<String> errMsg) {
        Response response = new Response();
        response.success = false;
        response.resultType = resultCode;
        response.details =  errMsg;
        return response;
    }

    /**
     * Create success response with success message.
     *
     * @param successMsg success message
     * @return successResponse
     */
    public static Response successResponse(List<String> successMsg) {
        Response response = new Response();
        response.success = true;
        response.resultType = ResultType.SUCCESS;
        response.details =  successMsg;
        return response;
    }

}