/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ISV;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST interface method for managing cloud provider credentials for users of a role ISV.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse/isv")
@CrossOrigin
@Secured({ROLE_ISV})
public class IsvCloudCredentialsApi {

    private final CredentialCenter credentialCenter;

    @Autowired
    public IsvCloudCredentialsApi(CredentialCenter credentialCenter) {
        this.credentialCenter = credentialCenter;
    }

    /**
     * Users in the ISV role get all cloud provider credentials added by the user for a cloud
     * service provider.
     *
     * @param csp  The cloud service provider.
     * @param type The type of credential.
     * @return Returns credentials of the cloud service provider and the user.
     */
    @Tag(name = "ISV Cloud Credentials Management",
            description = "APIs for managing isv's cloud provider credentials")
    @GetMapping(value = "/credentials",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description =
            "Users in the ISV role get all cloud provider credentials added by the user for a "
                    + "cloud service "
                    + "provider.")
    public List<AbstractCredentialInfo> getIsvCloudCredentials(
            @Parameter(name = "cspName", description = "The cloud service provider.")
            @RequestParam(name = "cspName", required = false) Csp csp,
            @Parameter(name = "type", description = "The type of credential.")
            @RequestParam(name = "type", required = false) CredentialType type) {
        return credentialCenter.listCredentials(csp, type, null);
    }

    /**
     * Add the user credentials for the ISV role used to connect to the cloud service provider.
     *
     * @param createCredential The credential to be created.
     */
    @Tag(name = "ISV Cloud Credentials Management",
            description = "APIs for managing isv's cloud provider credentials")
    @PostMapping(value = "/credentials",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(description = "Add the user credentials for the ISV role used to connect to the "
            + "cloud service provider.")
    public void addIsvCloudCredential(
            @Valid @RequestBody CreateCredential createCredential) {
        credentialCenter.addCredential(createCredential);
    }

    /**
     * Update the user credentials used for ISV to connect to the cloud service provider.
     *
     * @param updateCredential The credential to be updated.
     */
    @Tag(name = "ISV Cloud Credentials Management",
            description = "APIs for managing isv's cloud provider credentials")
    @PutMapping(value = "/credentials",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(description =
            "Update the user credentials used for ISV to connect to the cloud service provider.")
    public void updateIsvCloudCredential(
            @Valid @RequestBody CreateCredential updateCredential) {
        credentialCenter.updateCredential(updateCredential);
    }

    /**
     * Delete the credentials of the user in the USER role to connect to the cloud service
     * provider.
     *
     * @param csp  The cloud service provider.
     * @param type The type of credential.
     * @param name The name of credential.
     */
    @Tag(name = "ISV Cloud Credentials Management",
            description = "APIs for managing isv's cloud provider credentials")
    @DeleteMapping(value = "/credentials",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(description =
            "Delete the credentials of the user in the USER role to connect to the cloud service "
                    + "provider.")
    public void deleteIsvCloudCredential(
            @Parameter(name = "cspName", description = "The cloud service provider.")
            @RequestParam("cspName") Csp csp,
            @Parameter(name = "type", description = "The type of credential.")
            @RequestParam(name = "type") CredentialType type,
            @Parameter(name = "name", description = "The name of of credential.")
            @RequestParam(name = "name") String name) {
        credentialCenter.deleteCredential(csp, type, name, null);
    }
}
