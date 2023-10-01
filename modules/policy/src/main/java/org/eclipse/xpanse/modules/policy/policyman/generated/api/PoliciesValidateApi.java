package org.eclipse.xpanse.modules.policy.policyman.generated.api;

import org.eclipse.xpanse.modules.policy.policyman.generated.ApiClient;

import org.eclipse.xpanse.modules.policy.policyman.generated.model.ErrorResult;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.ValidatePolicyList;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.ValidateResponse;

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
@Component("org.eclipse.xpanse.modules.policy.policyman.generated.api.PoliciesValidateApi")
public class PoliciesValidateApi {
    private ApiClient apiClient;

    public PoliciesValidateApi() {
        this(new ApiClient());
    }

    @Autowired
    public PoliciesValidateApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Validate the policies
     * Validate the policies
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>500</b> - Internal Server Error
     * <p><b>502</b> - Bad Gateway
     * @param policyList policyList (required)
     * @return ValidateResponse
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ValidateResponse validatePoliciesPost(ValidatePolicyList policyList) throws RestClientException {
        return validatePoliciesPostWithHttpInfo(policyList).getBody();
    }

    /**
     * Validate the policies
     * Validate the policies
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>500</b> - Internal Server Error
     * <p><b>502</b> - Bad Gateway
     * @param policyList policyList (required)
     * @return ResponseEntity&lt;ValidateResponse&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<ValidateResponse> validatePoliciesPostWithHttpInfo(ValidatePolicyList policyList) throws RestClientException {
        Object localVarPostBody = policyList;
        
        // verify the required parameter 'policyList' is set
        if (policyList == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'policyList' when calling validatePoliciesPost");
        }
        

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
         };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {  };

        ParameterizedTypeReference<ValidateResponse> localReturnType = new ParameterizedTypeReference<ValidateResponse>() {};
        return apiClient.invokeAPI("/validate/policies", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
}
