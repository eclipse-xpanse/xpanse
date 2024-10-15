package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.ApiClient;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.BaseApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncDeployFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncDestroyFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncModifyFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuDeployFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuDestroyFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuModifyFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuPlan;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuPlanFromGitRepoRequest;
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

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.9.0")
@Component("org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromGitRepoApi")
public class OpenTofuFromGitRepoApi extends BaseApi {

    public OpenTofuFromGitRepoApi() {
        super(new ApiClient());
    }

    @Autowired
    public OpenTofuFromGitRepoApi(ApiClient apiClient) {
        super(apiClient);
    }

    /**
     * async deploy resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>202</b> - Accepted
     *
     * @param openTofuAsyncDeployFromGitRepoRequest (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDeployFromGitRepo(
            OpenTofuAsyncDeployFromGitRepoRequest openTofuAsyncDeployFromGitRepoRequest)
            throws RestClientException {
        asyncDeployFromGitRepoWithHttpInfo(openTofuAsyncDeployFromGitRepoRequest);
    }

    /**
     * async deploy resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>202</b> - Accepted
     *
     * @param openTofuAsyncDeployFromGitRepoRequest (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDeployFromGitRepoWithHttpInfo(
            OpenTofuAsyncDeployFromGitRepoRequest openTofuAsyncDeployFromGitRepoRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuAsyncDeployFromGitRepoRequest;

        // verify the required parameter 'openTofuAsyncDeployFromGitRepoRequest' is set
        if (openTofuAsyncDeployFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuAsyncDeployFromGitRepoRequest' when calling asyncDeployFromGitRepo");
        }


        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {
                "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {
                "application/json"
        };
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<Void> localReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/tofu-maker/git/deploy/async", HttpMethod.POST,
                Collections.emptyMap(), localVarQueryParams, localVarPostBody,
                localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
                localVarContentType, localVarAuthNames, localReturnType);
    }

    /**
     * Async destroy the OpenTofu modules
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>202</b> - Accepted
     *
     * @param openTofuAsyncDestroyFromGitRepoRequest (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDestroyFromGitRepo(
            OpenTofuAsyncDestroyFromGitRepoRequest openTofuAsyncDestroyFromGitRepoRequest)
            throws RestClientException {
        asyncDestroyFromGitRepoWithHttpInfo(openTofuAsyncDestroyFromGitRepoRequest);
    }

    /**
     * Async destroy the OpenTofu modules
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>202</b> - Accepted
     *
     * @param openTofuAsyncDestroyFromGitRepoRequest (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDestroyFromGitRepoWithHttpInfo(
            OpenTofuAsyncDestroyFromGitRepoRequest openTofuAsyncDestroyFromGitRepoRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuAsyncDestroyFromGitRepoRequest;

        // verify the required parameter 'openTofuAsyncDestroyFromGitRepoRequest' is set
        if (openTofuAsyncDestroyFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuAsyncDestroyFromGitRepoRequest' when calling asyncDestroyFromGitRepo");
        }


        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {
                "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {
                "application/json"
        };
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<Void> localReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/tofu-maker/git/destroy/async", HttpMethod.DELETE,
                Collections.emptyMap(), localVarQueryParams, localVarPostBody,
                localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
                localVarContentType, localVarAuthNames, localReturnType);
    }

    /**
     * async modify resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>202</b> - Accepted
     *
     * @param openTofuAsyncModifyFromGitRepoRequest (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncModifyFromGitRepo(
            OpenTofuAsyncModifyFromGitRepoRequest openTofuAsyncModifyFromGitRepoRequest)
            throws RestClientException {
        asyncModifyFromGitRepoWithHttpInfo(openTofuAsyncModifyFromGitRepoRequest);
    }

    /**
     * async modify resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>202</b> - Accepted
     *
     * @param openTofuAsyncModifyFromGitRepoRequest (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncModifyFromGitRepoWithHttpInfo(
            OpenTofuAsyncModifyFromGitRepoRequest openTofuAsyncModifyFromGitRepoRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuAsyncModifyFromGitRepoRequest;

        // verify the required parameter 'openTofuAsyncModifyFromGitRepoRequest' is set
        if (openTofuAsyncModifyFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuAsyncModifyFromGitRepoRequest' when calling asyncModifyFromGitRepo");
        }


        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {
                "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {
                "application/json"
        };
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<Void> localReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/tofu-maker/git/modify/async", HttpMethod.POST,
                Collections.emptyMap(), localVarQueryParams, localVarPostBody,
                localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
                localVarContentType, localVarAuthNames, localReturnType);
    }

    /**
     * Deploy resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     *
     * @param openTofuDeployFromGitRepoRequest (required)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult deployFromGitRepo(
            OpenTofuDeployFromGitRepoRequest openTofuDeployFromGitRepoRequest)
            throws RestClientException {
        return deployFromGitRepoWithHttpInfo(openTofuDeployFromGitRepoRequest).getBody();
    }

    /**
     * Deploy resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     *
     * @param openTofuDeployFromGitRepoRequest (required)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> deployFromGitRepoWithHttpInfo(
            OpenTofuDeployFromGitRepoRequest openTofuDeployFromGitRepoRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuDeployFromGitRepoRequest;

        // verify the required parameter 'openTofuDeployFromGitRepoRequest' is set
        if (openTofuDeployFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuDeployFromGitRepoRequest' when calling deployFromGitRepo");
        }


        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {
                "*/*", "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {
                "application/json"
        };
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<OpenTofuResult> localReturnType =
                new ParameterizedTypeReference<OpenTofuResult>() {
                };
        return apiClient.invokeAPI("/tofu-maker/git/deploy", HttpMethod.POST,
                Collections.emptyMap(), localVarQueryParams, localVarPostBody,
                localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
                localVarContentType, localVarAuthNames, localReturnType);
    }

    /**
     * Destroy resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     *
     * @param openTofuDestroyFromGitRepoRequest (required)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult destroyFromGitRepo(
            OpenTofuDestroyFromGitRepoRequest openTofuDestroyFromGitRepoRequest)
            throws RestClientException {
        return destroyFromGitRepoWithHttpInfo(openTofuDestroyFromGitRepoRequest).getBody();
    }

    /**
     * Destroy resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     *
     * @param openTofuDestroyFromGitRepoRequest (required)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> destroyFromGitRepoWithHttpInfo(
            OpenTofuDestroyFromGitRepoRequest openTofuDestroyFromGitRepoRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuDestroyFromGitRepoRequest;

        // verify the required parameter 'openTofuDestroyFromGitRepoRequest' is set
        if (openTofuDestroyFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuDestroyFromGitRepoRequest' when calling destroyFromGitRepo");
        }


        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {
                "*/*", "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {
                "application/json"
        };
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<OpenTofuResult> localReturnType =
                new ParameterizedTypeReference<OpenTofuResult>() {
                };
        return apiClient.invokeAPI("/tofu-maker/git/destroy", HttpMethod.POST,
                Collections.emptyMap(), localVarQueryParams, localVarPostBody,
                localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
                localVarContentType, localVarAuthNames, localReturnType);
    }

    /**
     * Modify resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     *
     * @param openTofuModifyFromGitRepoRequest (required)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult modifyFromGitRepo(
            OpenTofuModifyFromGitRepoRequest openTofuModifyFromGitRepoRequest)
            throws RestClientException {
        return modifyFromGitRepoWithHttpInfo(openTofuModifyFromGitRepoRequest).getBody();
    }

    /**
     * Modify resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     *
     * @param openTofuModifyFromGitRepoRequest (required)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> modifyFromGitRepoWithHttpInfo(
            OpenTofuModifyFromGitRepoRequest openTofuModifyFromGitRepoRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuModifyFromGitRepoRequest;

        // verify the required parameter 'openTofuModifyFromGitRepoRequest' is set
        if (openTofuModifyFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuModifyFromGitRepoRequest' when calling modifyFromGitRepo");
        }


        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {
                "*/*", "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {
                "application/json"
        };
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<OpenTofuResult> localReturnType =
                new ParameterizedTypeReference<OpenTofuResult>() {
                };
        return apiClient.invokeAPI("/tofu-maker/git/modify", HttpMethod.POST,
                Collections.emptyMap(), localVarQueryParams, localVarPostBody,
                localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
                localVarContentType, localVarAuthNames, localReturnType);
    }

    /**
     * Get OpenTofu Plan as JSON string from the list of script files provided
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     *
     * @param openTofuPlanFromGitRepoRequest (required)
     * @return OpenTofuPlan
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuPlan planFromGitRepo(
            OpenTofuPlanFromGitRepoRequest openTofuPlanFromGitRepoRequest)
            throws RestClientException {
        return planFromGitRepoWithHttpInfo(openTofuPlanFromGitRepoRequest).getBody();
    }

    /**
     * Get OpenTofu Plan as JSON string from the list of script files provided
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     *
     * @param openTofuPlanFromGitRepoRequest (required)
     * @return ResponseEntity&lt;OpenTofuPlan&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuPlan> planFromGitRepoWithHttpInfo(
            OpenTofuPlanFromGitRepoRequest openTofuPlanFromGitRepoRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuPlanFromGitRepoRequest;

        // verify the required parameter 'openTofuPlanFromGitRepoRequest' is set
        if (openTofuPlanFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuPlanFromGitRepoRequest' when calling planFromGitRepo");
        }


        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {
                "*/*", "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {
                "application/json"
        };
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<OpenTofuPlan> localReturnType =
                new ParameterizedTypeReference<OpenTofuPlan>() {
                };
        return apiClient.invokeAPI("/tofu-maker/git/plan", HttpMethod.POST,
                Collections.emptyMap(), localVarQueryParams, localVarPostBody,
                localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
                localVarContentType, localVarAuthNames, localReturnType);
    }

    /**
     * Deploy resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     *
     * @param openTofuDeployFromGitRepoRequest (required)
     * @return OpenTofuValidationResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuValidationResult validateScriptsFromGitRepo(
            OpenTofuDeployFromGitRepoRequest openTofuDeployFromGitRepoRequest)
            throws RestClientException {
        return validateScriptsFromGitRepoWithHttpInfo(openTofuDeployFromGitRepoRequest).getBody();
    }

    /**
     * Deploy resources via OpenTofu
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     *
     * @param openTofuDeployFromGitRepoRequest (required)
     * @return ResponseEntity&lt;OpenTofuValidationResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuValidationResult> validateScriptsFromGitRepoWithHttpInfo(
            OpenTofuDeployFromGitRepoRequest openTofuDeployFromGitRepoRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuDeployFromGitRepoRequest;

        // verify the required parameter 'openTofuDeployFromGitRepoRequest' is set
        if (openTofuDeployFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuDeployFromGitRepoRequest' when calling validateScriptsFromGitRepo");
        }


        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {
                "*/*", "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {
                "application/json"
        };
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<OpenTofuValidationResult> localReturnType =
                new ParameterizedTypeReference<OpenTofuValidationResult>() {
                };
        return apiClient.invokeAPI("/tofu-maker/git/validate", HttpMethod.POST,
                Collections.emptyMap(), localVarQueryParams, localVarPostBody,
                localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept,
                localVarContentType, localVarAuthNames, localReturnType);
    }

    @Override
    public <T> ResponseEntity<T> invokeAPI(String url, HttpMethod method, Object request,
                                           ParameterizedTypeReference<T> returnType)
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

        final String[] localVarAccepts = {
                "*/*", "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {
                "application/json"
        };
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        return apiClient.invokeAPI(localVarPath, method, uriVariables, localVarQueryParams,
                localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams,
                localVarAccept, localVarContentType, localVarAuthNames, returnType);
    }
}
