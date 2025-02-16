package org.eclipse.xpanse.modules.policy.policyman.generated.api;

import java.util.Collections;
import java.util.List;
import org.eclipse.xpanse.modules.policy.policyman.generated.ApiClient;
import org.eclipse.xpanse.modules.policy.policyman.generated.model.StackStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
@Component("org.eclipse.xpanse.modules.policy.policyman.generated.api.AdminApi")
public class AdminApi {
    private ApiClient apiClient;

    public AdminApi() {
        this(new ApiClient());
    }

    @Autowired
    public AdminApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Check health Check health status of service
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>500</b> - Internal Server Error
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @return SystemStatus
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public StackStatus healthGet() throws RestClientException {
        return healthGetWithHttpInfo().getBody();
    }

    /**
     * Check health Check health status of service
     *
     * <p><b>200</b> - OK
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>500</b> - Internal Server Error
     *
     * <p><b>502</b> - Bad Gateway
     *
     * @return ResponseEntity&lt;SystemStatus&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<StackStatus> healthGetWithHttpInfo() throws RestClientException {
        Object localVarPostBody = null;

        final MultiValueMap<String, String> localVarQueryParams =
                new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams =
                new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams =
                new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = {"application/json"};
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType =
                apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {};

        ParameterizedTypeReference<StackStatus> localReturnType =
                new ParameterizedTypeReference<StackStatus>() {};
        return apiClient.invokeAPI(
                "/health",
                HttpMethod.GET,
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
}
