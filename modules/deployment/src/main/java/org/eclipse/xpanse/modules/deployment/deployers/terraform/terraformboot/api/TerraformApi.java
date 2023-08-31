package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.api;

import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.ApiClient;

import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.Response;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformAsyncDeployFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformAsyncDestroyFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformBootSystemStatus;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformDeployFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformDeployWithScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformDestroyFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformDestroyWithScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformResult;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.model.TerraformValidationResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2023-08-31T14:40:02.713101900+08:00[Asia/Shanghai]")
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
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>202</b> - Accepted
     * @param terraformAsyncDeployFromDirectoryRequest  (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDeployWithScripts(TerraformAsyncDeployFromDirectoryRequest terraformAsyncDeployFromDirectoryRequest) throws RestClientException {
        asyncDeployWithScriptsWithHttpInfo(terraformAsyncDeployFromDirectoryRequest);
    }

    /**
     * 
     * async deploy resources via Terraform
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>202</b> - Accepted
     * @param terraformAsyncDeployFromDirectoryRequest  (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDeployWithScriptsWithHttpInfo(TerraformAsyncDeployFromDirectoryRequest terraformAsyncDeployFromDirectoryRequest) throws RestClientException {
        Object localVarPostBody = terraformAsyncDeployFromDirectoryRequest;
        
        // verify the required parameter 'terraformAsyncDeployFromDirectoryRequest' is set
        if (terraformAsyncDeployFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformAsyncDeployFromDirectoryRequest' when calling asyncDeployWithScripts");
        }
        

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
        return apiClient.invokeAPI("/terraform-boot/deploy/scripts/async", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Async destroy the Terraform modules
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>202</b> - Accepted
     * @param terraformAsyncDestroyFromDirectoryRequest  (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDestroyWithScripts(TerraformAsyncDestroyFromDirectoryRequest terraformAsyncDestroyFromDirectoryRequest) throws RestClientException {
        asyncDestroyWithScriptsWithHttpInfo(terraformAsyncDestroyFromDirectoryRequest);
    }

    /**
     * 
     * Async destroy the Terraform modules
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>202</b> - Accepted
     * @param terraformAsyncDestroyFromDirectoryRequest  (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDestroyWithScriptsWithHttpInfo(TerraformAsyncDestroyFromDirectoryRequest terraformAsyncDestroyFromDirectoryRequest) throws RestClientException {
        Object localVarPostBody = terraformAsyncDestroyFromDirectoryRequest;
        
        // verify the required parameter 'terraformAsyncDestroyFromDirectoryRequest' is set
        if (terraformAsyncDestroyFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformAsyncDestroyFromDirectoryRequest' when calling asyncDestroyWithScripts");
        }
        

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
        return apiClient.invokeAPI("/terraform-boot/destroy/scripts/async", HttpMethod.DELETE, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Deploy resources via Terraform
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformDeployFromDirectoryRequest  (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult deploy(String moduleDirectory, TerraformDeployFromDirectoryRequest terraformDeployFromDirectoryRequest) throws RestClientException {
        return deployWithHttpInfo(moduleDirectory, terraformDeployFromDirectoryRequest).getBody();
    }

    /**
     * 
     * Deploy resources via Terraform
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformDeployFromDirectoryRequest  (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> deployWithHttpInfo(String moduleDirectory, TerraformDeployFromDirectoryRequest terraformDeployFromDirectoryRequest) throws RestClientException {
        Object localVarPostBody = terraformDeployFromDirectoryRequest;
        
        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'moduleDirectory' when calling deploy");
        }
        
        // verify the required parameter 'terraformDeployFromDirectoryRequest' is set
        if (terraformDeployFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformDeployFromDirectoryRequest' when calling deploy");
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
     * Deploy resources via Terraform
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param terraformDeployWithScriptsRequest  (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult deployWithScripts(TerraformDeployWithScriptsRequest terraformDeployWithScriptsRequest) throws RestClientException {
        return deployWithScriptsWithHttpInfo(terraformDeployWithScriptsRequest).getBody();
    }

    /**
     * 
     * Deploy resources via Terraform
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param terraformDeployWithScriptsRequest  (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> deployWithScriptsWithHttpInfo(TerraformDeployWithScriptsRequest terraformDeployWithScriptsRequest) throws RestClientException {
        Object localVarPostBody = terraformDeployWithScriptsRequest;
        
        // verify the required parameter 'terraformDeployWithScriptsRequest' is set
        if (terraformDeployWithScriptsRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformDeployWithScriptsRequest' when calling deployWithScripts");
        }
        

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
        return apiClient.invokeAPI("/terraform-boot/deploy/scripts", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Destroy the Terraform modules
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformDestroyFromDirectoryRequest  (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult destroy(String moduleDirectory, TerraformDestroyFromDirectoryRequest terraformDestroyFromDirectoryRequest) throws RestClientException {
        return destroyWithHttpInfo(moduleDirectory, terraformDestroyFromDirectoryRequest).getBody();
    }

    /**
     * 
     * Destroy the Terraform modules
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformDestroyFromDirectoryRequest  (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> destroyWithHttpInfo(String moduleDirectory, TerraformDestroyFromDirectoryRequest terraformDestroyFromDirectoryRequest) throws RestClientException {
        Object localVarPostBody = terraformDestroyFromDirectoryRequest;
        
        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'moduleDirectory' when calling destroy");
        }
        
        // verify the required parameter 'terraformDestroyFromDirectoryRequest' is set
        if (terraformDestroyFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformDestroyFromDirectoryRequest' when calling destroy");
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
     * Destroy resources via Terraform
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param terraformDestroyWithScriptsRequest  (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult destroyWithScripts(TerraformDestroyWithScriptsRequest terraformDestroyWithScriptsRequest) throws RestClientException {
        return destroyWithScriptsWithHttpInfo(terraformDestroyWithScriptsRequest).getBody();
    }

    /**
     * 
     * Destroy resources via Terraform
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param terraformDestroyWithScriptsRequest  (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> destroyWithScriptsWithHttpInfo(TerraformDestroyWithScriptsRequest terraformDestroyWithScriptsRequest) throws RestClientException {
        Object localVarPostBody = terraformDestroyWithScriptsRequest;
        
        // verify the required parameter 'terraformDestroyWithScriptsRequest' is set
        if (terraformDestroyWithScriptsRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformDestroyWithScriptsRequest' when calling destroyWithScripts");
        }
        

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
        return apiClient.invokeAPI("/terraform-boot/destroy/scripts", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Check health of Terraform API service
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
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
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
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
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
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
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
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
