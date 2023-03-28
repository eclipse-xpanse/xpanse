/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.monitor.providers;

import com.huaweicloud.sdk.ces.v1.CesClient;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataRequest;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataRequest.FilterEnum;
import com.huaweicloud.sdk.ces.v1.model.ShowMetricDataResponse;
import com.huaweicloud.sdk.ces.v1.region.CesRegion;
import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.exception.ConnectionException;
import com.huaweicloud.sdk.core.exception.RequestTimeoutException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.resource.DeployVariable;
import org.eclipse.xpanse.modules.models.service.MonitorDataResponse;
import org.eclipse.xpanse.modules.monitor.Monitor;
import org.eclipse.xpanse.modules.monitor.providers.model.HuaweiMonitorResource;
import org.springframework.stereotype.Component;

/**
 * Plugin to git monitor data on Huawei cloud.
 */
@Component
@Slf4j
public class HuaweiMonitor implements Monitor {

    /**
     * Get environment variable for monitor.
     *
     * @param deployServiceEntity the deploy service entity.
     */
    private ICredential getEnv(DeployServiceEntity deployServiceEntity) {
        Map<String, String> variables = new HashMap<>();
        Map<String, String> request = deployServiceEntity.getCreateRequest().getProperty();
        for (DeployVariable variable : deployServiceEntity.getCreateRequest().getOcl()
                .getDeployment()
                .getContext()) {
            if (variable.getKind() == DeployVariableKind.ENV) {
                if (request.containsKey(variable.getName())) {
                    variables.put(variable.getName(), request.get(variable.getName()));
                } else {
                    variables.put(variable.getName(), System.getenv(variable.getName()));
                }
            }
            if (variable.getKind() == DeployVariableKind.FIX_ENV) {
                variables.put(variable.getName(), request.get(variable.getValue()));
            }
        }
        String ak = variables.get("HW_ACCESS_KEY");
        String sk = variables.get("HW_SECRET_KEY");
        ICredential auth = new BasicCredentials()
                .withAk(ak)
                .withSk(sk);
        return auth;
    }

    @Override
    public List<MonitorDataResponse> cpuUsage(DeployServiceEntity deployServiceEntity,
            Boolean monitorAgentEnabled, String fromTime, String toTime) {
        MonitorResourceHandler monitorResourceHandler = new MonitorResourceHandler();
        List<MonitorDataResponse> monitorDataResponseList = new ArrayList<>();
        for (DeployResourceEntity deployResourceEntity :
                deployServiceEntity.getDeployResourceList()) {
            if (deployResourceEntity.getResourceId() != null && deployResourceEntity.getKind()
                    .equals(DeployResourceKind.VM)) {
                HuaweiMonitorResource huaweiMonitorResource = monitorResourceHandler.handler(
                        deployResourceEntity, fromTime, toTime);
                CesClient client = CesClient.newBuilder()
                        .withCredential(this.getEnv(deployServiceEntity))
                        .withRegion(CesRegion.valueOf(
                                deployServiceEntity.getCreateRequest().getRegion()))
                        .build();
                ShowMetricDataRequest request = new ShowMetricDataRequest()
                        .withDim0(huaweiMonitorResource.getDim0())
                        .withFilter(FilterEnum.valueOf(huaweiMonitorResource.getFilter()))
                        .withPeriod(huaweiMonitorResource.getPeriod())
                        .withFrom(Long.valueOf(huaweiMonitorResource.getFrom()))
                        .withTo(Long.valueOf(huaweiMonitorResource.getTo()));
                if (monitorAgentEnabled) {
                    request.withNamespace("AGT.ECS").withMetricName("cpu_usage");
                } else {
                    request.withNamespace("SYS.ECS").withMetricName("cpu_util");
                }
                try {
                    ShowMetricDataResponse response = client.showMetricData(request);
                    MonitorResourceHandler monitorResourceHandle = new MonitorResourceHandler();
                    MonitorDataResponse monitorDataResponse =
                            monitorResourceHandle.convert(response,
                                    deployResourceEntity.getResourceId());
                    monitorDataResponseList.add(monitorDataResponse);
                } catch (ConnectionException e) {
                    log.error("Connection exception.", e.getMessage());
                } catch (RequestTimeoutException e) {
                    log.error("Request timeout exception.", e.getMessage());
                } catch (ServiceResponseException e) {
                    log.error("Service response exception.", e.getMessage());
                    log.error("Error message.", e.getErrorMsg());
                }
            }
        }
        return monitorDataResponseList;
    }

    @Override
    public List<MonitorDataResponse> memUsage(DeployServiceEntity deployServiceEntity,
            Boolean monitorAgentEnabled, String fromTime, String toTime) {
        MonitorResourceHandler monitorResourceHandler = new MonitorResourceHandler();
        List<MonitorDataResponse> monitorDataResponseList = new ArrayList<>();
        for (DeployResourceEntity deployResourceEntity :
                deployServiceEntity.getDeployResourceList()) {
            if (deployResourceEntity.getResourceId() != null && deployResourceEntity.getKind()
                    .equals(DeployResourceKind.VM)) {
                HuaweiMonitorResource huaweiMonitorResource = monitorResourceHandler.handler(
                        deployResourceEntity, fromTime, toTime);
                CesClient client = CesClient.newBuilder()
                        .withCredential(this.getEnv(deployServiceEntity))
                        .withRegion(CesRegion.valueOf(
                                deployServiceEntity.getCreateRequest().getRegion()))
                        .build();
                ShowMetricDataRequest request = new ShowMetricDataRequest()
                        .withDim0(huaweiMonitorResource.getDim0())
                        .withFilter(FilterEnum.valueOf(huaweiMonitorResource.getFilter()))
                        .withPeriod(huaweiMonitorResource.getPeriod())
                        .withFrom(Long.valueOf(huaweiMonitorResource.getFrom()))
                        .withTo(Long.valueOf(huaweiMonitorResource.getTo()));
                if (monitorAgentEnabled) {
                    request.withNamespace("AGT.ECS").withMetricName("mem_usedPercent");
                } else {
                    request.withNamespace("SYS.ECS").withMetricName("mem_util");
                }
                try {
                    ShowMetricDataResponse response = client.showMetricData(request);
                    MonitorResourceHandler monitorResourceHandle = new MonitorResourceHandler();
                    MonitorDataResponse monitorDataResponse =
                            monitorResourceHandle.convert(response,
                                    deployResourceEntity.getResourceId());
                    monitorDataResponseList.add(monitorDataResponse);
                } catch (ConnectionException e) {
                    log.error("Connection exception.", e.getMessage());
                } catch (RequestTimeoutException e) {
                    log.error("Request timeout exception.", e.getMessage());
                } catch (ServiceResponseException e) {
                    log.error("Service response exception.", e.getMessage());
                    log.error("Error message.", e.getErrorMsg());
                }
            }
        }
        return monitorDataResponseList;
    }

    @Override
    public Csp getCsp() {
        return Csp.HUAWEI;
    }
}
