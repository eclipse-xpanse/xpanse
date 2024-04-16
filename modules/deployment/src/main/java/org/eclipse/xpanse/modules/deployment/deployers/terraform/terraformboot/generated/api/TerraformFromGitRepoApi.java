package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.ApiClient;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncDeployFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncDestroyFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncModifyFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformDeployFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformDestroyFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformModifyFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformPlan;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformPlanFromGitRepoRequest;
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

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.4.0")
@Component("org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.TerraformFromGitRepoApi")
public class TerraformFromGitRepoApi {
    private ApiClient apiClient;

    public TerraformFromGitRepoApi() {
        this(new ApiClient());
    }

    @Autowired
    public TerraformFromGitRepoApi(ApiClient apiClient) {
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
     * async deploy resources via Terraform
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>202</b> - Accepted
     * @param terraformAsyncDeployFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDeployFromGitRepo(TerraformAsyncDeployFromGitRepoRequest terraformAsyncDeployFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        asyncDeployFromGitRepoWithHttpInfo(terraformAsyncDeployFromGitRepoRequest, xCustomRequestId);
    }

    /**
     * 
     * async deploy resources via Terraform
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>202</b> - Accepted
     * @param terraformAsyncDeployFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDeployFromGitRepoWithHttpInfo(TerraformAsyncDeployFromGitRepoRequest terraformAsyncDeployFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = terraformAsyncDeployFromGitRepoRequest;
        
        // verify the required parameter 'terraformAsyncDeployFromGitRepoRequest' is set
        if (terraformAsyncDeployFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformAsyncDeployFromGitRepoRequest' when calling asyncDeployFromGitRepo");
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
        return apiClient.invokeAPI("/terraform-boot/git/deploy/async", HttpMethod.POST,
                Collections.emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams,
                localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType,
                localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Async destroy the Terraform modules
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>202</b> - Accepted
     * @param terraformAsyncDestroyFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDestroyFromGitRepo(TerraformAsyncDestroyFromGitRepoRequest terraformAsyncDestroyFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        asyncDestroyFromGitRepoWithHttpInfo(terraformAsyncDestroyFromGitRepoRequest, xCustomRequestId);
    }

    /**
     * 
     * Async destroy the Terraform modules
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>202</b> - Accepted
     * @param terraformAsyncDestroyFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDestroyFromGitRepoWithHttpInfo(TerraformAsyncDestroyFromGitRepoRequest terraformAsyncDestroyFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = terraformAsyncDestroyFromGitRepoRequest;
        
        // verify the required parameter 'terraformAsyncDestroyFromGitRepoRequest' is set
        if (terraformAsyncDestroyFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformAsyncDestroyFromGitRepoRequest' when calling asyncDestroyFromGitRepo");
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
        return apiClient.invokeAPI("/terraform-boot/git/destroy/async", HttpMethod.DELETE,
                Collections.emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams,
                localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType,
                localVarAuthNames, localReturnType);
    }
    /**
     * 
     * async deploy resources via Terraform
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>202</b> - Accepted
     * @param terraformAsyncModifyFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncModifyFromGitRepo(TerraformAsyncModifyFromGitRepoRequest terraformAsyncModifyFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        asyncModifyFromGitRepoWithHttpInfo(terraformAsyncModifyFromGitRepoRequest, xCustomRequestId);
    }

    /**
     * 
     * async deploy resources via Terraform
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>202</b> - Accepted
     * @param terraformAsyncModifyFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncModifyFromGitRepoWithHttpInfo(TerraformAsyncModifyFromGitRepoRequest terraformAsyncModifyFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = terraformAsyncModifyFromGitRepoRequest;
        
        // verify the required parameter 'terraformAsyncModifyFromGitRepoRequest' is set
        if (terraformAsyncModifyFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformAsyncModifyFromGitRepoRequest' when calling asyncModifyFromGitRepo");
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
        return apiClient.invokeAPI("/terraform-boot/git/modify/async", HttpMethod.POST,
                Collections.emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams,
                localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType,
                localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Deploy resources via Terraform
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param terraformDeployFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult deployFromGitRepo(TerraformDeployFromGitRepoRequest terraformDeployFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        return deployFromGitRepoWithHttpInfo(terraformDeployFromGitRepoRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Deploy resources via Terraform
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param terraformDeployFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> deployFromGitRepoWithHttpInfo(TerraformDeployFromGitRepoRequest terraformDeployFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = terraformDeployFromGitRepoRequest;
        
        // verify the required parameter 'terraformDeployFromGitRepoRequest' is set
        if (terraformDeployFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformDeployFromGitRepoRequest' when calling deployFromGitRepo");
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

        ParameterizedTypeReference<TerraformResult> localReturnType = new ParameterizedTypeReference<TerraformResult>() {};
        return apiClient.invokeAPI("/terraform-boot/git/deploy", HttpMethod.POST,
                Collections.emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams,
                localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType,
                localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Destroy resources via Terraform
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param terraformDestroyFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult destroyFromGitRepo(TerraformDestroyFromGitRepoRequest terraformDestroyFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        return destroyFromGitRepoWithHttpInfo(terraformDestroyFromGitRepoRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Destroy resources via Terraform
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param terraformDestroyFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> destroyFromGitRepoWithHttpInfo(TerraformDestroyFromGitRepoRequest terraformDestroyFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = terraformDestroyFromGitRepoRequest;
        
        // verify the required parameter 'terraformDestroyFromGitRepoRequest' is set
        if (terraformDestroyFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformDestroyFromGitRepoRequest' when calling destroyFromGitRepo");
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

        ParameterizedTypeReference<TerraformResult> localReturnType = new ParameterizedTypeReference<TerraformResult>() {};
        return apiClient.invokeAPI("/terraform-boot/git/destroy", HttpMethod.POST,
                Collections.emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams,
                localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType,
                localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Modify resources via Terraform
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param terraformModifyFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult modifyFromGitRepo(TerraformModifyFromGitRepoRequest terraformModifyFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        return modifyFromGitRepoWithHttpInfo(terraformModifyFromGitRepoRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Modify resources via Terraform
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param terraformModifyFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> modifyFromGitRepoWithHttpInfo(TerraformModifyFromGitRepoRequest terraformModifyFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = terraformModifyFromGitRepoRequest;
        
        // verify the required parameter 'terraformModifyFromGitRepoRequest' is set
        if (terraformModifyFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformModifyFromGitRepoRequest' when calling modifyFromGitRepo");
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

        ParameterizedTypeReference<TerraformResult> localReturnType = new ParameterizedTypeReference<TerraformResult>() {};
        return apiClient.invokeAPI("/terraform-boot/git/modify", HttpMethod.POST,
                Collections.emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams,
                localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType,
                localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Get Terraform Plan as JSON string from the list of script files provided
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param terraformPlanFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return TerraformPlan
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformPlan planFromGitRepo(TerraformPlanFromGitRepoRequest terraformPlanFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        return planFromGitRepoWithHttpInfo(terraformPlanFromGitRepoRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Get Terraform Plan as JSON string from the list of script files provided
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param terraformPlanFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;TerraformPlan&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformPlan> planFromGitRepoWithHttpInfo(TerraformPlanFromGitRepoRequest terraformPlanFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = terraformPlanFromGitRepoRequest;
        
        // verify the required parameter 'terraformPlanFromGitRepoRequest' is set
        if (terraformPlanFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformPlanFromGitRepoRequest' when calling planFromGitRepo");
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

        ParameterizedTypeReference<TerraformPlan> localReturnType = new ParameterizedTypeReference<TerraformPlan>() {};
        return apiClient.invokeAPI("/terraform-boot/git/plan", HttpMethod.POST,
                Collections.emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams,
                localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType,
                localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Deploy resources via Terraform
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param terraformDeployFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return TerraformValidationResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformValidationResult validateScriptsFromGitRepo(TerraformDeployFromGitRepoRequest terraformDeployFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        return validateScriptsFromGitRepoWithHttpInfo(terraformDeployFromGitRepoRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Deploy resources via Terraform
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @param terraformDeployFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;TerraformValidationResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformValidationResult> validateScriptsFromGitRepoWithHttpInfo(TerraformDeployFromGitRepoRequest terraformDeployFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = terraformDeployFromGitRepoRequest;
        
        // verify the required parameter 'terraformDeployFromGitRepoRequest' is set
        if (terraformDeployFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformDeployFromGitRepoRequest' when calling validateScriptsFromGitRepo");
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

        ParameterizedTypeReference<TerraformValidationResult> localReturnType = new ParameterizedTypeReference<TerraformValidationResult>() {};
        return apiClient.invokeAPI("/terraform-boot/git/validate", HttpMethod.POST,
                Collections.emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams,
                localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType,
                localVarAuthNames, localReturnType);
    }
}
