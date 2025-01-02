/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.orchestrator.servicetemplate;

import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.enums.ServiceTemplateReviewPluginResultType;

/** Service template management interface. */
public interface ServiceTemplateManager {

    /** Validate service template. */
    ServiceTemplateReviewPluginResultType validateServiceTemplate(Ocl ocl);

    /** Run necessary preparation steps. */
    void prepareServiceTemplate(Ocl ocl);
}
