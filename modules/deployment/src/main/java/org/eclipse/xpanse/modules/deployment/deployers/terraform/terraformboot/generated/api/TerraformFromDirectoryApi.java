package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.ApiClient;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.BaseApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncDeployFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncDestroyFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncModifyFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformDeployFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformDestroyFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformModifyFromDirectoryRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformPlan;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformPlanFromDirectoryRequest;
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

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator " +
        "version: 7.10.0")
@Component("org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api" +
        ".TerraformFromDirectoryApi")
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
     * <p><b>502</b> - Bad Gateway
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     *
     * @param moduleDirectory                          directory name where the Terraform module files exist. (required)
     * @param terraformAsyncDeployFromDirectoryRequest (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDeployFromDirectory(String moduleDirectory,
                                         TerraformAsyncDeployFromDirectoryRequest terraformAsyncDeployFromDirectoryRequest)
            throws RestClientException {
        asyncDeployFromDirectoryWithHttpInfo(moduleDirectory, terraformAsyncDeployFromDirectoryRequest);
    }

    /**
     * async deploy resources via Terraform from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     *
     * @param moduleDirectory                          directory name where the Terraform module files exist. (required)
     * @param terraformAsyncDeployFromDirectoryRequest (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDeployFromDirectoryWithHttpInfo(String moduleDirectory,
                                                                     TerraformAsyncDeployFromDirectoryRequest terraformAsyncDeployFromDirectoryRequest)
            throws RestClientException {
        Object localVarPostBody = terraformAsyncDeployFromDirectoryRequest;

        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'moduleDirectory' when calling asyncDeployFromDirectory");
        }

        // verify the required parameter 'terraformAsyncDeployFromDirectoryRequest' is set
        if (terraformAsyncDeployFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformAsyncDeployFromDirectoryRequest' when calling " +
                            "asyncDeployFromDirectory");
        }

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"*/*"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<Void> localReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/terraform-boot/directory/deploy/async/{module_directory}", HttpMethod.POST,
                uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams,
                localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }

    /**
     * async destroy resources via Terraform from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     *
     * @param moduleDirectory                           directory name where the Terraform module files exist.
     *                                                  (required)
     * @param terraformAsyncDestroyFromDirectoryRequest (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDestroyFromDirectory(String moduleDirectory,
                                          TerraformAsyncDestroyFromDirectoryRequest terraformAsyncDestroyFromDirectoryRequest)
            throws RestClientException {
        asyncDestroyFromDirectoryWithHttpInfo(moduleDirectory, terraformAsyncDestroyFromDirectoryRequest);
    }

    /**
     * async destroy resources via Terraform from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     *
     * @param moduleDirectory                           directory name where the Terraform module files exist.
     *                                                  (required)
     * @param terraformAsyncDestroyFromDirectoryRequest (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDestroyFromDirectoryWithHttpInfo(String moduleDirectory,
                                                                      TerraformAsyncDestroyFromDirectoryRequest terraformAsyncDestroyFromDirectoryRequest)
            throws RestClientException {
        Object localVarPostBody = terraformAsyncDestroyFromDirectoryRequest;

        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'moduleDirectory' when calling asyncDestroyFromDirectory");
        }

        // verify the required parameter 'terraformAsyncDestroyFromDirectoryRequest' is set
        if (terraformAsyncDestroyFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformAsyncDestroyFromDirectoryRequest' when calling " +
                            "asyncDestroyFromDirectory");
        }

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"*/*"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<Void> localReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/terraform-boot/directory/destroy/async/{module_directory}", HttpMethod.DELETE,
                uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams,
                localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }

    /**
     * async modify resources via Terraform from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     *
     * @param moduleDirectory                          directory name where the Terraform module files exist. (required)
     * @param terraformAsyncModifyFromDirectoryRequest (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncModifyFromDirectory(String moduleDirectory,
                                         TerraformAsyncModifyFromDirectoryRequest terraformAsyncModifyFromDirectoryRequest)
            throws RestClientException {
        asyncModifyFromDirectoryWithHttpInfo(moduleDirectory, terraformAsyncModifyFromDirectoryRequest);
    }

    /**
     * async modify resources via Terraform from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     *
     * @param moduleDirectory                          directory name where the Terraform module files exist. (required)
     * @param terraformAsyncModifyFromDirectoryRequest (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncModifyFromDirectoryWithHttpInfo(String moduleDirectory,
                                                                     TerraformAsyncModifyFromDirectoryRequest terraformAsyncModifyFromDirectoryRequest)
            throws RestClientException {
        Object localVarPostBody = terraformAsyncModifyFromDirectoryRequest;

        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'moduleDirectory' when calling asyncModifyFromDirectory");
        }

        // verify the required parameter 'terraformAsyncModifyFromDirectoryRequest' is set
        if (terraformAsyncModifyFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformAsyncModifyFromDirectoryRequest' when calling " +
                            "asyncModifyFromDirectory");
        }

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"*/*"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<Void> localReturnType = new ParameterizedTypeReference<Void>() {
        };
        return apiClient.invokeAPI("/terraform-boot/directory/modify/async/{module_directory}", HttpMethod.POST,
                uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams,
                localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }

    /**
     * Deploy resources via Terraform from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory                     directory name where the Terraform module files exist. (required)
     * @param terraformDeployFromDirectoryRequest (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult deployFromDirectory(String moduleDirectory,
                                               TerraformDeployFromDirectoryRequest terraformDeployFromDirectoryRequest)
            throws RestClientException {
        return deployFromDirectoryWithHttpInfo(moduleDirectory, terraformDeployFromDirectoryRequest).getBody();
    }

    /**
     * Deploy resources via Terraform from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory                     directory name where the Terraform module files exist. (required)
     * @param terraformDeployFromDirectoryRequest (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> deployFromDirectoryWithHttpInfo(String moduleDirectory,
                                                                           TerraformDeployFromDirectoryRequest terraformDeployFromDirectoryRequest)
            throws RestClientException {
        Object localVarPostBody = terraformDeployFromDirectoryRequest;

        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'moduleDirectory' when calling deployFromDirectory");
        }

        // verify the required parameter 'terraformDeployFromDirectoryRequest' is set
        if (terraformDeployFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformDeployFromDirectoryRequest' when calling " +
                            "deployFromDirectory");
        }

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"*/*", "application/json"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<TerraformResult> localReturnType =
                new ParameterizedTypeReference<TerraformResult>() {
                };
        return apiClient.invokeAPI("/terraform-boot/directory/deploy/{module_directory}", HttpMethod.POST, uriVariables,
                localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams,
                localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }

    /**
     * Destroy the resources from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory                      directory name where the Terraform module files exist. (required)
     * @param terraformDestroyFromDirectoryRequest (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult destroyFromDirectory(String moduleDirectory,
                                                TerraformDestroyFromDirectoryRequest terraformDestroyFromDirectoryRequest)
            throws RestClientException {
        return destroyFromDirectoryWithHttpInfo(moduleDirectory, terraformDestroyFromDirectoryRequest).getBody();
    }

    /**
     * Destroy the resources from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory                      directory name where the Terraform module files exist. (required)
     * @param terraformDestroyFromDirectoryRequest (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> destroyFromDirectoryWithHttpInfo(String moduleDirectory,
                                                                            TerraformDestroyFromDirectoryRequest terraformDestroyFromDirectoryRequest)
            throws RestClientException {
        Object localVarPostBody = terraformDestroyFromDirectoryRequest;

        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'moduleDirectory' when calling destroyFromDirectory");
        }

        // verify the required parameter 'terraformDestroyFromDirectoryRequest' is set
        if (terraformDestroyFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformDestroyFromDirectoryRequest' when calling " +
                            "destroyFromDirectory");
        }

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"*/*", "application/json"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<TerraformResult> localReturnType =
                new ParameterizedTypeReference<TerraformResult>() {
                };
        return apiClient.invokeAPI("/terraform-boot/directory/destroy/{module_directory}", HttpMethod.DELETE,
                uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams,
                localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }

    /**
     * Modify resources via Terraform from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory                     directory name where the Terraform module files exist. (required)
     * @param terraformModifyFromDirectoryRequest (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult modifyFromDirectory(String moduleDirectory,
                                               TerraformModifyFromDirectoryRequest terraformModifyFromDirectoryRequest)
            throws RestClientException {
        return modifyFromDirectoryWithHttpInfo(moduleDirectory, terraformModifyFromDirectoryRequest).getBody();
    }

    /**
     * Modify resources via Terraform from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory                     directory name where the Terraform module files exist. (required)
     * @param terraformModifyFromDirectoryRequest (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> modifyFromDirectoryWithHttpInfo(String moduleDirectory,
                                                                           TerraformModifyFromDirectoryRequest terraformModifyFromDirectoryRequest)
            throws RestClientException {
        Object localVarPostBody = terraformModifyFromDirectoryRequest;

        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'moduleDirectory' when calling modifyFromDirectory");
        }

        // verify the required parameter 'terraformModifyFromDirectoryRequest' is set
        if (terraformModifyFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformModifyFromDirectoryRequest' when calling " +
                            "modifyFromDirectory");
        }

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"*/*", "application/json"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<TerraformResult> localReturnType =
                new ParameterizedTypeReference<TerraformResult>() {
                };
        return apiClient.invokeAPI("/terraform-boot/directory/modify/{module_directory}", HttpMethod.POST, uriVariables,
                localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams,
                localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }

    /**
     * Get Terraform Plan as JSON string from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory                   directory name where the Terraform module files exist. (required)
     * @param terraformPlanFromDirectoryRequest (required)
     * @param xCustomRequestId                  (optional)
     * @return TerraformPlan
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformPlan plan(String moduleDirectory,
                              TerraformPlanFromDirectoryRequest terraformPlanFromDirectoryRequest,
                              UUID xCustomRequestId) throws RestClientException {
        return planWithHttpInfo(moduleDirectory, terraformPlanFromDirectoryRequest, xCustomRequestId).getBody();
    }

    /**
     * Get Terraform Plan as JSON string from the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory                   directory name where the Terraform module files exist. (required)
     * @param terraformPlanFromDirectoryRequest (required)
     * @param xCustomRequestId                  (optional)
     * @return ResponseEntity&lt;TerraformPlan&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformPlan> planWithHttpInfo(String moduleDirectory,
                                                          TerraformPlanFromDirectoryRequest terraformPlanFromDirectoryRequest,
                                                          UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = terraformPlanFromDirectoryRequest;

        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'moduleDirectory' when calling plan");
        }

        // verify the required parameter 'terraformPlanFromDirectoryRequest' is set
        if (terraformPlanFromDirectoryRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformPlanFromDirectoryRequest' when calling plan");
        }

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        if (xCustomRequestId != null) {
            localVarHeaderParams.add("X-Custom-RequestId", apiClient.parameterToString(xCustomRequestId));
        }

        final String[] localVarAccepts = {"*/*", "application/json"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {"application/json"};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<TerraformPlan> localReturnType = new ParameterizedTypeReference<TerraformPlan>() {
        };
        return apiClient.invokeAPI("/terraform-boot/directory/plan/{module_directory}", HttpMethod.POST, uriVariables,
                localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams,
                localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }

    /**
     * Validate the Terraform modules in the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory  directory name where the Terraform module files exist. (required)
     * @param terraformVersion version of Terraform to execute the module files. (required)
     * @return TerraformValidationResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformValidationResult validateFromDirectory(String moduleDirectory, String terraformVersion)
            throws RestClientException {
        return validateFromDirectoryWithHttpInfo(moduleDirectory, terraformVersion).getBody();
    }

    /**
     * Validate the Terraform modules in the given directory.
     * <p><b>502</b> - Bad Gateway
     * <p><b>400</b> - Bad Request
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     *
     * @param moduleDirectory  directory name where the Terraform module files exist. (required)
     * @param terraformVersion version of Terraform to execute the module files. (required)
     * @return ResponseEntity&lt;TerraformValidationResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformValidationResult> validateFromDirectoryWithHttpInfo(String moduleDirectory,
                                                                                       String terraformVersion)
            throws RestClientException {
        Object localVarPostBody = null;

        // verify the required parameter 'moduleDirectory' is set
        if (moduleDirectory == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'moduleDirectory' when calling validateFromDirectory");
        }

        // verify the required parameter 'terraformVersion' is set
        if (terraformVersion == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'terraformVersion' when calling validateFromDirectory");
        }

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("module_directory", moduleDirectory);
        uriVariables.put("terraform_version", terraformVersion);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"*/*", "application/json"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<TerraformValidationResult> localReturnType =
                new ParameterizedTypeReference<TerraformValidationResult>() {
                };
        return apiClient.invokeAPI("/terraform-boot/directory/validate/{module_directory}/{terraform_version}",
                HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams,
                localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames,
                localReturnType);
    }

    @Override
    public <T> ResponseEntity<T> invokeAPI(String url, HttpMethod method, Object request,
                                           ParameterizedTypeReference<T> returnType) throws RestClientException {
        String localVarPath = url.replace(apiClient.getBasePath(), "");
        Object localVarPostBody = request;

        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"*/*", "application/json"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        return apiClient.invokeAPI(localVarPath, method, uriVariables, localVarQueryParams, localVarPostBody,
                localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType,
                localVarAuthNames, returnType);
    }
}
