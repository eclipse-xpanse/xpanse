/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicechange;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.xpanse.modules.models.servicechange.enums.ServiceChangeStatus;

/** The query model for ServiceChangeRequestEntity. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceChangeRequestQueryModel {

    private UUID orderId;

    private UUID serviceId;

    private String resourceName;

    private String configManager;

    private ServiceChangeStatus status;
}
