package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.ApiClient;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.BaseApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformAsyncRequestWithScriptsDirectory;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformPlan;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformRequestWithScriptsDirectory;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformResult;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformValidationResult;
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
        "org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api.TerraformFromDirectoryApi")
public class TerraformFromDirectoryApi extends BaseApi {

    public TerraformFromDirectoryApi() {
        super(new ApiClient());
    }

    @Autowired
    public TerraformFromDirectoryApi(ApiClient apiClient) {
        super(apiClient);
    }

    /**
     * async deploy resources via Terraform from the given directory.
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformAsyncRequestWithScriptsDirectory (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDeployFromDirectory(
            TerraformAsyncRequestWithScriptsDirectory terraformAsyncRequestWithScriptsDirectory)
            throws RestClientException {
        asyncDeployFromDirectoryWithHttpInfo(terraformAsyncRequestWithScriptsDirectory);
    }

    /**
     * async deploy resources via Terraform from the given directory.
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformAsyncRequestWithScriptsDirectory (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDeployFromDirectoryWithHttpInfo(
            TerraformAsyncRequestWithScriptsDirectory terraformAsyncRequestWithScriptsDirectory)
            throws RestClientException {
        Object localVarPostBody = terraformAsyncRequestWithScriptsDirectory;

        // verify the required parameter 'terraformAsyncRequestWithScriptsDirectory' is set
        if (terraformAsyncRequestWithScriptsDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformAsyncRequestWithScriptsDirectory'"
                            + " when calling asyncDeployFromDirectory");
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
                "/terra-boot/directory/deploy/async",
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
     * async destroy resources via Terraform from the given directory.
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformAsyncRequestWithScriptsDirectory (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDestroyFromDirectory(
            TerraformAsyncRequestWithScriptsDirectory terraformAsyncRequestWithScriptsDirectory)
            throws RestClientException {
        asyncDestroyFromDirectoryWithHttpInfo(terraformAsyncRequestWithScriptsDirectory);
    }

    /**
     * async destroy resources via Terraform from the given directory.
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformAsyncRequestWithScriptsDirectory (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDestroyFromDirectoryWithHttpInfo(
            TerraformAsyncRequestWithScriptsDirectory terraformAsyncRequestWithScriptsDirectory)
            throws RestClientException {
        Object localVarPostBody = terraformAsyncRequestWithScriptsDirectory;

        // verify the required parameter 'terraformAsyncRequestWithScriptsDirectory' is set
        if (terraformAsyncRequestWithScriptsDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformAsyncRequestWithScriptsDirectory'"
                            + " when calling asyncDestroyFromDirectory");
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
                "/terra-boot/directory/destroy/async",
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
     * async modify resources via Terraform from the given directory.
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformAsyncRequestWithScriptsDirectory (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncModifyFromDirectory(
            TerraformAsyncRequestWithScriptsDirectory terraformAsyncRequestWithScriptsDirectory)
            throws RestClientException {
        asyncModifyFromDirectoryWithHttpInfo(terraformAsyncRequestWithScriptsDirectory);
    }

    /**
     * async modify resources via Terraform from the given directory.
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformAsyncRequestWithScriptsDirectory (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncModifyFromDirectoryWithHttpInfo(
            TerraformAsyncRequestWithScriptsDirectory terraformAsyncRequestWithScriptsDirectory)
            throws RestClientException {
        Object localVarPostBody = terraformAsyncRequestWithScriptsDirectory;

        // verify the required parameter 'terraformAsyncRequestWithScriptsDirectory' is set
        if (terraformAsyncRequestWithScriptsDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformAsyncRequestWithScriptsDirectory'"
                            + " when calling asyncModifyFromDirectory");
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
                "/terra-boot/directory/modify/async",
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
     * Deploy resources via Terraform from the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsDirectory (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult deployFromDirectory(
            TerraformRequestWithScriptsDirectory terraformRequestWithScriptsDirectory)
            throws RestClientException {
        return deployFromDirectoryWithHttpInfo(terraformRequestWithScriptsDirectory).getBody();
    }

    /**
     * Deploy resources via Terraform from the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsDirectory (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> deployFromDirectoryWithHttpInfo(
            TerraformRequestWithScriptsDirectory terraformRequestWithScriptsDirectory)
            throws RestClientException {
        Object localVarPostBody = terraformRequestWithScriptsDirectory;

        // verify the required parameter 'terraformRequestWithScriptsDirectory' is set
        if (terraformRequestWithScriptsDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformRequestWithScriptsDirectory' when"
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

        ParameterizedTypeReference<TerraformResult> localReturnType =
                new ParameterizedTypeReference<TerraformResult>() {};
        return apiClient.invokeAPI(
                "/terra-boot/directory/deploy",
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
     * @param terraformRequestWithScriptsDirectory (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult destroyFromDirectory(
            TerraformRequestWithScriptsDirectory terraformRequestWithScriptsDirectory)
            throws RestClientException {
        return destroyFromDirectoryWithHttpInfo(terraformRequestWithScriptsDirectory).getBody();
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
     * @param terraformRequestWithScriptsDirectory (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> destroyFromDirectoryWithHttpInfo(
            TerraformRequestWithScriptsDirectory terraformRequestWithScriptsDirectory)
            throws RestClientException {
        Object localVarPostBody = terraformRequestWithScriptsDirectory;

        // verify the required parameter 'terraformRequestWithScriptsDirectory' is set
        if (terraformRequestWithScriptsDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformRequestWithScriptsDirectory' when"
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

        ParameterizedTypeReference<TerraformResult> localReturnType =
                new ParameterizedTypeReference<TerraformResult>() {};
        return apiClient.invokeAPI(
                "/terra-boot/directory/destroy",
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
     * Modify resources via Terraform from the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsDirectory (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult modifyFromDirectory(
            TerraformRequestWithScriptsDirectory terraformRequestWithScriptsDirectory)
            throws RestClientException {
        return modifyFromDirectoryWithHttpInfo(terraformRequestWithScriptsDirectory).getBody();
    }

    /**
     * Modify resources via Terraform from the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsDirectory (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> modifyFromDirectoryWithHttpInfo(
            TerraformRequestWithScriptsDirectory terraformRequestWithScriptsDirectory)
            throws RestClientException {
        Object localVarPostBody = terraformRequestWithScriptsDirectory;

        // verify the required parameter 'terraformRequestWithScriptsDirectory' is set
        if (terraformRequestWithScriptsDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformRequestWithScriptsDirectory' when"
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

        ParameterizedTypeReference<TerraformResult> localReturnType =
                new ParameterizedTypeReference<TerraformResult>() {};
        return apiClient.invokeAPI(
                "/terra-boot/directory/modify",
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
     * Get Terraform Plan as JSON string from the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsDirectory (required)
     * @return TerraformPlan
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformPlan plan(
            TerraformRequestWithScriptsDirectory terraformRequestWithScriptsDirectory)
            throws RestClientException {
        return planWithHttpInfo(terraformRequestWithScriptsDirectory).getBody();
    }

    /**
     * Get Terraform Plan as JSON string from the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsDirectory (required)
     * @return ResponseEntity&lt;TerraformPlan&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformPlan> planWithHttpInfo(
            TerraformRequestWithScriptsDirectory terraformRequestWithScriptsDirectory)
            throws RestClientException {
        Object localVarPostBody = terraformRequestWithScriptsDirectory;

        // verify the required parameter 'terraformRequestWithScriptsDirectory' is set
        if (terraformRequestWithScriptsDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformRequestWithScriptsDirectory' when"
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

        ParameterizedTypeReference<TerraformPlan> localReturnType =
                new ParameterizedTypeReference<TerraformPlan>() {};
        return apiClient.invokeAPI(
                "/terra-boot/directory/plan",
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
     * Validate the Terraform modules in the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsDirectory (required)
     * @return TerraformValidationResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformValidationResult validateFromDirectory(
            TerraformRequestWithScriptsDirectory terraformRequestWithScriptsDirectory)
            throws RestClientException {
        return validateFromDirectoryWithHttpInfo(terraformRequestWithScriptsDirectory).getBody();
    }

    /**
     * Validate the Terraform modules in the given directory.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsDirectory (required)
     * @return ResponseEntity&lt;TerraformValidationResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformValidationResult> validateFromDirectoryWithHttpInfo(
            TerraformRequestWithScriptsDirectory terraformRequestWithScriptsDirectory)
            throws RestClientException {
        Object localVarPostBody = terraformRequestWithScriptsDirectory;

        // verify the required parameter 'terraformRequestWithScriptsDirectory' is set
        if (terraformRequestWithScriptsDirectory == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformRequestWithScriptsDirectory' when"
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

        ParameterizedTypeReference<TerraformValidationResult> localReturnType =
                new ParameterizedTypeReference<TerraformValidationResult>() {};
        return apiClient.invokeAPI(
                "/terra-boot/directory/validate",
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
