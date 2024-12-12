/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/** Response for the REST API. */
@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {

    @NotNull
    @Schema(description = "The result code of response.")
    private ErrorType errorType;

    @NotNull
    @NotEmpty
    @Schema(description = "Details of the errors occurred")
    private List<String> details;

    public ErrorResponse() {}

    /**
     * Create error response with resultCode and errorMessage.
     *
     * @param resultCode error result code
     * @param errMsg error message
     * @return errorResponse
     */
    public static ErrorResponse errorResponse(ErrorType resultCode, List<String> errMsg) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.errorType = resultCode;
        errorResponse.details = errMsg;
        return errorResponse;
    }
}
