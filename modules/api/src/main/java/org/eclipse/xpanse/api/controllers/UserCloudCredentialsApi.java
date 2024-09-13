/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.credential.CredentialCenter;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.credential.CreateCredential;
import org.eclipse.xpanse.modules.models.credential.enums.CredentialType;
import org.eclipse.xpanse.modules.security.UserServiceHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
 * REST interface methods for managing cloud provider credentials for users of a role USER.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse/user")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
@ConditionalOnProperty(name = "enable.agent.api.only", havingValue = "false", matchIfMissing = true)
public class UserCloudCredentialsApi {


    @Resource
    private CredentialCenter credentialCenter;

    @Resource
    private UserServiceHelper userServiceHelper;


    /**
     * Get all cloud provider credentials added by the user for a cloud service provider.
     *
     * @param csp  The cloud service provider.
     * @param type The type of credential.
     * @return Returns credentials of the cloud service provider and the user.
     */
    @Tag(name = "UserCloudCredentialsManagement",
            description = "APIs for managing user's cloud provider credentials")
    @GetMapping(value = "/credentials",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description =
            "List all cloud provider credentials added by the user for a cloud service provider.")
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public List<AbstractCredentialInfo> getUserCloudCredentials(
            @Parameter(name = "cspName", description = "The cloud service provider.")
            @RequestParam(name = "cspName", required = false) Csp csp,
            @Parameter(name = "type", description = "The type of credential.")
            @RequestParam(name = "type", required = false) CredentialType type) {
        return credentialCenter.listCredentials(csp, type, getUserId());
    }

    /**
     * Add user's credential for connecting to the cloud service provider.
     *
     * @param createCredential The credential to be created.
     */
    @Tag(name = "UserCloudCredentialsManagement",
            description = "APIs for managing user's cloud provider credentials")
    @PostMapping(value = "/credentials",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(description = "Add user's credential for connecting to the cloud service provider.")
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public void addUserCloudCredential(
            @Valid @RequestBody CreateCredential createCredential) {
        createCredential.setUserId(getUserId());
        credentialCenter.addCredential(createCredential);
    }

    /**
     * Update user's credential for connecting to the cloud service provider.
     *
     * @param updateCredential The credential to be updated.
     */
    @Tag(name = "UserCloudCredentialsManagement",
            description = "APIs for managing user's cloud provider credentials")
    @PutMapping(value = "/credentials",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(description =
            "Update user's credential for connecting to the cloud service provider.")
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public void updateUserCloudCredential(
            @Valid @RequestBody CreateCredential updateCredential) {
        updateCredential.setUserId(getUserId());
        credentialCenter.updateCredential(updateCredential);
    }

    /**
     * Delete user's credential for connecting to the cloud service provider.
     *
     * @param csp  The cloud service provider.
     * @param type The type of credential.
     * @param name The name of credential.
     */
    @Tag(name = "UserCloudCredentialsManagement",
            description = "APIs for managing user's cloud provider credentials")
    @DeleteMapping(value = "/credentials",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(description =
            "Delete user's credential for connecting to the cloud service provider.")
    @AuditApiRequest(methodName = "getCspFromRequestUri")
    public void deleteUserCloudCredential(
            @Parameter(name = "cspName", description = "The cloud service provider.")
            @RequestParam("cspName") Csp csp,
            @Parameter(name = "siteName", description = "The site of the provider.")
            @RequestParam(name = "siteName") String siteName,
            @Parameter(name = "type", description = "The type of credential.")
            @RequestParam(name = "type") CredentialType type,
            @Parameter(name = "name", description = "The name of of credential.")
            @RequestParam(name = "name") String name) {
        credentialCenter.deleteCredential(csp, siteName, type, name, getUserId());
    }

    private String getUserId() {
        return userServiceHelper.getCurrentUserId();
    }

}
