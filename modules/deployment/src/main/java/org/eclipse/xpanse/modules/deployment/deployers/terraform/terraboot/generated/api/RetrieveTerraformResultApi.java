package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.ApiClient;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.BaseApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.model.ReFetchResult;
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
        "org.eclipse.xpanse.modules.deployment.deployers.terraform.terraboot.generated.api.RetrieveTerraformResultApi")
public class RetrieveTerraformResultApi extends BaseApi {

    public RetrieveTerraformResultApi() {
        super(new ApiClient());
    }

    @Autowired
    public RetrieveTerraformResultApi(ApiClient apiClient) {
        super(apiClient);
    }

    /**
     * Method to batch retrieve stored terraform result from terra-boot.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param UUID (required)
     * @return List&lt;ReFetchResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<ReFetchResult> getBatchTaskResults(List<UUID> UUID) throws RestClientException {
        return getBatchTaskResultsWithHttpInfo(UUID).getBody();
    }

    /**
     * Method to batch retrieve stored terraform result from terra-boot.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param UUID (required)
     * @return ResponseEntity&lt;List&lt;ReFetchResult&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<ReFetchResult>> getBatchTaskResultsWithHttpInfo(List<UUID> UUID)
            throws RestClientException {
        Object localVarPostBody = UUID;

        // verify the required parameter 'UUID' is set
        if (UUID == null) {
            throw new HttpClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Missing the required parameter 'UUID' when calling getBatchTaskResults");
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

        ParameterizedTypeReference<List<ReFetchResult>> localReturnType =
                new ParameterizedTypeReference<List<ReFetchResult>>() {};
        return apiClient.invokeAPI(
                "/terra-boot/task/results/batch",
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
     * Method to retrieve stored terraform result in case terra-boot receives a failure while
     * sending the terraform result via callback.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param requestId id of the request (required)
     * @return ReFetchResult
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ReFetchResult getStoredTaskResultByRequestId(UUID requestId) throws RestClientException {
        return getStoredTaskResultByRequestIdWithHttpInfo(requestId).getBody();
    }

    /**
     * Method to retrieve stored terraform result in case terra-boot receives a failure while
     * sending the terraform result via callback.
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @param requestId id of the request (required)
     * @return ResponseEntity&lt;ReFetchResult&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<ReFetchResult> getStoredTaskResultByRequestIdWithHttpInfo(UUID requestId)
            throws RestClientException {
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

        final String[] localVarAccepts = {"application/json", "*/*"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {"OAuth2Flow"};

        ParameterizedTypeReference<ReFetchResult> localReturnType =
                new ParameterizedTypeReference<ReFetchResult>() {};
        return apiClient.invokeAPI(
                "/terra-boot/task/result/{requestId}",
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

        final String[] localVarAccepts = {"application/json", "*/*"};
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
