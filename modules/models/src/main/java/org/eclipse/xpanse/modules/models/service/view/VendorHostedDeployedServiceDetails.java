/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.service.view;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** Define view object for vendor hosted detail of the deployed service. */
@EqualsAndHashCode(callSuper = true)
@Data
public class VendorHostedDeployedServiceDetails extends DeployedService {}
