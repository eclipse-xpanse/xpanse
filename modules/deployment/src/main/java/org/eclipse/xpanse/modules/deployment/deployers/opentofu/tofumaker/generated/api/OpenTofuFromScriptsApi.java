package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.ApiClient;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.BaseApi;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@jakarta.annotation.Generated(
        value = "org.openapitools.codegen.languages.JavaClientCodegen",
        comments = "Generator version: 7.11.0")
@Component(
        "org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromScriptsApi")
public class OpenTofuFromScriptsApi extends BaseApi {

    public OpenTofuFromScriptsApi() {
        super(new ApiClient());
    }

    @Autowired
    public OpenTofuFromScriptsApi(ApiClient apiClient) {
        super(apiClient);
    }

    /**
     * async deploy resources via OpenTofu
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>503</b> - Service Unavailable
     *
     * @param openTofuAsyncDeployFromScriptsRequest (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDeployWithScripts(
            OpenTofuAsyncDeployFromScriptsRequest openTofuAsyncDeployFromScriptsRequest)
            throws RestClientException {
        asyncDeployWithScriptsWithHttpInfo(openTofuAsyncDeployFromScriptsRequest);
    }

    /**
     * async deploy resources via OpenTofu
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>503</b> - Service Unavailable
     *
     * @param openTofuAsyncDeployFromScriptsRequest (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDeployWithScriptsWithHttpInfo(
            OpenTofuAsyncDeployFromScriptsRequest openTofuAsyncDeployFromScriptsRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuAsyncDeployFromScriptsRequest;

        // verify the required parameter 'openTofuAsyncDeployFromScriptsRequest' is set
        if (openTofuAsyncDeployFromScriptsRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuAsyncDeployFromScriptsRequest' when"
                            + " calling asyncDeployWithScripts");
        }

        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"*/*"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<Void> localReturnType =
                new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI(
                "/tofu-maker/scripts/deploy/async",
                HttpMethod.POST,
                Collections.<String, Object>emptyMap(),
                localVarQueryParams,
                localVarPostBody,
                localVarHeaderParams,
                localVarCookieParams,
                localVarFormParams,
                localVarAccept,
                localVarContentType,
                localVarAuthNames,
                localReturnType);
    }

    /**
     * Async destroy the OpenTofu modules
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>503</b> - Service Unavailable
     *
     * @param openTofuAsyncDestroyFromScriptsRequest (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDestroyWithScripts(
            OpenTofuAsyncDestroyFromScriptsRequest openTofuAsyncDestroyFromScriptsRequest)
            throws RestClientException {
        asyncDestroyWithScriptsWithHttpInfo(openTofuAsyncDestroyFromScriptsRequest);
    }

    /**
     * Async destroy the OpenTofu modules
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>503</b> - Service Unavailable
     *
     * @param openTofuAsyncDestroyFromScriptsRequest (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDestroyWithScriptsWithHttpInfo(
            OpenTofuAsyncDestroyFromScriptsRequest openTofuAsyncDestroyFromScriptsRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuAsyncDestroyFromScriptsRequest;

        // verify the required parameter 'openTofuAsyncDestroyFromScriptsRequest' is set
        if (openTofuAsyncDestroyFromScriptsRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuAsyncDestroyFromScriptsRequest' when"
                            + " calling asyncDestroyWithScripts");
        }

        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"*/*"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<Void> localReturnType =
                new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI(
                "/tofu-maker/scripts/destroy/async",
                HttpMethod.DELETE,
                Collections.<String, Object>emptyMap(),
                localVarQueryParams,
                localVarPostBody,
                localVarHeaderParams,
                localVarCookieParams,
                localVarFormParams,
                localVarAccept,
                localVarContentType,
                localVarAuthNames,
                localReturnType);
    }

    /**
     * async modify resources via OpenTofu
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>503</b> - Service Unavailable
     *
     * @param openTofuAsyncModifyFromScriptsRequest (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncModifyWithScripts(
            OpenTofuAsyncModifyFromScriptsRequest openTofuAsyncModifyFromScriptsRequest)
            throws RestClientException {
        asyncModifyWithScriptsWithHttpInfo(openTofuAsyncModifyFromScriptsRequest);
    }

    /**
     * async modify resources via OpenTofu
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>503</b> - Service Unavailable
     *
     * @param openTofuAsyncModifyFromScriptsRequest (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncModifyWithScriptsWithHttpInfo(
            OpenTofuAsyncModifyFromScriptsRequest openTofuAsyncModifyFromScriptsRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuAsyncModifyFromScriptsRequest;

        // verify the required parameter 'openTofuAsyncModifyFromScriptsRequest' is set
        if (openTofuAsyncModifyFromScriptsRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuAsyncModifyFromScriptsRequest' when"
                            + " calling asyncModifyWithScripts");
        }

        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"*/*"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<Void> localReturnType =
                new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI(
                "/tofu-maker/scripts/modify/async",
                HttpMethod.POST,
                Collections.<String, Object>emptyMap(),
                localVarQueryParams,
                localVarPostBody,
                localVarHeaderParams,
                localVarCookieParams,
                localVarFormParams,
                localVarAccept,
                localVarContentType,
                localVarAuthNames,
                localReturnType);
    }

    /**
     * Deploy resources via OpenTofu
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>503</b> - Service Unavailable
     *
     * @param openTofuDeployWithScriptsRequest (required)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult deployWithScripts(
            OpenTofuDeployWithScriptsRequest openTofuDeployWithScriptsRequest)
            throws RestClientException {
        return deployWithScriptsWithHttpInfo(openTofuDeployWithScriptsRequest).getBody();
    }

    /**
     * Deploy resources via OpenTofu
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>503</b> - Service Unavailable
     *
     * @param openTofuDeployWithScriptsRequest (required)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> deployWithScriptsWithHttpInfo(
            OpenTofuDeployWithScriptsRequest openTofuDeployWithScriptsRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuDeployWithScriptsRequest;

        // verify the required parameter 'openTofuDeployWithScriptsRequest' is set
        if (openTofuDeployWithScriptsRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuDeployWithScriptsRequest' when calling"
                            + " deployWithScripts");
        }

        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"application/json", "*/*"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<OpenTofuResult> localReturnType =
                new ParameterizedTypeReference<OpenTofuResult>() {};
        return apiClient.invokeAPI(
                "/tofu-maker/scripts/deploy",
                HttpMethod.POST,
                Collections.<String, Object>emptyMap(),
                localVarQueryParams,
                localVarPostBody,
                localVarHeaderParams,
                localVarCookieParams,
                localVarFormParams,
                localVarAccept,
                localVarContentType,
                localVarAuthNames,
                localReturnType);
    }

    /**
     * Destroy resources via OpenTofu
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>503</b> - Service Unavailable
     *
     * @param openTofuDestroyWithScriptsRequest (required)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult destroyWithScripts(
            OpenTofuDestroyWithScriptsRequest openTofuDestroyWithScriptsRequest)
            throws RestClientException {
        return destroyWithScriptsWithHttpInfo(openTofuDestroyWithScriptsRequest).getBody();
    }

    /**
     * Destroy resources via OpenTofu
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>503</b> - Service Unavailable
     *
     * @param openTofuDestroyWithScriptsRequest (required)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> destroyWithScriptsWithHttpInfo(
            OpenTofuDestroyWithScriptsRequest openTofuDestroyWithScriptsRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuDestroyWithScriptsRequest;

        // verify the required parameter 'openTofuDestroyWithScriptsRequest' is set
        if (openTofuDestroyWithScriptsRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuDestroyWithScriptsRequest' when"
                            + " calling destroyWithScripts");
        }

        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"application/json", "*/*"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<OpenTofuResult> localReturnType =
                new ParameterizedTypeReference<OpenTofuResult>() {};
        return apiClient.invokeAPI(
                "/tofu-maker/scripts/destroy",
                HttpMethod.POST,
                Collections.<String, Object>emptyMap(),
                localVarQueryParams,
                localVarPostBody,
                localVarHeaderParams,
                localVarCookieParams,
                localVarFormParams,
                localVarAccept,
                localVarContentType,
                localVarAuthNames,
                localReturnType);
    }

    /**
     * Modify resources via OpenTofu
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>503</b> - Service Unavailable
     *
     * @param openTofuModifyWithScriptsRequest (required)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult modifyWithScripts(
            OpenTofuModifyWithScriptsRequest openTofuModifyWithScriptsRequest)
            throws RestClientException {
        return modifyWithScriptsWithHttpInfo(openTofuModifyWithScriptsRequest).getBody();
    }

    /**
     * Modify resources via OpenTofu
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>503</b> - Service Unavailable
     *
     * @param openTofuModifyWithScriptsRequest (required)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> modifyWithScriptsWithHttpInfo(
            OpenTofuModifyWithScriptsRequest openTofuModifyWithScriptsRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuModifyWithScriptsRequest;

        // verify the required parameter 'openTofuModifyWithScriptsRequest' is set
        if (openTofuModifyWithScriptsRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuModifyWithScriptsRequest' when calling"
                            + " modifyWithScripts");
        }

        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"application/json", "*/*"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<OpenTofuResult> localReturnType =
                new ParameterizedTypeReference<OpenTofuResult>() {};
        return apiClient.invokeAPI(
                "/tofu-maker/scripts/modify",
                HttpMethod.POST,
                Collections.<String, Object>emptyMap(),
                localVarQueryParams,
                localVarPostBody,
                localVarHeaderParams,
                localVarCookieParams,
                localVarFormParams,
                localVarAccept,
                localVarContentType,
                localVarAuthNames,
                localReturnType);
    }

    /**
     * Get OpenTofu Plan as JSON string from the list of script files provided
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>503</b> - Service Unavailable
     *
     * @param openTofuPlanWithScriptsRequest (required)
     * @return OpenTofuPlan
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuPlan planWithScripts(
            OpenTofuPlanWithScriptsRequest openTofuPlanWithScriptsRequest)
            throws RestClientException {
        return planWithScriptsWithHttpInfo(openTofuPlanWithScriptsRequest).getBody();
    }

    /**
     * Get OpenTofu Plan as JSON string from the list of script files provided
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>503</b> - Service Unavailable
     *
     * @param openTofuPlanWithScriptsRequest (required)
     * @return ResponseEntity&lt;OpenTofuPlan&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuPlan> planWithScriptsWithHttpInfo(
            OpenTofuPlanWithScriptsRequest openTofuPlanWithScriptsRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuPlanWithScriptsRequest;

        // verify the required parameter 'openTofuPlanWithScriptsRequest' is set
        if (openTofuPlanWithScriptsRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuPlanWithScriptsRequest' when calling"
                            + " planWithScripts");
        }

        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"application/json", "*/*"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<OpenTofuPlan> localReturnType =
                new ParameterizedTypeReference<OpenTofuPlan>() {};
        return apiClient.invokeAPI(
                "/tofu-maker/scripts/plan",
                HttpMethod.POST,
                Collections.<String, Object>emptyMap(),
                localVarQueryParams,
                localVarPostBody,
                localVarHeaderParams,
                localVarCookieParams,
                localVarFormParams,
                localVarAccept,
                localVarContentType,
                localVarAuthNames,
                localReturnType);
    }

    /**
     * Deploy resources via OpenTofu
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>503</b> - Service Unavailable
     *
     * @param openTofuDeployWithScriptsRequest (required)
     * @return OpenTofuValidationResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuValidationResult validateWithScripts(
            OpenTofuDeployWithScriptsRequest openTofuDeployWithScriptsRequest)
            throws RestClientException {
        return validateWithScriptsWithHttpInfo(openTofuDeployWithScriptsRequest).getBody();
    }

    /**
     * Deploy resources via OpenTofu
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>503</b> - Service Unavailable
     *
     * @param openTofuDeployWithScriptsRequest (required)
     * @return ResponseEntity&lt;OpenTofuValidationResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuValidationResult> validateWithScriptsWithHttpInfo(
            OpenTofuDeployWithScriptsRequest openTofuDeployWithScriptsRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuDeployWithScriptsRequest;

        // verify the required parameter 'openTofuDeployWithScriptsRequest' is set
        if (openTofuDeployWithScriptsRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuDeployWithScriptsRequest' when calling"
                            + " validateWithScripts");
        }

        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"application/json", "*/*"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<OpenTofuValidationResult> localReturnType =
                new ParameterizedTypeReference<OpenTofuValidationResult>() {};
        return apiClient.invokeAPI(
                "/tofu-maker/scripts/validate",
                HttpMethod.POST,
                Collections.<String, Object>emptyMap(),
                localVarQueryParams,
                localVarPostBody,
                localVarHeaderParams,
                localVarCookieParams,
                localVarFormParams,
                localVarAccept,
                localVarContentType,
                localVarAuthNames,
                localReturnType);
    }

    @Override
    public <T> ResponseEntity<T> invokeAPI(
            String url, HttpMethod method, Object request, ParameterizedTypeReference<T> returnType)
            throws RestClientException {
        String localVarPath = url.replace(apiClient.getBasePath(), "");
        Object localVarPostBody = request;

        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"application/json", "*/*"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        return apiClient.invokeAPI(
                localVarPath,
                method,
                uriVariables,
                localVarQueryParams,
                localVarPostBody,
                localVarHeaderParams,
                localVarCookieParams,
                localVarFormParams,
                localVarAccept,
                localVarContentType,
                localVarAuthNames,
                returnType);
    }
}
