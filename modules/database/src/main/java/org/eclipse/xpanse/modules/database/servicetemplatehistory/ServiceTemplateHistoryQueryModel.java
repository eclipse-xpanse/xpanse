/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */


package org.eclipse.xpanse.modules.database.servicetemplatehistory;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import org.eclipse.xpanse.modules.models.common.enums.Csp;
import org.eclipse.xpanse.modules.models.servicetemplate.change.enums.ServiceTemplateChangeStatus;
import org.eclipse.xpanse.modules.models.servicetemplate.change.enums.ServiceTemplateRequestType;

/**
 * The query model for search service template history.
 */
@Data
@Builder
public class ServiceTemplateHistoryQueryModel {

    private Csp csp;

    private UUID serviceTemplateId;

    private ServiceTemplateRequestType requestType;

    private ServiceTemplateChangeStatus status;

}
