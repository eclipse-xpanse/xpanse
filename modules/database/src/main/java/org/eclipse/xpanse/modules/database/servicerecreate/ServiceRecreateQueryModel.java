/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.database.servicerecreate;

import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.modules.models.workflow.recreate.enums.RecreateStatus;

/**
 * The query model for search ServiceRecreate.
 */
@Data
public class ServiceRecreateQueryModel {

    private UUID recreateId;

    private UUID serviceId;

    private RecreateStatus recreateStatus;

    private String userId;
}
