package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api;

import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.ApiClient;

import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncDeployFromScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncDestroyFromScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncModifyFromScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuDeployWithScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuDestroyWithScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuModifyWithScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuPlan;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuPlanWithScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuResult;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuValidationResult;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.Response;
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

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.4.0")
@Component("org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromScriptsApi")
public class OpenTofuFromScriptsApi {
    private ApiClient apiClient;

    public OpenTofuFromScriptsApi() {
        this(new ApiClient());
    }

    @Autowired
    public OpenTofuFromScriptsApi(ApiClient apiClient) {
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
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     * @param openTofuAsyncDeployFromScriptsRequest  (required)
     * @param xCustomRequestId  (optional)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDeployWithScripts(OpenTofuAsyncDeployFromScriptsRequest openTofuAsyncDeployFromScriptsRequest, UUID xCustomRequestId) throws RestClientException {
        asyncDeployWithScriptsWithHttpInfo(openTofuAsyncDeployFromScriptsRequest, xCustomRequestId);
    }

    /**
     * 
     * async deploy resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     * @param openTofuAsyncDeployFromScriptsRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDeployWithScriptsWithHttpInfo(OpenTofuAsyncDeployFromScriptsRequest openTofuAsyncDeployFromScriptsRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuAsyncDeployFromScriptsRequest;
        
        // verify the required parameter 'openTofuAsyncDeployFromScriptsRequest' is set
        if (openTofuAsyncDeployFromScriptsRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuAsyncDeployFromScriptsRequest' when calling asyncDeployWithScripts");
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
        return apiClient.invokeAPI("/tofu-maker/scripts/deploy/async", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Async destroy the OpenTofu modules
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     * @param openTofuAsyncDestroyFromScriptsRequest  (required)
     * @param xCustomRequestId  (optional)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDestroyWithScripts(OpenTofuAsyncDestroyFromScriptsRequest openTofuAsyncDestroyFromScriptsRequest, UUID xCustomRequestId) throws RestClientException {
        asyncDestroyWithScriptsWithHttpInfo(openTofuAsyncDestroyFromScriptsRequest, xCustomRequestId);
    }

    /**
     * 
     * Async destroy the OpenTofu modules
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     * @param openTofuAsyncDestroyFromScriptsRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDestroyWithScriptsWithHttpInfo(OpenTofuAsyncDestroyFromScriptsRequest openTofuAsyncDestroyFromScriptsRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuAsyncDestroyFromScriptsRequest;
        
        // verify the required parameter 'openTofuAsyncDestroyFromScriptsRequest' is set
        if (openTofuAsyncDestroyFromScriptsRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuAsyncDestroyFromScriptsRequest' when calling asyncDestroyWithScripts");
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
        return apiClient.invokeAPI("/tofu-maker/scripts/destroy/async", HttpMethod.DELETE, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * async modify resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     * @param openTofuAsyncModifyFromScriptsRequest  (required)
     * @param xCustomRequestId  (optional)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncModifyWithScripts(OpenTofuAsyncModifyFromScriptsRequest openTofuAsyncModifyFromScriptsRequest, UUID xCustomRequestId) throws RestClientException {
        asyncModifyWithScriptsWithHttpInfo(openTofuAsyncModifyFromScriptsRequest, xCustomRequestId);
    }

    /**
     * 
     * async modify resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     * @param openTofuAsyncModifyFromScriptsRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncModifyWithScriptsWithHttpInfo(OpenTofuAsyncModifyFromScriptsRequest openTofuAsyncModifyFromScriptsRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuAsyncModifyFromScriptsRequest;
        
        // verify the required parameter 'openTofuAsyncModifyFromScriptsRequest' is set
        if (openTofuAsyncModifyFromScriptsRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuAsyncModifyFromScriptsRequest' when calling asyncModifyWithScripts");
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
        return apiClient.invokeAPI("/tofu-maker/scripts/modify/async", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Deploy resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param openTofuDeployWithScriptsRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult deployWithScripts(OpenTofuDeployWithScriptsRequest openTofuDeployWithScriptsRequest, UUID xCustomRequestId) throws RestClientException {
        return deployWithScriptsWithHttpInfo(openTofuDeployWithScriptsRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Deploy resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param openTofuDeployWithScriptsRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> deployWithScriptsWithHttpInfo(OpenTofuDeployWithScriptsRequest openTofuDeployWithScriptsRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuDeployWithScriptsRequest;
        
        // verify the required parameter 'openTofuDeployWithScriptsRequest' is set
        if (openTofuDeployWithScriptsRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuDeployWithScriptsRequest' when calling deployWithScripts");
        }
        

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

        ParameterizedTypeReference<OpenTofuResult> localReturnType = new ParameterizedTypeReference<OpenTofuResult>() {};
        return apiClient.invokeAPI("/tofu-maker/scripts/deploy", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Destroy resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param openTofuDestroyWithScriptsRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult destroyWithScripts(OpenTofuDestroyWithScriptsRequest openTofuDestroyWithScriptsRequest, UUID xCustomRequestId) throws RestClientException {
        return destroyWithScriptsWithHttpInfo(openTofuDestroyWithScriptsRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Destroy resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param openTofuDestroyWithScriptsRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> destroyWithScriptsWithHttpInfo(OpenTofuDestroyWithScriptsRequest openTofuDestroyWithScriptsRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuDestroyWithScriptsRequest;
        
        // verify the required parameter 'openTofuDestroyWithScriptsRequest' is set
        if (openTofuDestroyWithScriptsRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuDestroyWithScriptsRequest' when calling destroyWithScripts");
        }
        

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

        ParameterizedTypeReference<OpenTofuResult> localReturnType = new ParameterizedTypeReference<OpenTofuResult>() {};
        return apiClient.invokeAPI("/tofu-maker/scripts/destroy", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Modify resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param openTofuModifyWithScriptsRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult modifyWithScripts(OpenTofuModifyWithScriptsRequest openTofuModifyWithScriptsRequest, UUID xCustomRequestId) throws RestClientException {
        return modifyWithScriptsWithHttpInfo(openTofuModifyWithScriptsRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Modify resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param openTofuModifyWithScriptsRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> modifyWithScriptsWithHttpInfo(OpenTofuModifyWithScriptsRequest openTofuModifyWithScriptsRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuModifyWithScriptsRequest;
        
        // verify the required parameter 'openTofuModifyWithScriptsRequest' is set
        if (openTofuModifyWithScriptsRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuModifyWithScriptsRequest' when calling modifyWithScripts");
        }
        

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

        ParameterizedTypeReference<OpenTofuResult> localReturnType = new ParameterizedTypeReference<OpenTofuResult>() {};
        return apiClient.invokeAPI("/tofu-maker/scripts/modify", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Get OpenTofu Plan as JSON string from the list of script files provided
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param openTofuPlanWithScriptsRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return OpenTofuPlan
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuPlan planWithScripts(OpenTofuPlanWithScriptsRequest openTofuPlanWithScriptsRequest, UUID xCustomRequestId) throws RestClientException {
        return planWithScriptsWithHttpInfo(openTofuPlanWithScriptsRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Get OpenTofu Plan as JSON string from the list of script files provided
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param openTofuPlanWithScriptsRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;OpenTofuPlan&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuPlan> planWithScriptsWithHttpInfo(OpenTofuPlanWithScriptsRequest openTofuPlanWithScriptsRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuPlanWithScriptsRequest;
        
        // verify the required parameter 'openTofuPlanWithScriptsRequest' is set
        if (openTofuPlanWithScriptsRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuPlanWithScriptsRequest' when calling planWithScripts");
        }
        

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

        ParameterizedTypeReference<OpenTofuPlan> localReturnType = new ParameterizedTypeReference<OpenTofuPlan>() {};
        return apiClient.invokeAPI("/tofu-maker/scripts/plan", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Deploy resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param openTofuDeployWithScriptsRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return OpenTofuValidationResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuValidationResult validateWithScripts(OpenTofuDeployWithScriptsRequest openTofuDeployWithScriptsRequest, UUID xCustomRequestId) throws RestClientException {
        return validateWithScriptsWithHttpInfo(openTofuDeployWithScriptsRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Deploy resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>502</b> - Bad Gateway
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param openTofuDeployWithScriptsRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;OpenTofuValidationResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuValidationResult> validateWithScriptsWithHttpInfo(OpenTofuDeployWithScriptsRequest openTofuDeployWithScriptsRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = openTofuDeployWithScriptsRequest;
        
        // verify the required parameter 'openTofuDeployWithScriptsRequest' is set
        if (openTofuDeployWithScriptsRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'openTofuDeployWithScriptsRequest' when calling validateWithScripts");
        }
        

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

        ParameterizedTypeReference<OpenTofuValidationResult> localReturnType = new ParameterizedTypeReference<OpenTofuValidationResult>() {};
        return apiClient.invokeAPI("/tofu-maker/scripts/validate", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
}
