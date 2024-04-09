/*
 * Terraform-Boot API
 * RESTful Services to interact with Terraform-Boot runtime
 *
 * The version of the OpenAPI document: 1.0.5-SNAPSHOT
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.WebhookConfig;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * TerraformAsyncDestroyFromDirectoryRequest
 */
@JsonPropertyOrder({
  TerraformAsyncDestroyFromDirectoryRequest.JSON_PROPERTY_DEPLOYMENT_SCENARIO,
  TerraformAsyncDestroyFromDirectoryRequest.JSON_PROPERTY_VARIABLES,
  TerraformAsyncDestroyFromDirectoryRequest.JSON_PROPERTY_ENV_VARIABLES,
  TerraformAsyncDestroyFromDirectoryRequest.JSON_PROPERTY_WEBHOOK_CONFIG
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.4.0")
public class TerraformAsyncDestroyFromDirectoryRequest {
  /**
   * The deployment scenario when the Xpanse client send the destroy request. Valid values: deploy,modify,destroy,rollback,purge.
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

  public static final String JSON_PROPERTY_WEBHOOK_CONFIG = "webhookConfig";
  private WebhookConfig webhookConfig;

  public TerraformAsyncDestroyFromDirectoryRequest() {
  }

  public TerraformAsyncDestroyFromDirectoryRequest deploymentScenario(DeploymentScenarioEnum deploymentScenario) {
    
    this.deploymentScenario = deploymentScenario;
    return this;
  }

   /**
   * The deployment scenario when the Xpanse client send the destroy request. Valid values: deploy,modify,destroy,rollback,purge.
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


  public TerraformAsyncDestroyFromDirectoryRequest variables(Map<String, Object> variables) {
    
    this.variables = variables;
    return this;
  }

  public TerraformAsyncDestroyFromDirectoryRequest putVariablesItem(String key, Object variablesItem) {
    this.variables.put(key, variablesItem);
    return this;
  }

   /**
   * Key-value pairs of regular variables that must be used to execute the Terraform request.
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


  public TerraformAsyncDestroyFromDirectoryRequest envVariables(Map<String, String> envVariables) {
    
    this.envVariables = envVariables;
    return this;
  }

  public TerraformAsyncDestroyFromDirectoryRequest putEnvVariablesItem(String key, String envVariablesItem) {
    if (this.envVariables == null) {
      this.envVariables = new HashMap<>();
    }
    this.envVariables.put(key, envVariablesItem);
    return this;
  }

   /**
   * Key-value pairs of variables that must be injected as environment variables to terraform process.
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


  public TerraformAsyncDestroyFromDirectoryRequest webhookConfig(WebhookConfig webhookConfig) {
    
    this.webhookConfig = webhookConfig;
    return this;
  }

   /**
   * Get webhookConfig
   * @return webhookConfig
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_WEBHOOK_CONFIG)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public WebhookConfig getWebhookConfig() {
    return webhookConfig;
  }


  @JsonProperty(JSON_PROPERTY_WEBHOOK_CONFIG)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setWebhookConfig(WebhookConfig webhookConfig) {
    this.webhookConfig = webhookConfig;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TerraformAsyncDestroyFromDirectoryRequest terraformAsyncDestroyFromDirectoryRequest = (TerraformAsyncDestroyFromDirectoryRequest) o;
    return Objects.equals(this.deploymentScenario, terraformAsyncDestroyFromDirectoryRequest.deploymentScenario) &&
        Objects.equals(this.variables, terraformAsyncDestroyFromDirectoryRequest.variables) &&
        Objects.equals(this.envVariables, terraformAsyncDestroyFromDirectoryRequest.envVariables) &&
        Objects.equals(this.webhookConfig, terraformAsyncDestroyFromDirectoryRequest.webhookConfig);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deploymentScenario, variables, envVariables, webhookConfig);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TerraformAsyncDestroyFromDirectoryRequest {\n");
    sb.append("    deploymentScenario: ").append(toIndentedString(deploymentScenario)).append("\n");
    sb.append("    variables: ").append(toIndentedString(variables)).append("\n");
    sb.append("    envVariables: ").append(toIndentedString(envVariables)).append("\n");
    sb.append("    webhookConfig: ").append(toIndentedString(webhookConfig)).append("\n");
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

