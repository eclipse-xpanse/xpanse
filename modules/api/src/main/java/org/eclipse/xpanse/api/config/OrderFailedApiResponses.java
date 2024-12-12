/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.api.config;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.eclipse.xpanse.modules.models.response.OrderFailedErrorResponse;

/** Annotation for OrderFailedApiResponses. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
    @ApiResponse(
            responseCode = "400",
            content = @Content(schema = @Schema(implementation = OrderFailedErrorResponse.class))),
    @ApiResponse(
            responseCode = "401",
            content = @Content(schema = @Schema(implementation = OrderFailedErrorResponse.class))),
    @ApiResponse(
            responseCode = "403",
            content = @Content(schema = @Schema(implementation = OrderFailedErrorResponse.class))),
    @ApiResponse(
            responseCode = "408",
            content = @Content(schema = @Schema(implementation = OrderFailedErrorResponse.class))),
    @ApiResponse(
            responseCode = "422",
            content = @Content(schema = @Schema(implementation = OrderFailedErrorResponse.class))),
    @ApiResponse(
            responseCode = "500",
            content = @Content(schema = @Schema(implementation = OrderFailedErrorResponse.class))),
    @ApiResponse(
            responseCode = "502",
            content = @Content(schema = @Schema(implementation = OrderFailedErrorResponse.class)))
})
public @interface OrderFailedApiResponses {}
