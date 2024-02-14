package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api;

import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.ApiClient;

import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncDeployFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncDestroyFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuDeployFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuDestroyFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuPlan;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuPlanFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuResult;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuValidationResult;
import java.util.UUID;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
@Component("org.eclipse.xpanse.modules.deployment.deployers.opentofu.opentofumaker.generated.api.OpenTofuFromGitRepoApi")
public class OpenTofuFromGitRepoApi {
    private ApiClient apiClient;

    public OpenTofuFromGitRepoApi() {
        this(new ApiClient());
    }

    @Autowired
    public OpenTofuFromGitRepoApi(ApiClient apiClient) {
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
     * async deploy resources via OpenTofu
     * <p><b>202</b> - Accepted
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param openTofuAsyncDeployFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDeployFromGitRepo(OpenTofuAsyncDeployFromGitRepoRequest openTofuAsyncDeployFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        asyncDeployFromGitRepoWithHttpInfo(openTofuAsyncDeployFromGitRepoRequest, xCustomRequestId);
    }

    /**
     * 
     * async deploy resources via OpenTofu
     * <p><b>202</b> - Accepted
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param openTofuAsyncDeployFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDeployFromGitRepoWithHttpInfo(OpenTofuAsyncDeployFromGitRepoRequest openTofuAsyncDeployFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuAsyncDeployFromGitRepoRequest;
        
        // verify the required parameter 'openTofuAsyncDeployFromGitRepoRequest' is set
        if (openTofuAsyncDeployFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuAsyncDeployFromGitRepoRequest' when calling asyncDeployFromGitRepo");
        }
        

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
        return apiClient.invokeAPI("/tofu-maker/git/deploy/async", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Async destroy the OpenTofu modules
     * <p><b>202</b> - Accepted
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param openTofuAsyncDestroyFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDestroyFromGitRepo(OpenTofuAsyncDestroyFromGitRepoRequest openTofuAsyncDestroyFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        asyncDestroyFromGitRepoWithHttpInfo(openTofuAsyncDestroyFromGitRepoRequest, xCustomRequestId);
    }

    /**
     * 
     * Async destroy the OpenTofu modules
     * <p><b>202</b> - Accepted
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param openTofuAsyncDestroyFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDestroyFromGitRepoWithHttpInfo(OpenTofuAsyncDestroyFromGitRepoRequest openTofuAsyncDestroyFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuAsyncDestroyFromGitRepoRequest;
        
        // verify the required parameter 'openTofuAsyncDestroyFromGitRepoRequest' is set
        if (openTofuAsyncDestroyFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuAsyncDestroyFromGitRepoRequest' when calling asyncDestroyFromGitRepo");
        }
        

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
        return apiClient.invokeAPI("/tofu-maker/git/destroy/async", HttpMethod.DELETE, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Deploy resources via OpenTofu
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param openTofuDeployFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult deployFromGitRepo(OpenTofuDeployFromGitRepoRequest openTofuDeployFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        return deployFromGitRepoWithHttpInfo(openTofuDeployFromGitRepoRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Deploy resources via OpenTofu
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param openTofuDeployFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> deployFromGitRepoWithHttpInfo(OpenTofuDeployFromGitRepoRequest openTofuDeployFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuDeployFromGitRepoRequest;
        
        // verify the required parameter 'openTofuDeployFromGitRepoRequest' is set
        if (openTofuDeployFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuDeployFromGitRepoRequest' when calling deployFromGitRepo");
        }
        

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
        return apiClient.invokeAPI("/tofu-maker/git/deploy", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Destroy resources via OpenTofu
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param openTofuDestroyFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult destroyFromGitRepo(OpenTofuDestroyFromGitRepoRequest openTofuDestroyFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        return destroyFromGitRepoWithHttpInfo(openTofuDestroyFromGitRepoRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Destroy resources via OpenTofu
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param openTofuDestroyFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> destroyFromGitRepoWithHttpInfo(OpenTofuDestroyFromGitRepoRequest openTofuDestroyFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuDestroyFromGitRepoRequest;
        
        // verify the required parameter 'openTofuDestroyFromGitRepoRequest' is set
        if (openTofuDestroyFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuDestroyFromGitRepoRequest' when calling destroyFromGitRepo");
        }
        

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
        return apiClient.invokeAPI("/tofu-maker/git/destroy", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Get OpenTofu Plan as JSON string from the list of script files provided
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param openTofuPlanFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return OpenTofuPlan
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuPlan planFromGitRepo(OpenTofuPlanFromGitRepoRequest openTofuPlanFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        return planFromGitRepoWithHttpInfo(openTofuPlanFromGitRepoRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Get OpenTofu Plan as JSON string from the list of script files provided
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param openTofuPlanFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;OpenTofuPlan&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuPlan> planFromGitRepoWithHttpInfo(OpenTofuPlanFromGitRepoRequest openTofuPlanFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuPlanFromGitRepoRequest;
        
        // verify the required parameter 'openTofuPlanFromGitRepoRequest' is set
        if (openTofuPlanFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuPlanFromGitRepoRequest' when calling planFromGitRepo");
        }
        

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
        return apiClient.invokeAPI("/tofu-maker/git/plan", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Deploy resources via OpenTofu
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param openTofuDeployFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return OpenTofuValidationResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuValidationResult validateScriptsFromGitRepo(OpenTofuDeployFromGitRepoRequest openTofuDeployFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        return validateScriptsFromGitRepoWithHttpInfo(openTofuDeployFromGitRepoRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Deploy resources via OpenTofu
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * @param openTofuDeployFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;OpenTofuValidationResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuValidationResult> validateScriptsFromGitRepoWithHttpInfo(OpenTofuDeployFromGitRepoRequest openTofuDeployFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuDeployFromGitRepoRequest;
        
        // verify the required parameter 'openTofuDeployFromGitRepoRequest' is set
        if (openTofuDeployFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuDeployFromGitRepoRequest' when calling validateScriptsFromGitRepo");
        }
        

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

        ParameterizedTypeReference<OpenTofuValidationResult> localReturnType = new ParameterizedTypeReference<OpenTofuValidationResult>() {};
        return apiClient.invokeAPI("/tofu-maker/git/validate", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
}
