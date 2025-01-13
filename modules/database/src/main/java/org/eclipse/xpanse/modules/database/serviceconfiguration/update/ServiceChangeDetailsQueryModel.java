/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.serviceconfiguration.update;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.xpanse.modules.models.serviceconfiguration.enums.ServiceChangeStatus;

/** The query model for ServiceChangeDetailsEntity. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceChangeDetailsQueryModel {

    private UUID orderId;

    private UUID serviceId;

    private String resourceName;

    private String configManager;

    private ServiceChangeStatus status;
}
