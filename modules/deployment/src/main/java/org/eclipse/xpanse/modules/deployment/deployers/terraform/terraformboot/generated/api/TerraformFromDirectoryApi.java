package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api;

import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.ApiClient;

import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.Response;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncDeployFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncDestroyFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformDeployFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformDestroyFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformPlan;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformPlanFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformResult;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformValidationResult;
import java.util.UUID;

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

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
@Component("org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromDirectoryApi")
public class TerraformFromDirectoryApi {
    private ApiClient apiClient;

    public TerraformFromDirectoryApi() {
        this(new ApiClient());
    }

    @Autowired
    public TerraformFromDirectoryApi(ApiClient apiClient) {
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
     * async deploy resources via Terraform from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformAsyncDeployFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDeployFromDirectory(String moduleDirectory, TerraformAsyncDeployFromDirectoryRequest terraformAsyncDeployFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        asyncDeployFromDirectoryWithHttpInfo(moduleDirectory, terraformAsyncDeployFromDirectoryRequest, xCustomRequestId);
    }

    /**
     * 
     * async deploy resources via Terraform from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformAsyncDeployFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDeployFromDirectoryWithHttpInfo(String moduleDirectory, TerraformAsyncDeployFromDirectoryRequest terraformAsyncDeployFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = terraformAsyncDeployFromDirectoryRequest;
        
        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'moduleDirectory' when calling asyncDeployFromDirectory");
        }
        
        // verify the required parameter 'terraformAsyncDeployFromDirectoryRequest' is set
        if (terraformAsyncDeployFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformAsyncDeployFromDirectoryRequest' when calling asyncDeployFromDirectory");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        if (xCustomRequestId != null)
        localVarHeaderParams.add("X-Custom-RequestId", apiClient.parameterToString(xCustomRequestId));

        final String[] localVarAccepts = { 
            "*/*"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
         };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "OAuth2Flow" };

        ParameterizedTypeReference<Void> localReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/terraform-boot/directory/deploy/async/{module_directory}", HttpMethod.POST, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * async destroy resources via Terraform from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformAsyncDestroyFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDestroyFromDirectory(String moduleDirectory, TerraformAsyncDestroyFromDirectoryRequest terraformAsyncDestroyFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        asyncDestroyFromDirectoryWithHttpInfo(moduleDirectory, terraformAsyncDestroyFromDirectoryRequest, xCustomRequestId);
    }

    /**
     * 
     * async destroy resources via Terraform from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformAsyncDestroyFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDestroyFromDirectoryWithHttpInfo(String moduleDirectory, TerraformAsyncDestroyFromDirectoryRequest terraformAsyncDestroyFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = terraformAsyncDestroyFromDirectoryRequest;
        
        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'moduleDirectory' when calling asyncDestroyFromDirectory");
        }
        
        // verify the required parameter 'terraformAsyncDestroyFromDirectoryRequest' is set
        if (terraformAsyncDestroyFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformAsyncDestroyFromDirectoryRequest' when calling asyncDestroyFromDirectory");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        if (xCustomRequestId != null)
        localVarHeaderParams.add("X-Custom-RequestId", apiClient.parameterToString(xCustomRequestId));

        final String[] localVarAccepts = { 
            "*/*"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
         };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "OAuth2Flow" };

        ParameterizedTypeReference<Void> localReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/terraform-boot/directory/destroy/async/{module_directory}", HttpMethod.DELETE, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Deploy resources via Terraform from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformDeployFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult deploy(String moduleDirectory, TerraformDeployFromDirectoryRequest terraformDeployFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        return deployWithHttpInfo(moduleDirectory, terraformDeployFromDirectoryRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Deploy resources via Terraform from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformDeployFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> deployWithHttpInfo(String moduleDirectory, TerraformDeployFromDirectoryRequest terraformDeployFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
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

        if (xCustomRequestId != null)
        localVarHeaderParams.add("X-Custom-RequestId", apiClient.parameterToString(xCustomRequestId));

        final String[] localVarAccepts = { 
            "*/*", "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
         };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "OAuth2Flow" };

        ParameterizedTypeReference<TerraformResult> localReturnType = new ParameterizedTypeReference<TerraformResult>() {};
        return apiClient.invokeAPI("/terraform-boot/directory/deploy/{module_directory}", HttpMethod.POST, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Destroy the resources from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformDestroyFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult destroy(String moduleDirectory, TerraformDestroyFromDirectoryRequest terraformDestroyFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        return destroyWithHttpInfo(moduleDirectory, terraformDestroyFromDirectoryRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Destroy the resources from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformDestroyFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> destroyWithHttpInfo(String moduleDirectory, TerraformDestroyFromDirectoryRequest terraformDestroyFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
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

        if (xCustomRequestId != null)
        localVarHeaderParams.add("X-Custom-RequestId", apiClient.parameterToString(xCustomRequestId));

        final String[] localVarAccepts = { 
            "*/*", "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
         };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "OAuth2Flow" };

        ParameterizedTypeReference<TerraformResult> localReturnType = new ParameterizedTypeReference<TerraformResult>() {};
        return apiClient.invokeAPI("/terraform-boot/directory/destroy/{module_directory}", HttpMethod.DELETE, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Get Terraform Plan as JSON string from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformPlanFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return TerraformPlan
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformPlan plan(String moduleDirectory, TerraformPlanFromDirectoryRequest terraformPlanFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        return planWithHttpInfo(moduleDirectory, terraformPlanFromDirectoryRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Get Terraform Plan as JSON string from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param terraformPlanFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;TerraformPlan&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformPlan> planWithHttpInfo(String moduleDirectory, TerraformPlanFromDirectoryRequest terraformPlanFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = terraformPlanFromDirectoryRequest;
        
        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'moduleDirectory' when calling plan");
        }
        
        // verify the required parameter 'terraformPlanFromDirectoryRequest' is set
        if (terraformPlanFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformPlanFromDirectoryRequest' when calling plan");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        if (xCustomRequestId != null)
        localVarHeaderParams.add("X-Custom-RequestId", apiClient.parameterToString(xCustomRequestId));

        final String[] localVarAccepts = { 
            "*/*", "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
         };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "OAuth2Flow" };

        ParameterizedTypeReference<TerraformPlan> localReturnType = new ParameterizedTypeReference<TerraformPlan>() {};
        return apiClient.invokeAPI("/terraform-boot/directory/plan/{module_directory}", HttpMethod.POST, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Validate the Terraform modules in the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param xCustomRequestId  (optional)
     * @return TerraformValidationResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformValidationResult validate(String moduleDirectory, UUID xCustomRequestId) throws RestClientException {
        return validateWithHttpInfo(moduleDirectory, xCustomRequestId).getBody();
    }

    /**
     * 
     * Validate the Terraform modules in the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param moduleDirectory directory name where the Terraform module files exist. (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;TerraformValidationResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformValidationResult> validateWithHttpInfo(String moduleDirectory, UUID xCustomRequestId) throws RestClientException {
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

        if (xCustomRequestId != null)
        localVarHeaderParams.add("X-Custom-RequestId", apiClient.parameterToString(xCustomRequestId));

        final String[] localVarAccepts = { 
            "*/*", "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "OAuth2Flow" };

        ParameterizedTypeReference<TerraformValidationResult> localReturnType = new ParameterizedTypeReference<TerraformValidationResult>() {};
        return apiClient.invokeAPI("/terraform-boot/directory/validate/{module_directory}", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
}
