/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Specific Response when request the service order failed.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(Include.NON_NULL)
public class OrderFailedResponse extends Response {

    @Schema(description = "The service id associated with the request.")
    private String serviceId;

    @Schema(description = "The order id associated with the request.")
    private String orderId;

    private OrderFailedResponse() {
        super();
    }

    /**
     * Create error response with resultCode and errorMessage.
     *
     * @param resultCode error result code
     * @param errMsg     error message
     * @return errorResponse
     */
    public static OrderFailedResponse errorResponse(ResultType resultCode, List<String> errMsg) {
        OrderFailedResponse response = new OrderFailedResponse();
        response.setSuccess(false);
        response.setResultType(resultCode);
        response.setDetails(errMsg);
        return response;
    }
}