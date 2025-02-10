package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.ApiClient;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.BaseApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncDeployFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncDestroyFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuAsyncModifyFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuDeployFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuDestroyFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuModifyFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuPlan;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuPlanFromDirectoryRequest;
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
     * <p><b>400</b> - Bad Request
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>202</b> - Accepted
     *
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuAsyncDeployFromDirectoryRequest (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDeployFromDirectory(
            String moduleDirectory,
            OpenTofuAsyncDeployFromDirectoryRequest openTofuAsyncDeployFromDirectoryRequest)
            throws RestClientException {
        asyncDeployFromDirectoryWithHttpInfo(
                moduleDirectory, openTofuAsyncDeployFromDirectoryRequest);
    }

    /**
     * async deploy resources via OpenTofu from the given directory.
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>202</b> - Accepted
     *
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuAsyncDeployFromDirectoryRequest (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDeployFromDirectoryWithHttpInfo(
            String moduleDirectory,
            OpenTofuAsyncDeployFromDirectoryRequest openTofuAsyncDeployFromDirectoryRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuAsyncDeployFromDirectoryRequest;

        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'moduleDirectory' when calling"
                            + " asyncDeployFromDirectory");
        }

        // verify the required parameter 'openTofuAsyncDeployFromDirectoryRequest' is set
        if (openTofuAsyncDeployFromDirectoryRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuAsyncDeployFromDirectoryRequest' when"
                            + " calling asyncDeployFromDirectory");
        }

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

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
                "/tofu-maker/directory/deploy/async/{module_directory}",
                HttpMethod.POST,
                uriVariables,
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
     * <p><b>400</b> - Bad Request
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>202</b> - Accepted
     *
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuAsyncDestroyFromDirectoryRequest (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDestroyFromDirectory(
            String moduleDirectory,
            OpenTofuAsyncDestroyFromDirectoryRequest openTofuAsyncDestroyFromDirectoryRequest)
            throws RestClientException {
        asyncDestroyFromDirectoryWithHttpInfo(
                moduleDirectory, openTofuAsyncDestroyFromDirectoryRequest);
    }

    /**
     * async destroy resources via OpenTofu from the given directory.
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>202</b> - Accepted
     *
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuAsyncDestroyFromDirectoryRequest (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDestroyFromDirectoryWithHttpInfo(
            String moduleDirectory,
            OpenTofuAsyncDestroyFromDirectoryRequest openTofuAsyncDestroyFromDirectoryRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuAsyncDestroyFromDirectoryRequest;

        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'moduleDirectory' when calling"
                            + " asyncDestroyFromDirectory");
        }

        // verify the required parameter 'openTofuAsyncDestroyFromDirectoryRequest' is set
        if (openTofuAsyncDestroyFromDirectoryRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuAsyncDestroyFromDirectoryRequest' when"
                            + " calling asyncDestroyFromDirectory");
        }

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

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
                "/tofu-maker/directory/destroy/async/{module_directory}",
                HttpMethod.DELETE,
                uriVariables,
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
     * <p><b>400</b> - Bad Request
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>202</b> - Accepted
     *
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuAsyncModifyFromDirectoryRequest (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncModifyFromDirectory(
            String moduleDirectory,
            OpenTofuAsyncModifyFromDirectoryRequest openTofuAsyncModifyFromDirectoryRequest)
            throws RestClientException {
        asyncModifyFromDirectoryWithHttpInfo(
                moduleDirectory, openTofuAsyncModifyFromDirectoryRequest);
    }

    /**
     * async modify resources via OpenTofu from the given directory.
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>202</b> - Accepted
     *
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuAsyncModifyFromDirectoryRequest (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncModifyFromDirectoryWithHttpInfo(
            String moduleDirectory,
            OpenTofuAsyncModifyFromDirectoryRequest openTofuAsyncModifyFromDirectoryRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuAsyncModifyFromDirectoryRequest;

        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'moduleDirectory' when calling"
                            + " asyncModifyFromDirectory");
        }

        // verify the required parameter 'openTofuAsyncModifyFromDirectoryRequest' is set
        if (openTofuAsyncModifyFromDirectoryRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuAsyncModifyFromDirectoryRequest' when"
                            + " calling asyncModifyFromDirectory");
        }

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

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
                "/tofu-maker/directory/modify/async/{module_directory}",
                HttpMethod.POST,
                uriVariables,
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
     * <p><b>400</b> - Bad Request
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuDeployFromDirectoryRequest (required)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult deployFromDirectory(
            String moduleDirectory,
            OpenTofuDeployFromDirectoryRequest openTofuDeployFromDirectoryRequest)
            throws RestClientException {
        return deployFromDirectoryWithHttpInfo(moduleDirectory, openTofuDeployFromDirectoryRequest)
                .getBody();
    }

    /**
     * Deploy resources via OpenTofu from the given directory.
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuDeployFromDirectoryRequest (required)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> deployFromDirectoryWithHttpInfo(
            String moduleDirectory,
            OpenTofuDeployFromDirectoryRequest openTofuDeployFromDirectoryRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuDeployFromDirectoryRequest;

        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'moduleDirectory' when calling"
                            + " deployFromDirectory");
        }

        // verify the required parameter 'openTofuDeployFromDirectoryRequest' is set
        if (openTofuDeployFromDirectoryRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuDeployFromDirectoryRequest' when"
                            + " calling deployFromDirectory");
        }

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"*/*", "application/json"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<OpenTofuResult> localReturnType =
                new ParameterizedTypeReference<OpenTofuResult>() {};
        return apiClient.invokeAPI(
                "/tofu-maker/directory/deploy/{module_directory}",
                HttpMethod.POST,
                uriVariables,
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
     * <p><b>400</b> - Bad Request
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuDestroyFromDirectoryRequest (required)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult destroyFromDirectory(
            String moduleDirectory,
            OpenTofuDestroyFromDirectoryRequest openTofuDestroyFromDirectoryRequest)
            throws RestClientException {
        return destroyFromDirectoryWithHttpInfo(
                        moduleDirectory, openTofuDestroyFromDirectoryRequest)
                .getBody();
    }

    /**
     * Destroy the resources from the given directory.
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuDestroyFromDirectoryRequest (required)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> destroyFromDirectoryWithHttpInfo(
            String moduleDirectory,
            OpenTofuDestroyFromDirectoryRequest openTofuDestroyFromDirectoryRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuDestroyFromDirectoryRequest;

        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'moduleDirectory' when calling"
                            + " destroyFromDirectory");
        }

        // verify the required parameter 'openTofuDestroyFromDirectoryRequest' is set
        if (openTofuDestroyFromDirectoryRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuDestroyFromDirectoryRequest' when"
                            + " calling destroyFromDirectory");
        }

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"*/*", "application/json"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<OpenTofuResult> localReturnType =
                new ParameterizedTypeReference<OpenTofuResult>() {};
        return apiClient.invokeAPI(
                "/tofu-maker/directory/destroy/{module_directory}",
                HttpMethod.DELETE,
                uriVariables,
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
     * <p><b>400</b> - Bad Request
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuModifyFromDirectoryRequest (required)
     * @return OpenTofuResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuResult modifyFromDirectory(
            String moduleDirectory,
            OpenTofuModifyFromDirectoryRequest openTofuModifyFromDirectoryRequest)
            throws RestClientException {
        return modifyFromDirectoryWithHttpInfo(moduleDirectory, openTofuModifyFromDirectoryRequest)
                .getBody();
    }

    /**
     * Modify resources via OpenTofu from the given directory.
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuModifyFromDirectoryRequest (required)
     * @return ResponseEntity&lt;OpenTofuResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuResult> modifyFromDirectoryWithHttpInfo(
            String moduleDirectory,
            OpenTofuModifyFromDirectoryRequest openTofuModifyFromDirectoryRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuModifyFromDirectoryRequest;

        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'moduleDirectory' when calling"
                            + " modifyFromDirectory");
        }

        // verify the required parameter 'openTofuModifyFromDirectoryRequest' is set
        if (openTofuModifyFromDirectoryRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuModifyFromDirectoryRequest' when"
                            + " calling modifyFromDirectory");
        }

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"*/*", "application/json"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<OpenTofuResult> localReturnType =
                new ParameterizedTypeReference<OpenTofuResult>() {};
        return apiClient.invokeAPI(
                "/tofu-maker/directory/modify/{module_directory}",
                HttpMethod.POST,
                uriVariables,
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
     * <p><b>400</b> - Bad Request
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuPlanFromDirectoryRequest (required)
     * @return OpenTofuPlan
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuPlan plan(
            String moduleDirectory,
            OpenTofuPlanFromDirectoryRequest openTofuPlanFromDirectoryRequest)
            throws RestClientException {
        return planWithHttpInfo(moduleDirectory, openTofuPlanFromDirectoryRequest).getBody();
    }

    /**
     * Get OpenTofu Plan as JSON string from the given directory.
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param openTofuPlanFromDirectoryRequest (required)
     * @return ResponseEntity&lt;OpenTofuPlan&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuPlan> planWithHttpInfo(
            String moduleDirectory,
            OpenTofuPlanFromDirectoryRequest openTofuPlanFromDirectoryRequest)
            throws RestClientException {
        Object localVarPostBody = openTofuPlanFromDirectoryRequest;

        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'moduleDirectory' when calling plan");
        }

        // verify the required parameter 'openTofuPlanFromDirectoryRequest' is set
        if (openTofuPlanFromDirectoryRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'openTofuPlanFromDirectoryRequest' when calling"
                            + " plan");
        }

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"*/*", "application/json"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<OpenTofuPlan> localReturnType =
                new ParameterizedTypeReference<OpenTofuPlan>() {};
        return apiClient.invokeAPI(
                "/tofu-maker/directory/plan/{module_directory}",
                HttpMethod.POST,
                uriVariables,
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
     * <p><b>400</b> - Bad Request
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param opentofuVersion version of OpenTofu to execute the module files. (required)
     * @return OpenTofuValidationResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuValidationResult validateFromDirectory(
            String moduleDirectory, String opentofuVersion) throws RestClientException {
        return validateFromDirectoryWithHttpInfo(moduleDirectory, opentofuVersion).getBody();
    }

    /**
     * Validate the OpenTofu modules in the given directory.
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory directory name where the OpenTofu module files exist. (required)
     * @param opentofuVersion version of OpenTofu to execute the module files. (required)
     * @return ResponseEntity&lt;OpenTofuValidationResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuValidationResult> validateFromDirectoryWithHttpInfo(
            String moduleDirectory, String opentofuVersion) throws RestClientException {
        Object localVarPostBody = null;

        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'moduleDirectory' when calling"
                            + " validateFromDirectory");
        }

        // verify the required parameter 'opentofuVersion' is set
        if (opentofuVersion == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'opentofuVersion' when calling"
                            + " validateFromDirectory");
        }

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);
        uriVariables.put("opentofu_version", opentofuVersion);

        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"*/*", "application/json"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<OpenTofuValidationResult> localReturnType =
                new ParameterizedTypeReference<OpenTofuValidationResult>() {};
        return apiClient.invokeAPI(
                "/tofu-maker/directory/validate/{module_directory}/{opentofu_version}",
                HttpMethod.GET,
                uriVariables,
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

        final String[] localVarAccepts = {"*/*", "application/json"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
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
