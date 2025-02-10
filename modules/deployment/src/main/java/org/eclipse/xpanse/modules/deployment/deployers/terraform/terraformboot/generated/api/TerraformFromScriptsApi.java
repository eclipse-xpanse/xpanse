package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.ApiClient;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.BaseApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncDeployFromScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncDestroyFromScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncModifyFromScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformDeployWithScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformDestroyWithScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformModifyWithScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformPlan;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformPlanWithScriptsRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformResult;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformValidationResult;
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
        "org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromScriptsApi")
public class TerraformFromScriptsApi extends BaseApi {

    public TerraformFromScriptsApi() {
        super(new ApiClient());
    }

    @Autowired
    public TerraformFromScriptsApi(ApiClient apiClient) {
        super(apiClient);
    }

    /**
     * async deploy resources via Terraform
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>202</b> - Accepted
     *
     * @param terraformAsyncDeployFromScriptsRequest (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDeployWithScripts(
            TerraformAsyncDeployFromScriptsRequest terraformAsyncDeployFromScriptsRequest)
            throws RestClientException {
        asyncDeployWithScriptsWithHttpInfo(terraformAsyncDeployFromScriptsRequest);
    }

    /**
     * async deploy resources via Terraform
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>202</b> - Accepted
     *
     * @param terraformAsyncDeployFromScriptsRequest (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDeployWithScriptsWithHttpInfo(
            TerraformAsyncDeployFromScriptsRequest terraformAsyncDeployFromScriptsRequest)
            throws RestClientException {
        Object localVarPostBody = terraformAsyncDeployFromScriptsRequest;

        // verify the required parameter 'terraformAsyncDeployFromScriptsRequest' is set
        if (terraformAsyncDeployFromScriptsRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformAsyncDeployFromScriptsRequest' when"
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
                "/terraform-boot/scripts/deploy/async",
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
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>202</b> - Accepted
     *
     * @param terraformAsyncDestroyFromScriptsRequest (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDestroyWithScripts(
            TerraformAsyncDestroyFromScriptsRequest terraformAsyncDestroyFromScriptsRequest)
            throws RestClientException {
        asyncDestroyWithScriptsWithHttpInfo(terraformAsyncDestroyFromScriptsRequest);
    }

    /**
     * Async destroy the Terraform modules
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>202</b> - Accepted
     *
     * @param terraformAsyncDestroyFromScriptsRequest (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDestroyWithScriptsWithHttpInfo(
            TerraformAsyncDestroyFromScriptsRequest terraformAsyncDestroyFromScriptsRequest)
            throws RestClientException {
        Object localVarPostBody = terraformAsyncDestroyFromScriptsRequest;

        // verify the required parameter 'terraformAsyncDestroyFromScriptsRequest' is set
        if (terraformAsyncDestroyFromScriptsRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformAsyncDestroyFromScriptsRequest' when"
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
                "/terraform-boot/scripts/destroy/async",
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
     * async modify resources via Terraform
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>202</b> - Accepted
     *
     * @param terraformAsyncModifyFromScriptsRequest (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncModifyWithScripts(
            TerraformAsyncModifyFromScriptsRequest terraformAsyncModifyFromScriptsRequest)
            throws RestClientException {
        asyncModifyWithScriptsWithHttpInfo(terraformAsyncModifyFromScriptsRequest);
    }

    /**
     * async modify resources via Terraform
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>202</b> - Accepted
     *
     * @param terraformAsyncModifyFromScriptsRequest (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncModifyWithScriptsWithHttpInfo(
            TerraformAsyncModifyFromScriptsRequest terraformAsyncModifyFromScriptsRequest)
            throws RestClientException {
        Object localVarPostBody = terraformAsyncModifyFromScriptsRequest;

        // verify the required parameter 'terraformAsyncModifyFromScriptsRequest' is set
        if (terraformAsyncModifyFromScriptsRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformAsyncModifyFromScriptsRequest' when"
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
                "/terraform-boot/scripts/modify/async",
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
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param terraformDeployWithScriptsRequest (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult deployWithScripts(
            TerraformDeployWithScriptsRequest terraformDeployWithScriptsRequest)
            throws RestClientException {
        return deployWithScriptsWithHttpInfo(terraformDeployWithScriptsRequest).getBody();
    }

    /**
     * Deploy resources via Terraform
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param terraformDeployWithScriptsRequest (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> deployWithScriptsWithHttpInfo(
            TerraformDeployWithScriptsRequest terraformDeployWithScriptsRequest)
            throws RestClientException {
        Object localVarPostBody = terraformDeployWithScriptsRequest;

        // verify the required parameter 'terraformDeployWithScriptsRequest' is set
        if (terraformDeployWithScriptsRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformDeployWithScriptsRequest' when"
                            + " calling deployWithScripts");
        }

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

        ParameterizedTypeReference<TerraformResult> localReturnType =
                new ParameterizedTypeReference<TerraformResult>() {};
        return apiClient.invokeAPI(
                "/terraform-boot/scripts/deploy",
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
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param terraformDestroyWithScriptsRequest (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult destroyWithScripts(
            TerraformDestroyWithScriptsRequest terraformDestroyWithScriptsRequest)
            throws RestClientException {
        return destroyWithScriptsWithHttpInfo(terraformDestroyWithScriptsRequest).getBody();
    }

    /**
     * Destroy resources via Terraform
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param terraformDestroyWithScriptsRequest (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> destroyWithScriptsWithHttpInfo(
            TerraformDestroyWithScriptsRequest terraformDestroyWithScriptsRequest)
            throws RestClientException {
        Object localVarPostBody = terraformDestroyWithScriptsRequest;

        // verify the required parameter 'terraformDestroyWithScriptsRequest' is set
        if (terraformDestroyWithScriptsRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformDestroyWithScriptsRequest' when"
                            + " calling destroyWithScripts");
        }

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

        ParameterizedTypeReference<TerraformResult> localReturnType =
                new ParameterizedTypeReference<TerraformResult>() {};
        return apiClient.invokeAPI(
                "/terraform-boot/scripts/destroy",
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
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param terraformModifyWithScriptsRequest (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult modifyWithScripts(
            TerraformModifyWithScriptsRequest terraformModifyWithScriptsRequest)
            throws RestClientException {
        return modifyWithScriptsWithHttpInfo(terraformModifyWithScriptsRequest).getBody();
    }

    /**
     * Modify resources via Terraform
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param terraformModifyWithScriptsRequest (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> modifyWithScriptsWithHttpInfo(
            TerraformModifyWithScriptsRequest terraformModifyWithScriptsRequest)
            throws RestClientException {
        Object localVarPostBody = terraformModifyWithScriptsRequest;

        // verify the required parameter 'terraformModifyWithScriptsRequest' is set
        if (terraformModifyWithScriptsRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformModifyWithScriptsRequest' when"
                            + " calling modifyWithScripts");
        }

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

        ParameterizedTypeReference<TerraformResult> localReturnType =
                new ParameterizedTypeReference<TerraformResult>() {};
        return apiClient.invokeAPI(
                "/terraform-boot/scripts/modify",
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
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param terraformPlanWithScriptsRequest (required)
     * @return TerraformPlan
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformPlan planWithScripts(
            TerraformPlanWithScriptsRequest terraformPlanWithScriptsRequest)
            throws RestClientException {
        return planWithScriptsWithHttpInfo(terraformPlanWithScriptsRequest).getBody();
    }

    /**
     * Get Terraform Plan as JSON string from the list of script files provided
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param terraformPlanWithScriptsRequest (required)
     * @return ResponseEntity&lt;TerraformPlan&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformPlan> planWithScriptsWithHttpInfo(
            TerraformPlanWithScriptsRequest terraformPlanWithScriptsRequest)
            throws RestClientException {
        Object localVarPostBody = terraformPlanWithScriptsRequest;

        // verify the required parameter 'terraformPlanWithScriptsRequest' is set
        if (terraformPlanWithScriptsRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformPlanWithScriptsRequest' when calling"
                            + " planWithScripts");
        }

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

        ParameterizedTypeReference<TerraformPlan> localReturnType =
                new ParameterizedTypeReference<TerraformPlan>() {};
        return apiClient.invokeAPI(
                "/terraform-boot/scripts/plan",
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
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param terraformDeployWithScriptsRequest (required)
     * @return TerraformValidationResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformValidationResult validateWithScripts(
            TerraformDeployWithScriptsRequest terraformDeployWithScriptsRequest)
            throws RestClientException {
        return validateWithScriptsWithHttpInfo(terraformDeployWithScriptsRequest).getBody();
    }

    /**
     * Deploy resources via Terraform
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param terraformDeployWithScriptsRequest (required)
     * @return ResponseEntity&lt;TerraformValidationResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformValidationResult> validateWithScriptsWithHttpInfo(
            TerraformDeployWithScriptsRequest terraformDeployWithScriptsRequest)
            throws RestClientException {
        Object localVarPostBody = terraformDeployWithScriptsRequest;

        // verify the required parameter 'terraformDeployWithScriptsRequest' is set
        if (terraformDeployWithScriptsRequest == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformDeployWithScriptsRequest' when"
                            + " calling validateWithScripts");
        }

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

        ParameterizedTypeReference<TerraformValidationResult> localReturnType =
                new ParameterizedTypeReference<TerraformValidationResult>() {};
        return apiClient.invokeAPI(
                "/terraform-boot/scripts/validate",
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

        final String[] localVarAccepts = {"*/*", "application/json"};
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
