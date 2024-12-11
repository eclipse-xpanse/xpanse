package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.ApiClient;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.BaseApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformResult;
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
        comments = "Generator version: 7.10.0")
@Component(
        "org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.RetrieveTerraformResultApi")
public class RetrieveTerraformResultApi extends BaseApi {

    public RetrieveTerraformResultApi() {
        super(new ApiClient());
    }

    @Autowired
    public RetrieveTerraformResultApi(ApiClient apiClient) {
        super(apiClient);
    }

    /**
     * Method to retrieve stored terraform result in case terraform-boot receives a failure while
     * sending the terraform result via callback.
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param requestId id of the request (required)
     * @return TerraformResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformResult getStoredTaskResultByRequestId(String requestId)
            throws RestClientException {
        return getStoredTaskResultByRequestIdWithHttpInfo(requestId).getBody();
    }

    /**
     * Method to retrieve stored terraform result in case terraform-boot receives a failure while
     * sending the terraform result via callback.
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>200</b> - OK
     *
     * @param requestId id of the request (required)
     * @return ResponseEntity&lt;TerraformResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformResult> getStoredTaskResultByRequestIdWithHttpInfo(
            String requestId) throws RestClientException {
        Object localVarPostBody = null;

        // verify the required parameter 'requestId' is set
        if (requestId == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'requestId' when calling"
                            + " getStoredTaskResultByRequestId");
        }

        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("requestId", requestId);

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

        ParameterizedTypeReference<TerraformResult> localReturnType =
                new ParameterizedTypeReference<TerraformResult>() {};
        return apiClient.invokeAPI(
                "/terraform-boot/task/result/{requestId}",
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
