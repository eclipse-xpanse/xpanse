/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */


package org.eclipse.xpanse.modules.database.servicetemplaterequest;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestStatus;
import org.eclipse.xpanse.modules.models.servicetemplate.request.enums.ServiceTemplateRequestType;

/**
 * The query model for search service template request history.
 */
@Data
@Builder
public class ServiceTemplateRequestHistoryQueryModel {

    private Csp csp;

    private UUID serviceTemplateId;

    private ServiceTemplateRequestType requestType;

    private ServiceTemplateRequestStatus status;

}
