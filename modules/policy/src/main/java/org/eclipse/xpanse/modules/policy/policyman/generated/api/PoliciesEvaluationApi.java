package org.eclipse.xpanse.modules.policy.policyman.generated.api;

import org.eclipse.xpanse.modules.policy.policyman.generated.ApiClient;

import org.eclipse.xpanse.modules.policy.policyman.generated.model.ErrorResult;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalCmd;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalCmdList;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.EvalResult;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.RegoResult;

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
@Component("org.eclipse.xpanse.modules.policy.policyman.generated.api.PoliciesEvaluationApi")
public class PoliciesEvaluationApi {
    private ApiClient apiClient;

    public PoliciesEvaluationApi() {
        this(new ApiClient());
    }

    @Autowired
    public PoliciesEvaluationApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Evaluate the input by policies
     * Evaluate whether the input meets all the policies
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>500</b> - Internal Server Error
     * <p><b>502</b> - Bad Gateway
     * @param cmdList evalCmdList (required)
     * @return EvalResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public EvalResult evaluatePoliciesPost(EvalCmdList cmdList) throws RestClientException {
        return evaluatePoliciesPostWithHttpInfo(cmdList).getBody();
    }

    /**
     * Evaluate the input by policies
     * Evaluate whether the input meets all the policies
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>500</b> - Internal Server Error
     * <p><b>502</b> - Bad Gateway
     * @param cmdList evalCmdList (required)
     * @return ResponseEntity&lt;EvalResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<EvalResult> evaluatePoliciesPostWithHttpInfo(EvalCmdList cmdList) throws RestClientException {
        Object localVarPostBody = cmdList;
        
        // verify the required parameter 'cmdList' is set
        if (cmdList == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'cmdList' when calling evaluatePoliciesPost");
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

        ParameterizedTypeReference<EvalResult> localReturnType = new ParameterizedTypeReference<EvalResult>() {};
        return apiClient.invokeAPI("/evaluate/policies", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Evaluate the input by policies
     * Evaluate the input by all the policies and get raw result
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>500</b> - Internal Server Error
     * <p><b>502</b> - Bad Gateway
     * @param cmdList evalCmdList (required)
     * @return List&lt;RegoResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<RegoResult> evaluatePoliciesRawPost(EvalCmdList cmdList) throws RestClientException {
        return evaluatePoliciesRawPostWithHttpInfo(cmdList).getBody();
    }

    /**
     * Evaluate the input by policies
     * Evaluate the input by all the policies and get raw result
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>500</b> - Internal Server Error
     * <p><b>502</b> - Bad Gateway
     * @param cmdList evalCmdList (required)
     * @return ResponseEntity&lt;List&lt;RegoResult&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<RegoResult>> evaluatePoliciesRawPostWithHttpInfo(EvalCmdList cmdList) throws RestClientException {
        Object localVarPostBody = cmdList;
        
        // verify the required parameter 'cmdList' is set
        if (cmdList == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'cmdList' when calling evaluatePoliciesRawPost");
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

        ParameterizedTypeReference<List<RegoResult>> localReturnType = new ParameterizedTypeReference<List<RegoResult>>() {};
        return apiClient.invokeAPI("/evaluate/policies/raw", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Evaluate the input by policies
     * Evaluate whether the input meets the policy
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>500</b> - Internal Server Error
     * <p><b>502</b> - Bad Gateway
     * @param cmd evalCmd (required)
     * @return EvalResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public EvalResult evaluatePolicyPost(EvalCmd cmd) throws RestClientException {
        return evaluatePolicyPostWithHttpInfo(cmd).getBody();
    }

    /**
     * Evaluate the input by policies
     * Evaluate whether the input meets the policy
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>500</b> - Internal Server Error
     * <p><b>502</b> - Bad Gateway
     * @param cmd evalCmd (required)
     * @return ResponseEntity&lt;EvalResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<EvalResult> evaluatePolicyPostWithHttpInfo(EvalCmd cmd) throws RestClientException {
        Object localVarPostBody = cmd;
        
        // verify the required parameter 'cmd' is set
        if (cmd == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'cmd' when calling evaluatePolicyPost");
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

        ParameterizedTypeReference<EvalResult> localReturnType = new ParameterizedTypeReference<EvalResult>() {};
        return apiClient.invokeAPI("/evaluate/policy", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Evaluate the input by policies
     * Evaluate the input by the policy and get raw result
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>500</b> - Internal Server Error
     * <p><b>502</b> - Bad Gateway
     * @param cmd evalCmd (required)
     * @return List&lt;RegoResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<RegoResult> evaluatePolicyRawPost(EvalCmd cmd) throws RestClientException {
        return evaluatePolicyRawPostWithHttpInfo(cmd).getBody();
    }

    /**
     * Evaluate the input by policies
     * Evaluate the input by the policy and get raw result
     * <p><b>200</b> - OK
     * <p><b>400</b> - Bad Request
     * <p><b>500</b> - Internal Server Error
     * <p><b>502</b> - Bad Gateway
     * @param cmd evalCmd (required)
     * @return ResponseEntity&lt;List&lt;RegoResult&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<RegoResult>> evaluatePolicyRawPostWithHttpInfo(EvalCmd cmd) throws RestClientException {
        Object localVarPostBody = cmd;
        
        // verify the required parameter 'cmd' is set
        if (cmd == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'cmd' when calling evaluatePolicyRawPost");
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

        ParameterizedTypeReference<List<RegoResult>> localReturnType = new ParameterizedTypeReference<List<RegoResult>>() {};
        return apiClient.invokeAPI("/evaluate/policy/raw", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
}
