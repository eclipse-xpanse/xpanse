package org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.api;

import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.ApiClient;

import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.model.OpenTofuAsyncDeployFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.model.OpenTofuAsyncDestroyFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.model.OpenTofuDeployFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.model.OpenTofuDestroyFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.model.OpenTofuPlan;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.model.OpenTofuPlanFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.model.OpenTofuResult;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.model.OpenTofuValidationResult;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.model.Response;
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
@Component("org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.api.OpenTofuFromDirectoryApi")
public class OpenTofuFromDirectoryApi {
    private ApiClient apiClient;

    public OpenTofuFromDirectoryApi() {
        this(new ApiClient());
    }

    @Autowired
    public OpenTofuFromDirectoryApi(ApiClient apiClient) {
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
     * async deploy resources via OpenTofu from the given directory.
     * <p><b>202</b> - Accepted
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuAsyncDeployFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDeployFromDirectory(String moduleDirectory, OpenTofuAsyncDeployFromDirectoryRequest openTofuAsyncDeployFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        asyncDeployFromDirectoryWithHttpInfo(moduleDirectory, openTofuAsyncDeployFromDirectoryRequest, xCustomRequestId);
    }

    /**
     * 
     * async deploy resources via OpenTofu from the given directory.
     * <p><b>202</b> - Accepted
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuAsyncDeployFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDeployFromDirectoryWithHttpInfo(String moduleDirectory, OpenTofuAsyncDeployFromDirectoryRequest openTofuAsyncDeployFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuAsyncDeployFromDirectoryRequest;
        
        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'moduleDirectory' when calling asyncDeployFromDirectory");
        }
        
        // verify the required parameter 'openTofuAsyncDeployFromDirectoryRequest' is set
        if (openTofuAsyncDeployFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuAsyncDeployFromDirectoryRequest' when calling asyncDeployFromDirectory");
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
        return apiClient.invokeAPI("/tofu-maker/directory/deploy/async/{module_directory}", HttpMethod.POST, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * async destroy resources via OpenTofu from the given directory.
     * <p><b>202</b> - Accepted
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuAsyncDestroyFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDestroyFromDirectory(String moduleDirectory, OpenTofuAsyncDestroyFromDirectoryRequest openTofuAsyncDestroyFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        asyncDestroyFromDirectoryWithHttpInfo(moduleDirectory, openTofuAsyncDestroyFromDirectoryRequest, xCustomRequestId);
    }

    /**
     * 
     * async destroy resources via OpenTofu from the given directory.
     * <p><b>202</b> - Accepted
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuAsyncDestroyFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDestroyFromDirectoryWithHttpInfo(String moduleDirectory, OpenTofuAsyncDestroyFromDirectoryRequest openTofuAsyncDestroyFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuAsyncDestroyFromDirectoryRequest;
        
        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'moduleDirectory' when calling asyncDestroyFromDirectory");
        }
        
        // verify the required parameter 'openTofuAsyncDestroyFromDirectoryRequest' is set
        if (openTofuAsyncDestroyFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuAsyncDestroyFromDirectoryRequest' when calling asyncDestroyFromDirectory");
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
        return apiClient.invokeAPI("/tofu-maker/directory/destroy/async/{module_directory}", HttpMethod.DELETE, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Deploy resources via OpenTofu from the given directory.
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuDeployFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult deployFromDirectory(String moduleDirectory, OpenTofuDeployFromDirectoryRequest openTofuDeployFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        return deployFromDirectoryWithHttpInfo(moduleDirectory, openTofuDeployFromDirectoryRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Deploy resources via OpenTofu from the given directory.
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuDeployFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> deployFromDirectoryWithHttpInfo(String moduleDirectory, OpenTofuDeployFromDirectoryRequest openTofuDeployFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuDeployFromDirectoryRequest;
        
        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'moduleDirectory' when calling deployFromDirectory");
        }
        
        // verify the required parameter 'openTofuDeployFromDirectoryRequest' is set
        if (openTofuDeployFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuDeployFromDirectoryRequest' when calling deployFromDirectory");
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
            "application/json", "*/*"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
         };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "OAuth2Flow" };

        ParameterizedTypeReference<OpenTofuResult> localReturnType = new ParameterizedTypeReference<OpenTofuResult>() {};
        return apiClient.invokeAPI("/tofu-maker/directory/deploy/{module_directory}", HttpMethod.POST, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Destroy the resources from the given directory.
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuDestroyFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult destroyFromDirectory(String moduleDirectory, OpenTofuDestroyFromDirectoryRequest openTofuDestroyFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        return destroyFromDirectoryWithHttpInfo(moduleDirectory, openTofuDestroyFromDirectoryRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Destroy the resources from the given directory.
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuDestroyFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> destroyFromDirectoryWithHttpInfo(String moduleDirectory, OpenTofuDestroyFromDirectoryRequest openTofuDestroyFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuDestroyFromDirectoryRequest;
        
        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'moduleDirectory' when calling destroyFromDirectory");
        }
        
        // verify the required parameter 'openTofuDestroyFromDirectoryRequest' is set
        if (openTofuDestroyFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuDestroyFromDirectoryRequest' when calling destroyFromDirectory");
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
            "application/json", "*/*"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
         };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "OAuth2Flow" };

        ParameterizedTypeReference<OpenTofuResult> localReturnType = new ParameterizedTypeReference<OpenTofuResult>() {};
        return apiClient.invokeAPI("/tofu-maker/directory/destroy/{module_directory}", HttpMethod.DELETE, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Get OpenTofu Plan as JSON string from the given directory.
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuPlanFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return OpenTofuPlan
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuPlan plan(String moduleDirectory, OpenTofuPlanFromDirectoryRequest openTofuPlanFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        return planWithHttpInfo(moduleDirectory, openTofuPlanFromDirectoryRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Get OpenTofu Plan as JSON string from the given directory.
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuPlanFromDirectoryRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;OpenTofuPlan&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuPlan> planWithHttpInfo(String moduleDirectory, OpenTofuPlanFromDirectoryRequest openTofuPlanFromDirectoryRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuPlanFromDirectoryRequest;
        
        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'moduleDirectory' when calling plan");
        }
        
        // verify the required parameter 'openTofuPlanFromDirectoryRequest' is set
        if (openTofuPlanFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuPlanFromDirectoryRequest' when calling plan");
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
            "application/json", "*/*"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
         };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "OAuth2Flow" };

        ParameterizedTypeReference<OpenTofuPlan> localReturnType = new ParameterizedTypeReference<OpenTofuPlan>() {};
        return apiClient.invokeAPI("/tofu-maker/directory/plan/{module_directory}", HttpMethod.POST, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Validate the OpenTofu modules in the given directory.
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param xCustomRequestId  (optional)
     * @return OpenTofuValidationResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuValidationResult validateFromDirectory(String moduleDirectory, UUID xCustomRequestId) throws RestClientException {
        return validateFromDirectoryWithHttpInfo(moduleDirectory, xCustomRequestId).getBody();
    }

    /**
     * 
     * Validate the OpenTofu modules in the given directory.
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;OpenTofuValidationResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuValidationResult> validateFromDirectoryWithHttpInfo(String moduleDirectory, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'moduleDirectory' when calling validateFromDirectory");
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
            "application/json", "*/*"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "OAuth2Flow" };

        ParameterizedTypeReference<OpenTofuValidationResult> localReturnType = new ParameterizedTypeReference<OpenTofuValidationResult>() {};
        return apiClient.invokeAPI("/tofu-maker/directory/validate/{module_directory}", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
}
