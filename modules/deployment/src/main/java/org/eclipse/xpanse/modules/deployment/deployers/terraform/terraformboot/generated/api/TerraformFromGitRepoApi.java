package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api;

import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.ApiClient;

import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.Response;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncDeployFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformAsyncDestroyFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformDeployFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformDestroyFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformPlan;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformPlanFromGitRepoRequest;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformResult;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformValidationResult;
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

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
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
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     * @param terraformAsyncDeployFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDeployWithScripts1(TerraformAsyncDeployFromGitRepoRequest terraformAsyncDeployFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        asyncDeployWithScripts1WithHttpInfo(terraformAsyncDeployFromGitRepoRequest, xCustomRequestId);
    }

    /**
     * 
     * async deploy resources via Terraform
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     * @param terraformAsyncDeployFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDeployWithScripts1WithHttpInfo(TerraformAsyncDeployFromGitRepoRequest terraformAsyncDeployFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = terraformAsyncDeployFromGitRepoRequest;
        
        // verify the required parameter 'terraformAsyncDeployFromGitRepoRequest' is set
        if (terraformAsyncDeployFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformAsyncDeployFromGitRepoRequest' when calling asyncDeployWithScripts1");
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
        return apiClient.invokeAPI("/terraform-boot/git/deploy/async", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Async destroy the Terraform modules
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     * @param terraformAsyncDestroyFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void asyncDestroyWithScripts1(TerraformAsyncDestroyFromGitRepoRequest terraformAsyncDestroyFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        asyncDestroyWithScripts1WithHttpInfo(terraformAsyncDestroyFromGitRepoRequest, xCustomRequestId);
    }

    /**
     * 
     * Async destroy the Terraform modules
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>202</b> - Accepted
     * @param terraformAsyncDestroyFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> asyncDestroyWithScripts1WithHttpInfo(TerraformAsyncDestroyFromGitRepoRequest terraformAsyncDestroyFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = terraformAsyncDestroyFromGitRepoRequest;
        
        // verify the required parameter 'terraformAsyncDestroyFromGitRepoRequest' is set
        if (terraformAsyncDestroyFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformAsyncDestroyFromGitRepoRequest' when calling asyncDestroyWithScripts1");
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
        return apiClient.invokeAPI("/terraform-boot/git/destroy/async", HttpMethod.DELETE, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Deploy resources via Terraform
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param terraformDeployFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult deployWithScripts1(TerraformDeployFromGitRepoRequest terraformDeployFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        return deployWithScripts1WithHttpInfo(terraformDeployFromGitRepoRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Deploy resources via Terraform
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param terraformDeployFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> deployWithScripts1WithHttpInfo(TerraformDeployFromGitRepoRequest terraformDeployFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = terraformDeployFromGitRepoRequest;
        
        // verify the required parameter 'terraformDeployFromGitRepoRequest' is set
        if (terraformDeployFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformDeployFromGitRepoRequest' when calling deployWithScripts1");
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
        return apiClient.invokeAPI("/terraform-boot/git/deploy", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Destroy resources via Terraform
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param terraformDestroyFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult destroyWithScripts1(TerraformDestroyFromGitRepoRequest terraformDestroyFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        return destroyWithScripts1WithHttpInfo(terraformDestroyFromGitRepoRequest, xCustomRequestId).getBody();
    }

    /**
     * 
     * Destroy resources via Terraform
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param terraformDestroyFromGitRepoRequest  (required)
     * @param xCustomRequestId  (optional)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> destroyWithScripts1WithHttpInfo(TerraformDestroyFromGitRepoRequest terraformDestroyFromGitRepoRequest, UUID xCustomRequestId) throws RestClientException {
        Object localVarPostBody = terraformDestroyFromGitRepoRequest;
        
        // verify the required parameter 'terraformDestroyFromGitRepoRequest' is set
        if (terraformDestroyFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformDestroyFromGitRepoRequest' when calling destroyWithScripts1");
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
        return apiClient.invokeAPI("/terraform-boot/git/destroy", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Get Terraform Plan as JSON string from the list of script files provided
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
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
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
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
        return apiClient.invokeAPI("/terraform-boot/git/plan", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * 
     * Deploy resources via Terraform
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param terraformDeployFromGitRepoRequest  (required)
     * @return TerraformValidationResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformValidationResult validateWithScripts1(TerraformDeployFromGitRepoRequest terraformDeployFromGitRepoRequest) throws RestClientException {
        return validateWithScripts1WithHttpInfo(terraformDeployFromGitRepoRequest).getBody();
    }

    /**
     * 
     * Deploy resources via Terraform
     * <p><b>502</b> - Bad Gateway
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>200</b> - OK
     * @param terraformDeployFromGitRepoRequest  (required)
     * @return ResponseEntity&lt;TerraformValidationResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformValidationResult> validateWithScripts1WithHttpInfo(TerraformDeployFromGitRepoRequest terraformDeployFromGitRepoRequest) throws RestClientException {
        Object localVarPostBody = terraformDeployFromGitRepoRequest;
        
        // verify the required parameter 'terraformDeployFromGitRepoRequest' is set
        if (terraformDeployFromGitRepoRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'terraformDeployFromGitRepoRequest' when calling validateWithScripts1");
        }
        

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

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
        return apiClient.invokeAPI("/terraform-boot/git/validate", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
}
