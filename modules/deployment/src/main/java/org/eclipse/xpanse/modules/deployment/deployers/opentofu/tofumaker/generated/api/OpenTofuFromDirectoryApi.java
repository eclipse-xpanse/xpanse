package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.ApiClient;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.BaseApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncRequestWithScriptsDirectory;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuPlan;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuRequestWithScriptsDirectory;
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
        "org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.OpenTofuFromDirectoryApi")
public class OpenTofuFromDirectoryApi extends BaseApi {

    public OpenTofuFromDirectoryApi() {
        super(new ApiClient());
    }

    @Autowired
    public OpenTofuFromDirectoryApi(ApiClient apiClient) {
        super(apiClient);
    }

    /**
     * async deploy resources via OpenTofu from the given directory.
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param openTofuAsyncRequestWithScriptsDirectory (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDeployFromDirectory(
            OpenTofuAsyncRequestWithScriptsDirectory openTofuAsyncRequestWithScriptsDirectory)
            throws RestClientException {
        asyncDeployFromDirectoryWithHttpInfo(openTofuAsyncRequestWithScriptsDirectory);
    }

    /**
     * async deploy resources via OpenTofu from the given directory.
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param openTofuAsyncRequestWithScriptsDirectory (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDeployFromDirectoryWithHttpInfo(
            OpenTofuAsyncRequestWithScriptsDirectory openTofuAsyncRequestWithScriptsDirectory)
            throws RestClientException {
        Object localVarPostBody = openTofuAsyncRequestWithScriptsDirectory;

        // verify the required parameter 'openTofuAsyncRequestWithScriptsDirectory' is set
        if (openTofuAsyncRequestWithScriptsDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuAsyncRequestWithScriptsDirectory' when"
                            + " calling asyncDeployFromDirectory");
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
                "/tofu-maker/directory/deploy/async",
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
     * async destroy resources via OpenTofu from the given directory.
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param openTofuAsyncRequestWithScriptsDirectory (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDestroyFromDirectory(
            OpenTofuAsyncRequestWithScriptsDirectory openTofuAsyncRequestWithScriptsDirectory)
            throws RestClientException {
        asyncDestroyFromDirectoryWithHttpInfo(openTofuAsyncRequestWithScriptsDirectory);
    }

    /**
     * async destroy resources via OpenTofu from the given directory.
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param openTofuAsyncRequestWithScriptsDirectory (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDestroyFromDirectoryWithHttpInfo(
            OpenTofuAsyncRequestWithScriptsDirectory openTofuAsyncRequestWithScriptsDirectory)
            throws RestClientException {
        Object localVarPostBody = openTofuAsyncRequestWithScriptsDirectory;

        // verify the required parameter 'openTofuAsyncRequestWithScriptsDirectory' is set
        if (openTofuAsyncRequestWithScriptsDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuAsyncRequestWithScriptsDirectory' when"
                            + " calling asyncDestroyFromDirectory");
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
                "/tofu-maker/directory/destroy/async",
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
     * async modify resources via OpenTofu from the given directory.
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param openTofuAsyncRequestWithScriptsDirectory (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncModifyFromDirectory(
            OpenTofuAsyncRequestWithScriptsDirectory openTofuAsyncRequestWithScriptsDirectory)
            throws RestClientException {
        asyncModifyFromDirectoryWithHttpInfo(openTofuAsyncRequestWithScriptsDirectory);
    }

    /**
     * async modify resources via OpenTofu from the given directory.
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param openTofuAsyncRequestWithScriptsDirectory (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncModifyFromDirectoryWithHttpInfo(
            OpenTofuAsyncRequestWithScriptsDirectory openTofuAsyncRequestWithScriptsDirectory)
            throws RestClientException {
        Object localVarPostBody = openTofuAsyncRequestWithScriptsDirectory;

        // verify the required parameter 'openTofuAsyncRequestWithScriptsDirectory' is set
        if (openTofuAsyncRequestWithScriptsDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuAsyncRequestWithScriptsDirectory' when"
                            + " calling asyncModifyFromDirectory");
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
                "/tofu-maker/directory/modify/async",
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
     * Deploy resources via OpenTofu from the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param openTofuRequestWithScriptsDirectory (required)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult deployFromDirectory(
            OpenTofuRequestWithScriptsDirectory openTofuRequestWithScriptsDirectory)
            throws RestClientException {
        return deployFromDirectoryWithHttpInfo(openTofuRequestWithScriptsDirectory).getBody();
    }

    /**
     * Deploy resources via OpenTofu from the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param openTofuRequestWithScriptsDirectory (required)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> deployFromDirectoryWithHttpInfo(
            OpenTofuRequestWithScriptsDirectory openTofuRequestWithScriptsDirectory)
            throws RestClientException {
        Object localVarPostBody = openTofuRequestWithScriptsDirectory;

        // verify the required parameter 'openTofuRequestWithScriptsDirectory' is set
        if (openTofuRequestWithScriptsDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuRequestWithScriptsDirectory' when"
                            + " calling deployFromDirectory");
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
                "/tofu-maker/directory/deploy",
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
     * Destroy the resources from the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param openTofuRequestWithScriptsDirectory (required)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult destroyFromDirectory(
            OpenTofuRequestWithScriptsDirectory openTofuRequestWithScriptsDirectory)
            throws RestClientException {
        return destroyFromDirectoryWithHttpInfo(openTofuRequestWithScriptsDirectory).getBody();
    }

    /**
     * Destroy the resources from the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param openTofuRequestWithScriptsDirectory (required)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> destroyFromDirectoryWithHttpInfo(
            OpenTofuRequestWithScriptsDirectory openTofuRequestWithScriptsDirectory)
            throws RestClientException {
        Object localVarPostBody = openTofuRequestWithScriptsDirectory;

        // verify the required parameter 'openTofuRequestWithScriptsDirectory' is set
        if (openTofuRequestWithScriptsDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuRequestWithScriptsDirectory' when"
                            + " calling destroyFromDirectory");
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
                "/tofu-maker/directory/destroy",
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
     * Modify resources via OpenTofu from the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param openTofuRequestWithScriptsDirectory (required)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult modifyFromDirectory(
            OpenTofuRequestWithScriptsDirectory openTofuRequestWithScriptsDirectory)
            throws RestClientException {
        return modifyFromDirectoryWithHttpInfo(openTofuRequestWithScriptsDirectory).getBody();
    }

    /**
     * Modify resources via OpenTofu from the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param openTofuRequestWithScriptsDirectory (required)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> modifyFromDirectoryWithHttpInfo(
            OpenTofuRequestWithScriptsDirectory openTofuRequestWithScriptsDirectory)
            throws RestClientException {
        Object localVarPostBody = openTofuRequestWithScriptsDirectory;

        // verify the required parameter 'openTofuRequestWithScriptsDirectory' is set
        if (openTofuRequestWithScriptsDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuRequestWithScriptsDirectory' when"
                            + " calling modifyFromDirectory");
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
                "/tofu-maker/directory/modify",
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
     * Get OpenTofu Plan as JSON string from the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param openTofuRequestWithScriptsDirectory (required)
     * @return OpenTofuPlan
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuPlan plan(
            OpenTofuRequestWithScriptsDirectory openTofuRequestWithScriptsDirectory)
            throws RestClientException {
        return planWithHttpInfo(openTofuRequestWithScriptsDirectory).getBody();
    }

    /**
     * Get OpenTofu Plan as JSON string from the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param openTofuRequestWithScriptsDirectory (required)
     * @return ResponseEntity&lt;OpenTofuPlan&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuPlan> planWithHttpInfo(
            OpenTofuRequestWithScriptsDirectory openTofuRequestWithScriptsDirectory)
            throws RestClientException {
        Object localVarPostBody = openTofuRequestWithScriptsDirectory;

        // verify the required parameter 'openTofuRequestWithScriptsDirectory' is set
        if (openTofuRequestWithScriptsDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuRequestWithScriptsDirectory' when"
                            + " calling plan");
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
                "/tofu-maker/directory/plan",
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
     * Validate the OpenTofu modules in the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param openTofuRequestWithScriptsDirectory (required)
     * @return OpenTofuValidationResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuValidationResult validateFromDirectory(
            OpenTofuRequestWithScriptsDirectory openTofuRequestWithScriptsDirectory)
            throws RestClientException {
        return validateFromDirectoryWithHttpInfo(openTofuRequestWithScriptsDirectory).getBody();
    }

    /**
     * Validate the OpenTofu modules in the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param openTofuRequestWithScriptsDirectory (required)
     * @return ResponseEntity&lt;OpenTofuValidationResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuValidationResult> validateFromDirectoryWithHttpInfo(
            OpenTofuRequestWithScriptsDirectory openTofuRequestWithScriptsDirectory)
            throws RestClientException {
        Object localVarPostBody = openTofuRequestWithScriptsDirectory;

        // verify the required parameter 'openTofuRequestWithScriptsDirectory' is set
        if (openTofuRequestWithScriptsDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuRequestWithScriptsDirectory' when"
                            + " calling validateFromDirectory");
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
                "/tofu-maker/directory/validate",
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
