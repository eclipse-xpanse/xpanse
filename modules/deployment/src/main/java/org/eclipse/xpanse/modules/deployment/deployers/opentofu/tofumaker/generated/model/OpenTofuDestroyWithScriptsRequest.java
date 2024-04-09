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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * OpenTofuDestroyWithScriptsRequest
 */
@JsonPropertyOrder({
  OpenTofuDestroyWithScriptsRequest.JSON_PROPERTY_DEPLOYMENT_SCENARIO,
  OpenTofuDestroyWithScriptsRequest.JSON_PROPERTY_VARIABLES,
  OpenTofuDestroyWithScriptsRequest.JSON_PROPERTY_ENV_VARIABLES,
  OpenTofuDestroyWithScriptsRequest.JSON_PROPERTY_SCRIPTS,
  OpenTofuDestroyWithScriptsRequest.JSON_PROPERTY_TF_STATE
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.4.0")
public class OpenTofuDestroyWithScriptsRequest {
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

  public static final String JSON_PROPERTY_SCRIPTS = "scripts";
  private List<String> scripts = new ArrayList<>();

  public static final String JSON_PROPERTY_TF_STATE = "tfState";
  private String tfState;

  public OpenTofuDestroyWithScriptsRequest() {
  }

  public OpenTofuDestroyWithScriptsRequest deploymentScenario(DeploymentScenarioEnum deploymentScenario) {
    
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


  public OpenTofuDestroyWithScriptsRequest variables(Map<String, Object> variables) {
    
    this.variables = variables;
    return this;
  }

  public OpenTofuDestroyWithScriptsRequest putVariablesItem(String key, Object variablesItem) {
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


  public OpenTofuDestroyWithScriptsRequest envVariables(Map<String, String> envVariables) {
    
    this.envVariables = envVariables;
    return this;
  }

  public OpenTofuDestroyWithScriptsRequest putEnvVariablesItem(String key, String envVariablesItem) {
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


  public OpenTofuDestroyWithScriptsRequest scripts(List<String> scripts) {
    
    this.scripts = scripts;
    return this;
  }

  public OpenTofuDestroyWithScriptsRequest addScriptsItem(String scriptsItem) {
    if (this.scripts == null) {
      this.scripts = new ArrayList<>();
    }
    this.scripts.add(scriptsItem);
    return this;
  }

   /**
   * List of script files for destroy requests deployed via scripts
   * @return scripts
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SCRIPTS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public List<String> getScripts() {
    return scripts;
  }


  @JsonProperty(JSON_PROPERTY_SCRIPTS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setScripts(List<String> scripts) {
    this.scripts = scripts;
  }


  public OpenTofuDestroyWithScriptsRequest tfState(String tfState) {
    
    this.tfState = tfState;
    return this;
  }

   /**
   * The .tfState file content after deployment
   * @return tfState
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_TF_STATE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getTfState() {
    return tfState;
  }


  @JsonProperty(JSON_PROPERTY_TF_STATE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setTfState(String tfState) {
    this.tfState = tfState;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OpenTofuDestroyWithScriptsRequest openTofuDestroyWithScriptsRequest = (OpenTofuDestroyWithScriptsRequest) o;
    return Objects.equals(this.deploymentScenario, openTofuDestroyWithScriptsRequest.deploymentScenario) &&
        Objects.equals(this.variables, openTofuDestroyWithScriptsRequest.variables) &&
        Objects.equals(this.envVariables, openTofuDestroyWithScriptsRequest.envVariables) &&
        Objects.equals(this.scripts, openTofuDestroyWithScriptsRequest.scripts) &&
        Objects.equals(this.tfState, openTofuDestroyWithScriptsRequest.tfState);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deploymentScenario, variables, envVariables, scripts, tfState);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OpenTofuDestroyWithScriptsRequest {\n");
    sb.append("    deploymentScenario: ").append(toIndentedString(deploymentScenario)).append("\n");
    sb.append("    variables: ").append(toIndentedString(variables)).append("\n");
    sb.append("    envVariables: ").append(toIndentedString(envVariables)).append("\n");
    sb.append("    scripts: ").append(toIndentedString(scripts)).append("\n");
    sb.append("    tfState: ").append(toIndentedString(tfState)).append("\n");
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

