package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api;

import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.ApiClient;

import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuMakerSystemStatus;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.Response;

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

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.4.0")
@Component("org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.AdminApi")
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
     * 
     * Check health of OpenTofu Maker API service
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @return OpenTofuMakerSystemStatus
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public OpenTofuMakerSystemStatus healthCheck() throws RestClientException {
        return healthCheckWithHttpInfo().getBody();
    }

    /**
     * 
     * Check health of OpenTofu Maker API service
     * <p><b>422</b> - Unprocessable Entity
     * <p><b>400</b> - Bad Request
     * <p><b>503</b> - Service Unavailable
     * <p><b>502</b> - Bad Gateway
     * <p><b>200</b> - OK
     * @return ResponseEntity&lt;OpenTofuMakerSystemStatus&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<OpenTofuMakerSystemStatus> healthCheckWithHttpInfo() throws RestClientException {
        Object localVarPostBody = null;
        

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*", "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "OAuth2Flow" };

        ParameterizedTypeReference<OpenTofuMakerSystemStatus> localReturnType = new ParameterizedTypeReference<OpenTofuMakerSystemStatus>() {};
        return apiClient.invokeAPI("/tofu-maker/health", HttpMethod.GET, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
}
