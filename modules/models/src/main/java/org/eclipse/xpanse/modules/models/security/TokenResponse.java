/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Data model of token response. */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"access_token", "token_type", "expires_in", "id_token", "scopes"})
public class TokenResponse {

    @NotNull
    @Schema(description = "An access_token as a JWT or opaque token.")
    @JsonProperty("access_token")
    private String accessToken;

    @NotNull
    @Schema(description = "Type of the access_token.")
    @JsonProperty("token_type")
    private String tokenType;

    @NotNull
    @Schema(description = "Number of second until the expiration of the access_token")
    @JsonProperty("expires_in")
    private String expiresIn;

    @NotNull
    @Schema(description = "An id_token of the authorized service user")
    @JsonProperty("id_token")
    private String idToken;

    @Schema(description = "Scopes of the access_token.")
    @JsonProperty("scopes")
    private String scopes;
}
