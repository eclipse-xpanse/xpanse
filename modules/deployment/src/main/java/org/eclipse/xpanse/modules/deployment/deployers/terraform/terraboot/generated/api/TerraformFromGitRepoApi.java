package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.ApiClient;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.BaseApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformAsyncRequestWithScriptsGitRepo;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformPlan;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.TerraformRequestWithScriptsGitRepo;
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
        "org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api.TerraformFromGitRepoApi")
public class TerraformFromGitRepoApi extends BaseApi {

    public TerraformFromGitRepoApi() {
        super(new ApiClient());
    }

    @Autowired
    public TerraformFromGitRepoApi(ApiClient apiClient) {
        super(apiClient);
    }

    /**
     * async deploy resources via Terraform
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformAsyncRequestWithScriptsGitRepo (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDeployFromGitRepo(
            TerraformAsyncRequestWithScriptsGitRepo terraformAsyncRequestWithScriptsGitRepo)
            throws RestClientException {
        asyncDeployFromGitRepoWithHttpInfo(terraformAsyncRequestWithScriptsGitRepo);
    }

    /**
     * async deploy resources via Terraform
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformAsyncRequestWithScriptsGitRepo (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDeployFromGitRepoWithHttpInfo(
            TerraformAsyncRequestWithScriptsGitRepo terraformAsyncRequestWithScriptsGitRepo)
            throws RestClientException {
        Object localVarPostBody = terraformAsyncRequestWithScriptsGitRepo;

        // verify the required parameter 'terraformAsyncRequestWithScriptsGitRepo' is set
        if (terraformAsyncRequestWithScriptsGitRepo == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformAsyncRequestWithScriptsGitRepo' when"
                            + " calling asyncDeployFromGitRepo");
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
                "/terra-boot/git/deploy/async",
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
     * Async destroy the Terraform modules
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformAsyncRequestWithScriptsGitRepo (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDestroyFromGitRepo(
            TerraformAsyncRequestWithScriptsGitRepo terraformAsyncRequestWithScriptsGitRepo)
            throws RestClientException {
        asyncDestroyFromGitRepoWithHttpInfo(terraformAsyncRequestWithScriptsGitRepo);
    }

    /**
     * Async destroy the Terraform modules
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformAsyncRequestWithScriptsGitRepo (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDestroyFromGitRepoWithHttpInfo(
            TerraformAsyncRequestWithScriptsGitRepo terraformAsyncRequestWithScriptsGitRepo)
            throws RestClientException {
        Object localVarPostBody = terraformAsyncRequestWithScriptsGitRepo;

        // verify the required parameter 'terraformAsyncRequestWithScriptsGitRepo' is set
        if (terraformAsyncRequestWithScriptsGitRepo == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformAsyncRequestWithScriptsGitRepo' when"
                            + " calling asyncDestroyFromGitRepo");
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
                "/terra-boot/git/destroy/async",
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
     * async deploy resources via Terraform
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformAsyncRequestWithScriptsGitRepo (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncModifyFromGitRepo(
            TerraformAsyncRequestWithScriptsGitRepo terraformAsyncRequestWithScriptsGitRepo)
            throws RestClientException {
        asyncModifyFromGitRepoWithHttpInfo(terraformAsyncRequestWithScriptsGitRepo);
    }

    /**
     * async deploy resources via Terraform
     *
     * <p><b>202</b> - Accepted
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformAsyncRequestWithScriptsGitRepo (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncModifyFromGitRepoWithHttpInfo(
            TerraformAsyncRequestWithScriptsGitRepo terraformAsyncRequestWithScriptsGitRepo)
            throws RestClientException {
        Object localVarPostBody = terraformAsyncRequestWithScriptsGitRepo;

        // verify the required parameter 'terraformAsyncRequestWithScriptsGitRepo' is set
        if (terraformAsyncRequestWithScriptsGitRepo == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformAsyncRequestWithScriptsGitRepo' when"
                            + " calling asyncModifyFromGitRepo");
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
                "/terra-boot/git/modify/async",
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
     * Deploy resources via Terraform
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsGitRepo (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult deployFromGitRepo(
            TerraformRequestWithScriptsGitRepo terraformRequestWithScriptsGitRepo)
            throws RestClientException {
        return deployFromGitRepoWithHttpInfo(terraformRequestWithScriptsGitRepo).getBody();
    }

    /**
     * Deploy resources via Terraform
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsGitRepo (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> deployFromGitRepoWithHttpInfo(
            TerraformRequestWithScriptsGitRepo terraformRequestWithScriptsGitRepo)
            throws RestClientException {
        Object localVarPostBody = terraformRequestWithScriptsGitRepo;

        // verify the required parameter 'terraformRequestWithScriptsGitRepo' is set
        if (terraformRequestWithScriptsGitRepo == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformRequestWithScriptsGitRepo' when"
                            + " calling deployFromGitRepo");
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
                "/terra-boot/git/deploy",
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
     * Destroy resources via Terraform
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsGitRepo (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult destroyFromGitRepo(
            TerraformRequestWithScriptsGitRepo terraformRequestWithScriptsGitRepo)
            throws RestClientException {
        return destroyFromGitRepoWithHttpInfo(terraformRequestWithScriptsGitRepo).getBody();
    }

    /**
     * Destroy resources via Terraform
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsGitRepo (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> destroyFromGitRepoWithHttpInfo(
            TerraformRequestWithScriptsGitRepo terraformRequestWithScriptsGitRepo)
            throws RestClientException {
        Object localVarPostBody = terraformRequestWithScriptsGitRepo;

        // verify the required parameter 'terraformRequestWithScriptsGitRepo' is set
        if (terraformRequestWithScriptsGitRepo == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformRequestWithScriptsGitRepo' when"
                            + " calling destroyFromGitRepo");
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
                "/terra-boot/git/destroy",
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
     * Modify resources via Terraform
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsGitRepo (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult modifyFromGitRepo(
            TerraformRequestWithScriptsGitRepo terraformRequestWithScriptsGitRepo)
            throws RestClientException {
        return modifyFromGitRepoWithHttpInfo(terraformRequestWithScriptsGitRepo).getBody();
    }

    /**
     * Modify resources via Terraform
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsGitRepo (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> modifyFromGitRepoWithHttpInfo(
            TerraformRequestWithScriptsGitRepo terraformRequestWithScriptsGitRepo)
            throws RestClientException {
        Object localVarPostBody = terraformRequestWithScriptsGitRepo;

        // verify the required parameter 'terraformRequestWithScriptsGitRepo' is set
        if (terraformRequestWithScriptsGitRepo == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformRequestWithScriptsGitRepo' when"
                            + " calling modifyFromGitRepo");
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
                "/terra-boot/git/modify",
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
     * Get Terraform Plan as JSON string from the list of script files provided
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsGitRepo (required)
     * @return TerraformPlan
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformPlan planFromGitRepo(
            TerraformRequestWithScriptsGitRepo terraformRequestWithScriptsGitRepo)
            throws RestClientException {
        return planFromGitRepoWithHttpInfo(terraformRequestWithScriptsGitRepo).getBody();
    }

    /**
     * Get Terraform Plan as JSON string from the list of script files provided
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsGitRepo (required)
     * @return ResponseEntity&lt;TerraformPlan&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformPlan> planFromGitRepoWithHttpInfo(
            TerraformRequestWithScriptsGitRepo terraformRequestWithScriptsGitRepo)
            throws RestClientException {
        Object localVarPostBody = terraformRequestWithScriptsGitRepo;

        // verify the required parameter 'terraformRequestWithScriptsGitRepo' is set
        if (terraformRequestWithScriptsGitRepo == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformRequestWithScriptsGitRepo' when"
                            + " calling planFromGitRepo");
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
                "/terra-boot/git/plan",
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
     * Deploy resources via Terraform
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsGitRepo (required)
     * @return TerraformValidationResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformValidationResult validateScriptsFromGitRepo(
            TerraformRequestWithScriptsGitRepo terraformRequestWithScriptsGitRepo)
            throws RestClientException {
        return validateScriptsFromGitRepoWithHttpInfo(terraformRequestWithScriptsGitRepo).getBody();
    }

    /**
     * Deploy resources via Terraform
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param terraformRequestWithScriptsGitRepo (required)
     * @return ResponseEntity&lt;TerraformValidationResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformValidationResult> validateScriptsFromGitRepoWithHttpInfo(
            TerraformRequestWithScriptsGitRepo terraformRequestWithScriptsGitRepo)
            throws RestClientException {
        Object localVarPostBody = terraformRequestWithScriptsGitRepo;

        // verify the required parameter 'terraformRequestWithScriptsGitRepo' is set
        if (terraformRequestWithScriptsGitRepo == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformRequestWithScriptsGitRepo' when"
                            + " calling validateScriptsFromGitRepo");
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
                "/terra-boot/git/validate",
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
