/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.database.servicetemplate;

import lombok.Builder;
import lombok.Data;
import org.eclipse.xpanse.modules.models.common.enums.Category;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceHostingType;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateRegistrationState;

/** The query model for search register services. */
@Data
@Builder
public class ServiceTemplateQueryModel {

    private Category category;

    private Csp csp;

    private String serviceName;

    private String serviceVersion;

    private ServiceHostingType serviceHostingType;

    private ServiceTemplateRegistrationState serviceTemplateRegistrationState;

    private Boolean isAvailableInCatalog;

    private Boolean isReviewInProgress;

    private Boolean checkNamespace;

    private String namespace;
}
