/*
 * Tofu-Maker API
 * RESTful Services to interact with Tofu-Maker runtime
 *
 * The version of the OpenAPI document: 1.0.11-SNAPSHOT
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** OpenTofuDeployFromDirectoryRequest */
@JsonPropertyOrder({
    OpenTofuDeployFromDirectoryRequest.JSON_PROPERTY_REQUEST_ID,
    OpenTofuDeployFromDirectoryRequest.JSON_PROPERTY_OPEN_TOFU_VERSION,
    OpenTofuDeployFromDirectoryRequest.JSON_PROPERTY_IS_PLAN_ONLY,
    OpenTofuDeployFromDirectoryRequest.JSON_PROPERTY_VARIABLES,
    OpenTofuDeployFromDirectoryRequest.JSON_PROPERTY_ENV_VARIABLES
})
@jakarta.annotation.Generated(
        value = "org.openapitools.codegen.languages.JavaClientCodegen",
        comments = "Generator version: 7.10.0")
public class OpenTofuDeployFromDirectoryRequest {
    public static final String JSON_PROPERTY_REQUEST_ID = "requestId";
    @jakarta.annotation.Nullable private UUID requestId;

    public static final String JSON_PROPERTY_OPEN_TOFU_VERSION = "openTofuVersion";
    @jakarta.annotation.Nonnull private String openTofuVersion;

    public static final String JSON_PROPERTY_IS_PLAN_ONLY = "isPlanOnly";
    @jakarta.annotation.Nonnull private Boolean isPlanOnly;

    public static final String JSON_PROPERTY_VARIABLES = "variables";
    @jakarta.annotation.Nonnull private Map<String, Object> variables = new HashMap<>();

    public static final String JSON_PROPERTY_ENV_VARIABLES = "envVariables";
    @jakarta.annotation.Nullable private Map<String, String> envVariables = new HashMap<>();

    public OpenTofuDeployFromDirectoryRequest() {}

    public OpenTofuDeployFromDirectoryRequest requestId(
            @jakarta.annotation.Nullable UUID requestId) {

        this.requestId = requestId;
        return this;
    }

    /**
     * Id of the request.
     *
     * @return requestId
     */
    @jakarta.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_REQUEST_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getRequestId() {
        return requestId;
    }

    @JsonProperty(JSON_PROPERTY_REQUEST_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRequestId(@jakarta.annotation.Nullable UUID requestId) {
        this.requestId = requestId;
    }

    public OpenTofuDeployFromDirectoryRequest openTofuVersion(
            @jakarta.annotation.Nonnull String openTofuVersion) {

        this.openTofuVersion = openTofuVersion;
        return this;
    }

    /**
     * The required version of the OpenTofu which will execute the scripts.
     *
     * @return openTofuVersion
     */
    @jakarta.annotation.Nonnull
    @JsonProperty(JSON_PROPERTY_OPEN_TOFU_VERSION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getOpenTofuVersion() {
        return openTofuVersion;
    }

    @JsonProperty(JSON_PROPERTY_OPEN_TOFU_VERSION)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setOpenTofuVersion(@jakarta.annotation.Nonnull String openTofuVersion) {
        this.openTofuVersion = openTofuVersion;
    }

    public OpenTofuDeployFromDirectoryRequest isPlanOnly(
            @jakarta.annotation.Nonnull Boolean isPlanOnly) {

        this.isPlanOnly = isPlanOnly;
        return this;
    }

    /**
     * Flag to control if the deployment must only generate the OpenTofu or it must also apply the
     * changes.
     *
     * @return isPlanOnly
     */
    @jakarta.annotation.Nonnull
    @JsonProperty(JSON_PROPERTY_IS_PLAN_ONLY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public Boolean getIsPlanOnly() {
        return isPlanOnly;
    }

    @JsonProperty(JSON_PROPERTY_IS_PLAN_ONLY)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setIsPlanOnly(@jakarta.annotation.Nonnull Boolean isPlanOnly) {
        this.isPlanOnly = isPlanOnly;
    }

    public OpenTofuDeployFromDirectoryRequest variables(
            @jakarta.annotation.Nonnull Map<String, Object> variables) {

        this.variables = variables;
        return this;
    }

    public OpenTofuDeployFromDirectoryRequest putVariablesItem(String key, Object variablesItem) {
        this.variables.put(key, variablesItem);
        return this;
    }

    /**
     * Key-value pairs of variables that must be used to execute the OpenTofu request.
     *
     * @return variables
     */
    @jakarta.annotation.Nonnull
    @JsonProperty(JSON_PROPERTY_VARIABLES)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public Map<String, Object> getVariables() {
        return variables;
    }

    @JsonProperty(JSON_PROPERTY_VARIABLES)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setVariables(@jakarta.annotation.Nonnull Map<String, Object> variables) {
        this.variables = variables;
    }

    public OpenTofuDeployFromDirectoryRequest envVariables(
            @jakarta.annotation.Nullable Map<String, String> envVariables) {

        this.envVariables = envVariables;
        return this;
    }

    public OpenTofuDeployFromDirectoryRequest putEnvVariablesItem(
            String key, String envVariablesItem) {
        if (this.envVariables == null) {
            this.envVariables = new HashMap<>();
        }
        this.envVariables.put(key, envVariablesItem);
        return this;
    }

    /**
     * Key-value pairs of variables that must be injected as environment variables to OpenTofu
     * process.
     *
     * @return envVariables
     */
    @jakarta.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_ENV_VARIABLES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getEnvVariables() {
        return envVariables;
    }

    @JsonProperty(JSON_PROPERTY_ENV_VARIABLES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnvVariables(@jakarta.annotation.Nullable Map<String, String> envVariables) {
        this.envVariables = envVariables;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenTofuDeployFromDirectoryRequest openTofuDeployFromDirectoryRequest =
                (OpenTofuDeployFromDirectoryRequest) o;
        return Objects.equals(this.requestId, openTofuDeployFromDirectoryRequest.requestId)
                && Objects.equals(
                        this.openTofuVersion, openTofuDeployFromDirectoryRequest.openTofuVersion)
                && Objects.equals(this.isPlanOnly, openTofuDeployFromDirectoryRequest.isPlanOnly)
                && Objects.equals(this.variables, openTofuDeployFromDirectoryRequest.variables)
                && Objects.equals(
                        this.envVariables, openTofuDeployFromDirectoryRequest.envVariables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, openTofuVersion, isPlanOnly, variables, envVariables);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OpenTofuDeployFromDirectoryRequest {\n");
        sb.append("    requestId: ").append(toIndentedString(requestId)).append("\n");
        sb.append("    openTofuVersion: ").append(toIndentedString(openTofuVersion)).append("\n");
        sb.append("    isPlanOnly: ").append(toIndentedString(isPlanOnly)).append("\n");
        sb.append("    variables: ").append(toIndentedString(variables)).append("\n");
        sb.append("    envVariables: ").append(toIndentedString(envVariables)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first
     * line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
