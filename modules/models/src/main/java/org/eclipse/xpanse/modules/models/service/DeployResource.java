/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.service;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Map;
import lombok.Data;
import org.eclipse.xpanse.modules.models.enums.DeployResourceKind;

/**
 * DeployResource model.
 */
@Data
public class DeployResource {

    /**
     * The ID of the deployed resource.
     */
    private String resourceId;
    /**
     * The name of the deployed resource.
     */
    private String name;
    /**
     * The kind of the deployed resource.
     */
    @Enumerated(EnumType.STRING)
    private DeployResourceKind kind;

    /**
     * The property of the deployed resource.
     */
    private Map<String, String> property;
}
