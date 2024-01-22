/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */


package org.eclipse.xpanse.modules.database.service;

import lombok.Data;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.service.deploy.enums.ServiceDeploymentState;

/**
 * The query model for search services.
 */
@Data
public class ServiceQueryModel {

    private Csp csp;

    private Category category;

    private String serviceName;

    private String serviceVersion;

    private ServiceDeploymentState serviceState;

    private String userId;

}
