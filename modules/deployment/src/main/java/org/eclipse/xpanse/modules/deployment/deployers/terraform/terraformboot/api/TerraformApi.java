package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.ApiClient;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformAsyncDeployRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformAsyncDestroyRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformBootSystemStatus;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformDeployRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformDestroyRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformResult;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformValidationResult;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-08-29T19:58:56.131485900+08:00[Asia/Shanghai]")
public class TerraformApi {
    private ApiClient apiClient;

    public TerraformApi() {
        this(new ApiClient());
    }

    public TerraformApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 
     * async deploy resources via Terraform
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>400</b> - Bad Request
     * <p><b>202</b> - Accepted
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformAsyncDeployRequest  (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDeploy(String moduleDirectory, TerraformAsyncDeployRequest terraformAsyncDeployRequest) throws RestClientException {
        asyncDeployWithHttpInfo(moduleDirectory, terraformAsyncDeployRequest);
    }

    /**
     * 
     * async deploy resources via Terraform
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>400</b> - Bad Request
     * <p><b>202</b> - Accepted
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformAsyncDeployRequest  (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDeployWithHttpInfo(String moduleDirectory, TerraformAsyncDeployRequest terraformAsyncDeployRequest) throws RestClientException {
        Object localVarPostBody = terraformAsyncDeployRequest;
        
        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'moduleDirectory' when calling asyncDeploy");
        }
        
        // verify the required parameter 'terraformAsyncDeployRequest' is set
        if (terraformAsyncDeployRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformAsyncDeployRequest' when calling asyncDeploy");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
         };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<Void> localReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/terraform-boot/deploy/async/{module_directory}", HttpMethod.POST, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Async destroy the Terraform modules
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>400</b> - Bad Request
     * <p><b>202</b> - Accepted
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformAsyncDestroyRequest  (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDestroy(String moduleDirectory, TerraformAsyncDestroyRequest terraformAsyncDestroyRequest) throws RestClientException {
        asyncDestroyWithHttpInfo(moduleDirectory, terraformAsyncDestroyRequest);
    }

    /**
     * 
     * Async destroy the Terraform modules
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>400</b> - Bad Request
     * <p><b>202</b> - Accepted
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformAsyncDestroyRequest  (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDestroyWithHttpInfo(String moduleDirectory, TerraformAsyncDestroyRequest terraformAsyncDestroyRequest) throws RestClientException {
        Object localVarPostBody = terraformAsyncDestroyRequest;
        
        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'moduleDirectory' when calling asyncDestroy");
        }
        
        // verify the required parameter 'terraformAsyncDestroyRequest' is set
        if (terraformAsyncDestroyRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformAsyncDestroyRequest' when calling asyncDestroy");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
         };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<Void> localReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/terraform-boot/destroy/async/{module_directory}", HttpMethod.DELETE, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Deploy resources via Terraform
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>400</b> - Bad Request
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformDeployRequest  (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult deploy(String moduleDirectory, TerraformDeployRequest terraformDeployRequest) throws RestClientException {
        return deployWithHttpInfo(moduleDirectory, terraformDeployRequest).getBody();
    }

    /**
     * 
     * Deploy resources via Terraform
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>400</b> - Bad Request
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformDeployRequest  (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> deployWithHttpInfo(String moduleDirectory, TerraformDeployRequest terraformDeployRequest) throws RestClientException {
        Object localVarPostBody = terraformDeployRequest;
        
        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'moduleDirectory' when calling deploy");
        }
        
        // verify the required parameter 'terraformDeployRequest' is set
        if (terraformDeployRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformDeployRequest' when calling deploy");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*", "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
         };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<TerraformResult> localReturnType = new ParameterizedTypeReference<TerraformResult>() {};
        return apiClient.invokeAPI("/terraform-boot/deploy/{module_directory}", HttpMethod.POST, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Destroy the Terraform modules
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>400</b> - Bad Request
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformDestroyRequest  (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult destroy(String moduleDirectory, TerraformDestroyRequest terraformDestroyRequest) throws RestClientException {
        return destroyWithHttpInfo(moduleDirectory, terraformDestroyRequest).getBody();
    }

    /**
     * 
     * Destroy the Terraform modules
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>400</b> - Bad Request
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformDestroyRequest  (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> destroyWithHttpInfo(String moduleDirectory, TerraformDestroyRequest terraformDestroyRequest) throws RestClientException {
        Object localVarPostBody = terraformDestroyRequest;
        
        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'moduleDirectory' when calling destroy");
        }
        
        // verify the required parameter 'terraformDestroyRequest' is set
        if (terraformDestroyRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformDestroyRequest' when calling destroy");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*", "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
         };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<TerraformResult> localReturnType = new ParameterizedTypeReference<TerraformResult>() {};
        return apiClient.invokeAPI("/terraform-boot/destroy/{module_directory}", HttpMethod.DELETE, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Check health of Terraform API service
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>400</b> - Bad Request
     * <p><b>200</b> - OK
     * @return TerraformBootSystemStatus
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformBootSystemStatus healthCheck() throws RestClientException {
        return healthCheckWithHttpInfo().getBody();
    }

    /**
     * 
     * Check health of Terraform API service
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>400</b> - Bad Request
     * <p><b>200</b> - OK
     * @return ResponseEntity&lt;TerraformBootSystemStatus&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformBootSystemStatus> healthCheckWithHttpInfo() throws RestClientException {
        Object localVarPostBody = null;
        

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*", "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<TerraformBootSystemStatus> localReturnType = new ParameterizedTypeReference<TerraformBootSystemStatus>() {};
        return apiClient.invokeAPI("/terraform-boot/health", HttpMethod.GET, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Validate the Terraform modules
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>400</b> - Bad Request
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @return TerraformValidationResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformValidationResult validate(String moduleDirectory) throws RestClientException {
        return validateWithHttpInfo(moduleDirectory).getBody();
    }

    /**
     * 
     * Validate the Terraform modules
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>400</b> - Bad Request
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @return ResponseEntity&lt;TerraformValidationResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformValidationResult> validateWithHttpInfo(String moduleDirectory) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'moduleDirectory' when calling validate");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*", "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<TerraformValidationResult> localReturnType = new ParameterizedTypeReference<TerraformValidationResult>() {};
        return apiClient.invokeAPI("/terraform-boot/validate/{module_directory}", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
}
