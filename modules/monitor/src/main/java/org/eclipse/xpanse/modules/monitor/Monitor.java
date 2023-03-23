/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor;

import java.util.List;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.service.MonitorDataResponse;

/**
 * This interface describes the cpuUsage and memUsage for querying the server.
 */
public interface Monitor {

    /**
     * get the Csp of the monitor.
     */
    Csp getCsp();

    /**
     * Method to git service cpuUsage.
     *
     * @param deployServiceEntity the resource of the deployment.
     * @param monitorAgentEnabled the agent state.
     * @param fromTime            the start time of the monitor.
     * @param toTime              the start time of the monitor.
     */
    List<MonitorDataResponse> cpuUsage(DeployServiceEntity deployServiceEntity,
            Boolean monitorAgentEnabled, String fromTime, String toTime);

    /**
     * Method to git service memUsage.
     *
     * @param deployServiceEntity the resource of the deployment.
     * @param monitorAgentEnabled the agent state.
     * @param fromTime            the start time of the monitor.
     * @param toTime              the start time of the monitor.
     */
    List<MonitorDataResponse> memUsage(DeployServiceEntity deployServiceEntity,
            Boolean monitorAgentEnabled, String fromTime, String toTime);


}
