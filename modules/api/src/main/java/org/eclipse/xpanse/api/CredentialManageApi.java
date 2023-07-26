/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api;

import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.models.security.constant.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.CredentialVariable;
import org.eclipse.xpanse.modules.models.credential.CredentialVariables;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialTypeMessage;
import org.eclipse.xpanse.modules.models.service.common.enums.Csp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
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
 * REST interface methods for managing cloud provider credentials of the user.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
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
     * @return Returns list the available credential types of the cloud service provider.
     */
    @Tag(name = "Credentials Management",
            description = "APIs for managing user's cloud provider credentials")
    @GetMapping(value = "/auth/csp/{cspName}/credential/types",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Get the credential types supported by the cloud service provider.")
    public List<CredentialType> getCredentialTypesByCsp(
            @Parameter(name = "cspName", description = "The cloud service provider.")
            @PathVariable(name = "cspName") Csp csp) {
        return credentialCenter.getAvailableCredentialTypesByCsp(csp);
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
    @GetMapping(value = "/auth/csp/{cspName}/credential/capabilities",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description =
            "Get the credential capabilities defined by the cloud service provider.")
    public List<AbstractCredentialInfo> getCredentialCapabilitiesByCsp(
            @Parameter(name = "cspName", description = "name of the cloud service provider.")
            @PathVariable(name = "cspName") Csp csp,
            @Parameter(name = "type", description = "The type of credential.")
            @RequestParam(name = "type", required = false) CredentialType type,
            @Parameter(name = "name", description = "The name of credential.")
            @RequestParam(name = "name", required = false) String name) {
        List<AbstractCredentialInfo> abstractCredentialInfos =
                credentialCenter.getCredentialCapabilitiesByCsp(csp, type, name);
        getCredentialCapabilitiesValue(abstractCredentialInfos);
        return abstractCredentialInfos;
    }

    /**
     * Get all cloud provider credentials added by the user.
     *
     * @param userName The name of the user who provided the credential.
     * @return Returns all credentials of the user.
     */
    @Tag(name = "Credentials Management",
            description = "APIs for managing user's cloud provider credentials")
    @GetMapping(value = "/auth/user/credentials",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Get all cloud provider credentials added by the user.")
    public List<AbstractCredentialInfo> getCredentialsByUser(
            @Parameter(name = "userName",
                    description = "The name of user who provided the credential.",
                    required = true)
            @RequestParam(name = "userName") String userName) {
        return credentialCenter.getCredentialsByUser(userName);
    }

    /**
     * Get all cloud provider credentials added by the user for a cloud service provider.
     *
     * @param csp      The cloud service provider.
     * @param userName The name of the user who provided the credential.
     * @param type     The type of credential.
     * @return Returns credentials of the cloud service provider and the user.
     */
    @Tag(name = "Credentials Management",
            description = "APIs for managing user's cloud provider credentials")
    @GetMapping(value = "/auth/csp/{cspName}/credentials",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description =
            "Get all cloud provider credentials added by the user for a cloud service provider.")
    public List<AbstractCredentialInfo> getCredentials(
            @Parameter(name = "cspName", description = "The cloud service provider.")
            @PathVariable(name = "cspName") Csp csp,
            @Parameter(name = "type", description = "The type of credential.")
            @RequestParam(name = "type", required = false) CredentialType type,
            @Parameter(name = "userName",
                    description = "The name of user who provided the credential.")
            @RequestParam(name = "userName") String userName) {
        return credentialCenter.getCredentials(csp, type, userName);
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
    @GetMapping(value = "/auth/csp/{cspName}/openapi/{type}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Returns the OpenAPI document for adding a credential.")
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
     * Add user's credential for connecting to the cloud service provider.
     *
     * @param createCredential The credential to be created.
     */
    @Tag(name = "Credentials Management",
            description = "APIs for managing user's cloud provider credentials")
    @PostMapping(value = "/auth/csp/credential",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(description = "Add user's credential for connecting to the cloud service provider.")
    public void addCredential(
            @Valid @RequestBody CreateCredential createCredential) {
        credentialCenter.addCredential(createCredential);
    }

    /**
     * Update user's credential for connecting to the cloud service provider.
     *
     * @param updateCredential The credential to be updated.
     */
    @Tag(name = "Credentials Management",
            description = "APIs for managing user's cloud provider credentials")
    @PutMapping(value = "/auth/csp/credential",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(description =
            "Update user's credential for connecting to the cloud service provider.")
    public void updateCredential(
            @Valid @RequestBody CreateCredential updateCredential) {
        credentialCenter.updateCredential(updateCredential);
    }

    /**
     * Delete user's credential for connecting to the cloud service provider.
     *
     * @param csp      The cloud service provider.
     * @param userName The name of the user who provided credential.
     */
    @Tag(name = "Credentials Management",
            description = "APIs for managing user's cloud provider credentials")
    @DeleteMapping(value = "/auth/csp/{cspName}/credential",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(description =
            "Delete user's credential for connecting to the cloud service provider.")
    public void deleteCredential(
            @Parameter(name = "cspName", description = "The cloud service provider.")
            @PathVariable("cspName") Csp csp,
            @Parameter(name = "type", description = "The type of credential.")
            @RequestParam(name = "type") CredentialType type,
            @Parameter(name = "name", description = "The name of of credential.")
            @RequestParam(name = "name") String name,
            @Parameter(name = "userName", description = "The name of user who provided credential.")
            @RequestParam(name = "userName") String userName) {
        credentialCenter.deleteCredential(csp, type, name, userName);
    }

    private void getCredentialCapabilitiesValue(
            List<AbstractCredentialInfo> abstractCredentialInfoList) {

        for (AbstractCredentialInfo abstractCredentialInfo : abstractCredentialInfoList) {
            if (abstractCredentialInfo.getType() == CredentialType.VARIABLES) {
                CredentialVariables credentialVariables =
                        (CredentialVariables) abstractCredentialInfo;
                for (CredentialVariable variable : credentialVariables.getVariables()) {
                    variable.setValue(
                            CredentialTypeMessage.getMessageByType(CredentialType.VARIABLES));
                }
            }
        }
    }

}
