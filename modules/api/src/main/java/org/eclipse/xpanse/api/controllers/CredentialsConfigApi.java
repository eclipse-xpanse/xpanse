/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;


import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_ISV;
import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST interface method for managing cloud provider configuration credentials.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ISV, ROLE_ADMIN, ROLE_USER})
public class CredentialsConfigApi {

    private final CredentialCenter credentialCenter;

    @Autowired
    public CredentialsConfigApi(CredentialCenter credentialCenter) {
        this.credentialCenter = credentialCenter;
    }

    /**
     * List the available credential types of the cloud service provider.
     *
     * @param csp The cloud service provider.
     * @return Returns list the available credential types of the cloud service provider.
     */
    @Tag(name = "Credentials Management",
            description = "APIs for managing user's cloud provider credentials")
    @GetMapping(value = "/credential_types",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "List the credential types supported by the cloud service provider.")
    public List<CredentialType> getCredentialTypes(
            @Parameter(name = "cspName", description = "The cloud service provider.")
            @RequestParam(name = "cspName", required = false) Csp csp) {
        if (Objects.isNull(csp)) {
            return Arrays.stream(CredentialType.values()).toList();
        }
        return credentialCenter.listAvailableCredentialTypesByCsp(csp);
    }


    /**
     * Get the credential capabilities defined by the cloud service provider.
     *
     * @param csp  The cloud service provider.
     * @param type The type of credential.
     * @return Returns list of credential capabilities defined by the cloud service provider.
     */
    @Tag(name = "Credentials Management",
            description = "APIs for managing user's cloud provider credentials")
    @GetMapping(value = "/credentials/capabilities",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description =
            "List the credential capabilities defined by the cloud service provider.")
    public List<AbstractCredentialInfo> getCredentialCapabilities(
            @Parameter(name = "cspName", description = "name of the cloud service provider.")
            @RequestParam(name = "cspName") Csp csp,
            @Parameter(name = "type", description = "The type of credential.")
            @RequestParam(name = "type", required = false) CredentialType type,
            @Parameter(name = "name", description = "The name of credential.")
            @RequestParam(name = "name", required = false) String name) {
        List<AbstractCredentialInfo> abstractCredentialInfos =
                credentialCenter.listCredentialCapabilities(csp, type, name);
        credentialCenter.getCredentialCapabilitiesValue(abstractCredentialInfos);
        return abstractCredentialInfos;
    }

    /**
     * Get the OpenAPI document for adding a credential.
     *
     * @param csp  The cloud service provider.
     * @param type The type of credential.
     * @return Link of credential openApi url.
     */
    @Tag(name = "Credentials Management",
            description = "APIs for managing user's cloud provider credentials")
    @GetMapping(value = "/credentials/openapi/{csp}/{type}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Returns the OpenAPI document for adding a credential.")
    public Link getCredentialOpenApi(
            @Parameter(name = "csp", description = "The cloud service provider.")
            @PathVariable(name = "csp") Csp csp,
            @Parameter(name = "type", description = "The type of credential.")
            @PathVariable(name = "type") CredentialType type) {
        String apiUrl = credentialCenter.getCredentialOpenApiUrl(csp, type);
        String successMsg = String.format(
                "Get API document of adding credential with type %s of the cloud service provider"
                        + " %s successfully. Url %s", type.toValue(), csp.toValue(), apiUrl);
        log.info(successMsg);
        return Link.of(apiUrl, "OpenApi");
    }
}
