/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor;

import java.util.List;
import org.eclipse.xpanse.modules.credential.AbstractCredentialInfo;
import org.eclipse.xpanse.modules.models.service.DeployResource;

/**
 * The interface for the monitor metrics exporter.
 */
public interface MetricsExporter {

    /**
     * Get metrics of the @deployResource.
     *
     * @param deployResource the deployed resource of the service.
     */
    List<Metric> getMetrics(AbstractCredentialInfo credential, DeployResource deployResource);

}
