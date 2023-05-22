/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.credential.CreateCredential;
import org.eclipse.xpanse.modules.credential.CredentialDefinition;
import org.eclipse.xpanse.modules.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.orchestrator.credential.CredentialCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST interface methods for credential management.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
public class CredentialManageApi {

    private final CredentialCenter credentialCenter;

    @Autowired
    public CredentialManageApi(CredentialCenter credentialCenter) {
        this.credentialCenter = credentialCenter;
    }


    /**
     * List the available credential types of the cloud service provider.
     *
     * @param csp The cloud service provider.
     * @return Returns list of the available credential types of the cloud service provider.
     */
    @Tag(name = "Credentials Management",
            description = "APIs to manage credentials for authentication.")
    @GetMapping(value = "/auth/csp/{cspName}/credential/types",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "List credential types provided by the cloud service provider.")
    public List<CredentialType> getCredentialTypesByCsp(
            @Parameter(name = "cspName", description = "The cloud service provider.")
            @PathVariable(name = "cspName") Csp csp) {
        return credentialCenter.getAvailableCredentialTypesByCsp(csp);
    }


    /**
     * List credential capabilities of the cloud service provider.
     *
     * @param csp  The cloud service provider.
     * @param type The type of credential.
     * @return Returns list of credential capabilities of the cloud service provider.
     */
    @Tag(name = "Credentials Management",
            description = "APIs to manage credentials for authentication.")
    @GetMapping(value = "/auth/csp/{cspName}/credential/capabilities",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "List credential capabilities provided by the cloud service provider.")
    public List<AbstractCredentialInfo> getCredentialCapabilitiesByCsp(
            @Parameter(name = "cspName", description = "The cloud service provider.")
            @PathVariable(name = "cspName") Csp csp,
            @Parameter(name = "type", description = "The type of credential.")
            @RequestParam(name = "type", required = false) CredentialType type) {
        return credentialCenter.getCredentialCapabilitiesByCsp(csp, type);
    }


    /**
     * List credentials of the cloud service provider and the user.
     *
     * @param csp      The cloud service provider.
     * @param userName The name of user who provided the credential.
     * @param type     The type of credential.
     * @return Returns credentials of the cloud service provider and the user.
     */
    @Tag(name = "Credentials Management",
            description = "APIs to manage credentials for authentication.")
    @GetMapping(value = "/auth/csp/{cspName}/credentials",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "List credentials of the cloud service provider and the user.")
    public List<CredentialDefinition> getCredentialDefinitionsByCsp(
            @Parameter(name = "cspName", description = "The cloud service provider.")
            @PathVariable(name = "cspName") Csp csp,
            @Parameter(name = "userName",
                    description = "The name of user who provided the credential.")
            @RequestParam(name = "userName") String userName,
            @Parameter(name = "type", description = "The type of credential.")
            @RequestParam(name = "type", required = false) CredentialType type) {
        return credentialCenter.getCredentialDefinitionsByCsp(csp, userName, type);
    }

    /**
     * Get the API document for adding credential.
     *
     * @param csp  The cloud service provider.
     * @param type The type of credential.
     * @return Link of credential openApi url.
     */
    @Tag(name = "Services Available",
            description = "APIs to query the available services.")
    @GetMapping(value = "/auth/csp/{cspName}/openapi/{type}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Get the API document for adding credential of the Csp.")
    public Link getCredentialOpenApi(
            @Parameter(name = "cspName", description = "The cloud service provider.")
            @PathVariable(name = "cspName") Csp csp,
            @Parameter(name = "type", description = "The type of credential.")
            @PathVariable(name = "type") CredentialType type) {
        String apiUrl = credentialCenter.getCredentialOpenApiUrl(csp, type);
        String successMsg = String.format(
                "Get API document of adding credential with type %s of the cloud service provider"
                        + " %s successfully. Url %s", type.toValue(), csp.toValue(), apiUrl);
        log.info(successMsg);
        return Link.of(apiUrl, "OpenApi");
    }

    /**
     * Add credential of the cloud service provider.
     *
     * @param csp              The cloud service provider.
     * @param createCredential The credential to create.
     */
    @Tag(name = "Credentials Management",
            description = "APIs to manage credentials for authentication.")
    @PostMapping(value = "/auth/csp/{cspName}/credential",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Add credential of the cloud service provider.")
    public Boolean addCredential(
            @Parameter(name = "cspName", description = "The cloud service provider.")
            @PathVariable("cspName") Csp csp,
            @Valid @RequestBody CreateCredential createCredential) {
        return credentialCenter.addCredential(csp, createCredential);
    }

    /**
     * Update credential of the cloud service provider.
     *
     * @param csp              The cloud service provider.
     * @param updateCredential The credential to update.
     */
    @Tag(name = "Credentials Management",
            description = "APIs to manage credentials for authentication.")
    @PutMapping(value = "/auth/csp/{cspName}/credential",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Update credential of the cloud service provider.")
    public Boolean updateCredential(
            @Parameter(name = "cspName", description = "The cloud service provider")
            @PathVariable("cspName") Csp csp,
            @Valid @RequestBody CreateCredential updateCredential) {
        return credentialCenter.updateCredential(csp, updateCredential);
    }

    /**
     * Delete credential of the cloud service provider and the user.
     *
     * @param csp      The cloud service provider.
     * @param userName The name of user who provided credential.
     */
    @Tag(name = "Credentials Management",
            description = "APIs to manage credentials for authentication.")
    @DeleteMapping(value = "/auth/csp/{cspName}/credential",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Delete credential of the cloud service provider and the user.")
    public Boolean deleteCredential(
            @Parameter(name = "cspName", description = "The cloud service provider.")
            @PathVariable("cspName") Csp csp,
            @Parameter(name = "userName", description = "The name of user who provided credential.")
            @RequestParam(name = "userName") String userName) {
        return credentialCenter.deleteCredential(csp, userName);
    }


    /**
     * List the available credential types of the service.
     *
     * @param id The UUID of the deployed service.
     * @return Returns list of the available credential types of the service.
     */
    @Tag(name = "Credentials Management",
            description = "APIs to manage credentials for authentication.")
    @GetMapping(value = "/auth/service/{id}/credential/types",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "List credential types provided by the cloud service provider.")
    public List<CredentialType> getCredentialTypesByServiceId(
            @Parameter(name = "id", description = "The id of the deployed service.")
            @PathVariable("id") String id) {
        return credentialCenter.getAvailableCredentialTypesByServiceId(id);
    }

    /**
     * List credential capabilities of the service.
     *
     * @param id The id of deployed service.
     * @return Returns credential capabilities list of the service.
     */
    @Tag(name = "Credentials Management",
            description = "APIs to manage credentials for authentication.")
    @GetMapping(value = "/auth/service/{id}/credential/capabilities",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "List credential capabilities of the service.")
    public List<AbstractCredentialInfo> getCredentialCapabilitiesByServiceId(
            @Parameter(name = "id", description = "The id of the deployed service.")
            @PathVariable("id") String id,
            @Parameter(name = "type", description = "The type of credential.")
            @RequestParam(name = "type", required = false) CredentialType type) {
        return credentialCenter.getCredentialCapabilitiesByServiceId(id, type);
    }

    /**
     * List credentials of the service.
     *
     * @param id   The id of deployed service.
     * @param type The type of credential.
     * @return Returns credential capabilities list of the service.
     */
    @Tag(name = "Credentials Management",
            description = "APIs to manage credentials for authentication.")
    @GetMapping(value = "/auth/service/{id}/credentials",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "List credentials of the service.")
    public List<CredentialDefinition> getCredentialDefinitionsByServiceId(
            @Parameter(name = "id", description = "The id of the deployed service.")
            @PathVariable("id") String id,
            @Parameter(name = "type", description = "The type of credential.")
            @RequestParam(name = "type", required = false) CredentialType type) {
        return credentialCenter.getCredentialDefinitionsByServiceId(id, type);

    }

    /**
     * Add credential for the service.
     *
     * @param id               The id of deployed service.
     * @param createCredential The credential to create.
     */
    @Tag(name = "Credentials Management",
            description = "APIs to manage credentials for authentication.")
    @PostMapping(value = "/auth/service/{id}/credential",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Add credential for the service.")
    public Boolean addCredentialByServiceId(
            @Parameter(name = "id", description = "The id of the deployed service.")
            @PathVariable("id") String id,
            @Valid @RequestBody CreateCredential createCredential) {
        return credentialCenter.addCredentialByServiceId(id, createCredential);
    }

    /**
     * Update credential for the service.
     *
     * @param id               The id of deployed service.
     * @param updateCredential The credential to update.
     */
    @Tag(name = "Credentials Management",
            description = "APIs to manage credentials for authentication.")
    @PutMapping(value = "/auth/service/{id}/credential",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Update credential for the service.")
    public Boolean updateCredentialByServiceId(
            @Parameter(name = "id", description = "The id of the deployed service.")
            @PathVariable("id") String id,
            @Valid @RequestBody CreateCredential updateCredential) {
        return credentialCenter.updateCredentialByServiceId(id, updateCredential);
    }

    /**
     * Delete credentials of the service.
     *
     * @param id   The id of deployed service.
     * @param type The type of credential.
     */
    @Tag(name = "Credentials Management",
            description = "APIs to manage credentials for authentication.")
    @DeleteMapping(value = "/auth/service/{id}/credential",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Delete credentials of the service.")
    public Boolean deleteCredentialByServiceId(
            @Parameter(name = "id", description = "The id of the deployed service.")
            @PathVariable("id") String id,
            @RequestParam(name = "type", required = false) CredentialType type) {
        return credentialCenter.deleteCredentialByServiceId(id, type);
    }

    /**
     * Get the API document for adding credential.
     *
     * @param id   The id of deployed service.
     * @param type The type of credential.
     * @return Link of credential openApi url.
     */
    @Tag(name = "Services Available",
            description = "APIs to query the available services.")
    @GetMapping(value = "/auth/service/{id}/openapi/{type}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Get the API document for adding credential of the Csp.")
    public Link getCredentialOpenApiByServiceId(
            @Parameter(name = "id", description = "The id of the deployed service.")
            @PathVariable(name = "id") String id,
            @Parameter(name = "type", description = "The type of credential.")
            @PathVariable(name = "type") CredentialType type) {
        String apiUrl = credentialCenter.getCredentialOpenApiUrlByServiceId(id, type);
        String successMsg = String.format(
                "Get API document of adding credential with type %s of the deployed service "
                        + " %s successfully. Url %s", type.toValue(), id, apiUrl);
        log.info(successMsg);
        return Link.of(apiUrl, "OpenApi");
    }
}
