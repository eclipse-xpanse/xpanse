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
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.modify.ModifyServiceSpecifications;
import org.eclipse.xpanse.modules.models.service.deploy.DeployRequest;
import org.eclipse.xpanse.modules.security.IdentityProviderManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST interface methods for service modify.
 */
@Slf4j
@RestController
@RequestMapping("/xpanse")
@CrossOrigin
@Secured({ROLE_ADMIN, ROLE_USER})
public class ModifyServiceSpecificationsApi {

    @Resource
    private IdentityProviderManager identityProviderManager;

    @Resource
    private ModifyServiceSpecifications modifyServiceSpecifications;

    /**
     * Start a task to deploy registered service.
     *
     * @param deployRequest the managed service to create.
     * @return response
     */
    @Tag(name = "Service", description = "APIs to manage the service instances")
    @Operation(description = "Start a task to deploy service using registered service template.")
    @PutMapping(value = "/services/modify/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public UUID modify(@Valid @RequestBody
                               DeployRequest deployRequest, @Parameter(name = "id", description =
            "The id of deployed service")
                       @PathVariable("id") String id) {
        log.info("Starting modify service with name {}, version {}, csp {}",
                deployRequest.getServiceName(),
                deployRequest.getVersion(), deployRequest.getCsp());

        Optional<String> userIdOptional = identityProviderManager.getCurrentLoginUserId();
        deployRequest.setUserId(userIdOptional.orElse(null));
        deployRequest.setId(UUID.fromString(id));

        modifyServiceSpecifications.modify(deployRequest);
        String successMsg = String.format(
                "Task for starting managed service %s-%s-%s-%s started. UUID %s",
                deployRequest.getServiceName(),
                deployRequest.getVersion(),
                deployRequest.getCsp().toValue(),
                deployRequest.getServiceHostingType().toValue(),
                deployRequest.getId());
        log.info(successMsg);
        return deployRequest.getId();
    }
}
