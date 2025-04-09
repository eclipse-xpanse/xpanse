/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicechange;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import org.eclipse.xpanse.modules.models.servicechange.enums.ServiceChangeStatus;

/** The query model for ServiceChangeRequestEntity. */
@Data
@Builder
public class ServiceChangeRequestQueryModel {

    private UUID orderId;

    private UUID serviceId;

    private String resourceName;

    private String changeHandler;

    private ServiceChangeStatus status;
}
