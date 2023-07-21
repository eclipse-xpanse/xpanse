/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.zitadel.config;

import io.swagger.v3.oas.models.Operation;
import java.util.Objects;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

/**
 * Customizer for appending required role to description of Swagger Annotation operation.
 */
@Profile("zitadel")
@Component
public class RequiredRoleDescriptionCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        // get annotation secured of method
        var securedAnnotation = handlerMethod.getMethodAnnotation(Secured.class);
        if (Objects.isNull(securedAnnotation)) {
            // get annotation secured of class
            securedAnnotation = handlerMethod.getBeanType().getAnnotation(Secured.class);
        }
        if (Objects.nonNull(securedAnnotation)) {
            String description = operation.getDescription() == null ? ""
                    : (operation.getDescription());
            operation.setDescription(description + "<br>**Required role: "
                    + String.join(" or ", securedAnnotation.value()) + "**");
        }
        return operation;
    }
}