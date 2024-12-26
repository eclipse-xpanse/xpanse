package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.ApiClient;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.BaseApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformBootSystemStatus;
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

@jakarta.annotation.Generated(
        value = "org.openapitools.codegen.languages.JavaClientCodegen",
        comments = "Generator version: 7.10.0")
@Component(
        "org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.AdminApi")
public class AdminApi extends BaseApi {

    public AdminApi() {
        super(new ApiClient());
    }

    @Autowired
    public AdminApi(ApiClient apiClient) {
        super(apiClient);
    }

    /**
     * Check health of Terraform Boot API service
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>200</b> - OK
     *
     * @return TerraformBootSystemStatus
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TerraformBootSystemStatus healthCheck() throws RestClientException {
        return healthCheckWithHttpInfo().getBody();
    }

    /**
     * Check health of Terraform Boot API service
     *
     * <p><b>422</b> - Unprocessable Entity
     *
     * <p><b>400</b> - Bad Request
     *
     * <p><b>503</b> - Service Unavailable
     *
     * <p><b>502</b> - Bad Gateway
     *
     * <p><b>200</b> - OK
     *
     * @return ResponseEntity&lt;TerraformBootSystemStatus&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<TerraformBootSystemStatus> healthCheckWithHttpInfo()
            throws RestClientException {
        Object localVarPostBody = null;

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

        ParameterizedTypeReference<TerraformBootSystemStatus> localReturnType =
                new ParameterizedTypeReference<TerraformBootSystemStatus>() {};
        return apiClient.invokeAPI(
                "/terraform-boot/health",
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
