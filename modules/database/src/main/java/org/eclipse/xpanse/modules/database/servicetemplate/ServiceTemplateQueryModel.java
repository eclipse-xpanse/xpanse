/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */


package org.eclipse.xpanse.modules.database.servicetemplate;

import lombok.Data;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;

/**
 * The query model for search register services.
 */
@Data
public class ServiceTemplateQueryModel {

    private Csp csp;

    private Category category;

    private String serviceName;

    private String serviceVersion;

    private String namespace;

    private ServiceHostingType serviceHostingType;
}
