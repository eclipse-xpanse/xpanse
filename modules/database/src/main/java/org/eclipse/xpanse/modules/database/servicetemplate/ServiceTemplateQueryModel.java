/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */


package org.eclipse.xpanse.modules.database.servicetemplate;

import lombok.Data;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceRegistrationState;

/**
 * The query model for search register services.
 */
@Data
public class ServiceTemplateQueryModel {

    private Category category;

    private Csp csp;

    private String serviceName;

    private String serviceVersion;

    private ServiceHostingType serviceHostingType;

    private ServiceRegistrationState serviceRegistrationState;

    private boolean checkNamespace;

    private String namespace;

    /**
     * Constructor of ServiceTemplateQueryModel.
     */
    public ServiceTemplateQueryModel(Category category, Csp csp, String serviceName,
                                     String serviceVersion, ServiceHostingType serviceHostingType,
                                     ServiceRegistrationState serviceRegistrationState,
                                     boolean checkNamespace) {
        this.csp = csp;
        this.category = category;
        this.serviceName = serviceName;
        this.serviceVersion = serviceVersion;
        this.serviceHostingType = serviceHostingType;
        this.serviceRegistrationState = serviceRegistrationState;
        this.checkNamespace = checkNamespace;
    }
}
