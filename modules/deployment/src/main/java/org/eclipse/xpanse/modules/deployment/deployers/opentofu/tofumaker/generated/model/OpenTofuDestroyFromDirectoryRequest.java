/*
 * Tofu-Maker API
 * RESTful Services to interact with Tofu-Maker runtime
 *
 * The version of the OpenAPI document: 1.0.0-SNAPSHOT
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * OpenTofuDestroyFromDirectoryRequest
 */
@JsonPropertyOrder({
  OpenTofuDestroyFromDirectoryRequest.JSON_PROPERTY_DEPLOYMENT_SCENARIO,
  OpenTofuDestroyFromDirectoryRequest.JSON_PROPERTY_VARIABLES,
  OpenTofuDestroyFromDirectoryRequest.JSON_PROPERTY_ENV_VARIABLES
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.4.0")
public class OpenTofuDestroyFromDirectoryRequest {
  /**
   * This value can be set by the client if they wish to know the type ofrequest for which the callback response is generated from tofu-maker. There will beno difference in the way request is executed. This information is only set in thecallback response again for the client to handle the callback response accordingly.
   */
  public enum DeploymentScenarioEnum {
    DEPLOY("deploy"),
    
    MODIFY("modify"),
    
    DESTROY("destroy"),
    
    ROLLBACK("rollback"),
    
    PURGE("purge");

    private String value;

    DeploymentScenarioEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static DeploymentScenarioEnum fromValue(String value) {
      for (DeploymentScenarioEnum b : DeploymentScenarioEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_DEPLOYMENT_SCENARIO = "deploymentScenario";
  private DeploymentScenarioEnum deploymentScenario;

  public static final String JSON_PROPERTY_VARIABLES = "variables";
  private Map<String, Object> variables = new HashMap<>();

  public static final String JSON_PROPERTY_ENV_VARIABLES = "envVariables";
  private Map<String, String> envVariables = new HashMap<>();

  public OpenTofuDestroyFromDirectoryRequest() {
  }

  public OpenTofuDestroyFromDirectoryRequest deploymentScenario(DeploymentScenarioEnum deploymentScenario) {
    
    this.deploymentScenario = deploymentScenario;
    return this;
  }

   /**
   * This value can be set by the client if they wish to know the type ofrequest for which the callback response is generated from tofu-maker. There will beno difference in the way request is executed. This information is only set in thecallback response again for the client to handle the callback response accordingly.
   * @return deploymentScenario
  **/
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_DEPLOYMENT_SCENARIO)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public DeploymentScenarioEnum getDeploymentScenario() {
    return deploymentScenario;
  }


  @JsonProperty(JSON_PROPERTY_DEPLOYMENT_SCENARIO)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDeploymentScenario(DeploymentScenarioEnum deploymentScenario) {
    this.deploymentScenario = deploymentScenario;
  }


  public OpenTofuDestroyFromDirectoryRequest variables(Map<String, Object> variables) {
    
    this.variables = variables;
    return this;
  }

  public OpenTofuDestroyFromDirectoryRequest putVariablesItem(String key, Object variablesItem) {
    this.variables.put(key, variablesItem);
    return this;
  }

   /**
   * Key-value pairs of regular variables that must be used to execute the OpenTofu request.
   * @return variables
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_VARIABLES)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public Map<String, Object> getVariables() {
    return variables;
  }


  @JsonProperty(JSON_PROPERTY_VARIABLES)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setVariables(Map<String, Object> variables) {
    this.variables = variables;
  }


  public OpenTofuDestroyFromDirectoryRequest envVariables(Map<String, String> envVariables) {
    
    this.envVariables = envVariables;
    return this;
  }

  public OpenTofuDestroyFromDirectoryRequest putEnvVariablesItem(String key, String envVariablesItem) {
    if (this.envVariables == null) {
      this.envVariables = new HashMap<>();
    }
    this.envVariables.put(key, envVariablesItem);
    return this;
  }

   /**
   * Key-value pairs of variables that must be injected as environment variables to OpenTofu process.
   * @return envVariables
  **/
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ENV_VARIABLES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public Map<String, String> getEnvVariables() {
    return envVariables;
  }


  @JsonProperty(JSON_PROPERTY_ENV_VARIABLES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setEnvVariables(Map<String, String> envVariables) {
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
    OpenTofuDestroyFromDirectoryRequest openTofuDestroyFromDirectoryRequest = (OpenTofuDestroyFromDirectoryRequest) o;
    return Objects.equals(this.deploymentScenario, openTofuDestroyFromDirectoryRequest.deploymentScenario) &&
        Objects.equals(this.variables, openTofuDestroyFromDirectoryRequest.variables) &&
        Objects.equals(this.envVariables, openTofuDestroyFromDirectoryRequest.envVariables);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deploymentScenario, variables, envVariables);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OpenTofuDestroyFromDirectoryRequest {\n");
    sb.append("    deploymentScenario: ").append(toIndentedString(deploymentScenario)).append("\n");
    sb.append("    variables: ").append(toIndentedString(variables)).append("\n");
    sb.append("    envVariables: ").append(toIndentedString(envVariables)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

