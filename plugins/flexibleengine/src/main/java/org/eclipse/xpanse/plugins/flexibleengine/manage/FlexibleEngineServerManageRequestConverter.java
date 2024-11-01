/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.flexibleengine.manage;

import com.huaweicloud.sdk.ecs.v2.model.BatchRebootServersRequest;
import com.huaweicloud.sdk.ecs.v2.model.BatchRebootServersRequestBody;
import com.huaweicloud.sdk.ecs.v2.model.BatchRebootSeversOption;
import com.huaweicloud.sdk.ecs.v2.model.BatchStartServersOption;
import com.huaweicloud.sdk.ecs.v2.model.BatchStartServersRequest;
import com.huaweicloud.sdk.ecs.v2.model.BatchStartServersRequestBody;
import com.huaweicloud.sdk.ecs.v2.model.BatchStopServersOption;
import com.huaweicloud.sdk.ecs.v2.model.BatchStopServersOption.TypeEnum;
import com.huaweicloud.sdk.ecs.v2.model.BatchStopServersRequest;
import com.huaweicloud.sdk.ecs.v2.model.BatchStopServersRequestBody;
import com.huaweicloud.sdk.ecs.v2.model.ServerId;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.resource.ServiceResourceEntity;
import org.springframework.stereotype.Component;

/**
 * ECS Server Manage Request Converter for FlexibleEngine.
 */
@Slf4j
@Component
public class FlexibleEngineServerManageRequestConverter {

    /**
     * Build BatchStartServersRequest Object.
     */
    public BatchStartServersRequest buildBatchStartServersRequest(
            List<ServiceResourceEntity> serviceResourceEntityList) {
        BatchStartServersRequest request = new BatchStartServersRequest();
        BatchStartServersRequestBody body = new BatchStartServersRequestBody();
        BatchStartServersOption osStart = new BatchStartServersOption();
        List<ServerId> serverIdList = getServerIdList(serviceResourceEntityList);
        osStart.withServers(serverIdList);
        body.withOsStart(osStart);
        request.withBody(body);
        return request;
    }

    /**
     * Build BatchStopServersRequest Object.
     */
    public BatchStopServersRequest buildBatchStopServersRequest(
            List<ServiceResourceEntity> serviceResourceEntityList) {
        BatchStopServersRequest request = new BatchStopServersRequest();
        BatchStopServersRequestBody body = new BatchStopServersRequestBody();
        BatchStopServersOption osStop = new BatchStopServersOption();
        List<ServerId> serverIdList = getServerIdList(serviceResourceEntityList);
        osStop.withServers(serverIdList).withType(TypeEnum.SOFT);
        body.withOsStop(osStop);
        request.withBody(body);
        return request;
    }

    /**
     * Build BatchRebootServersRequest Object.
     */
    public BatchRebootServersRequest buildBatchRebootServersRequest(
            List<ServiceResourceEntity> serviceResourceEntityList) {
        BatchRebootServersRequest request = new BatchRebootServersRequest();
        BatchRebootServersRequestBody body = new BatchRebootServersRequestBody();
        BatchRebootSeversOption reboot = new BatchRebootSeversOption();
        List<ServerId> serverIdList = getServerIdList(serviceResourceEntityList);
        reboot.withServers(serverIdList).withType(BatchRebootSeversOption.TypeEnum.SOFT);
        body.withReboot(reboot);
        request.withBody(body);
        return request;

    }

    private List<ServerId> getServerIdList(List<ServiceResourceEntity> serviceResourceEntityList) {
        return serviceResourceEntityList.stream()
                .map(ServiceResourceEntity::getResourceId)
                .map(uuid -> new ServerId().withId(uuid))
                .collect(Collectors.toList());
    }

}