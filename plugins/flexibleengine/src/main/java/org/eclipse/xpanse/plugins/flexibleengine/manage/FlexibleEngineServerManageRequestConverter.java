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
import org.eclipse.xpanse.modules.database.resource.DeployResourceEntity;
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
            List<DeployResourceEntity> deployResourceEntityList) {
        BatchStartServersRequest request = new BatchStartServersRequest();
        BatchStartServersRequestBody body = new BatchStartServersRequestBody();
        BatchStartServersOption osStart = new BatchStartServersOption();
        List<ServerId> serverIdList = getServerIdList(deployResourceEntityList);
        osStart.withServers(serverIdList);
        body.withOsStart(osStart);
        request.withBody(body);
        return request;
    }

    /**
     * Build BatchStopServersRequest Object.
     */
    public BatchStopServersRequest buildBatchStopServersRequest(
            List<DeployResourceEntity> deployResourceEntityList) {
        BatchStopServersRequest request = new BatchStopServersRequest();
        BatchStopServersRequestBody body = new BatchStopServersRequestBody();
        BatchStopServersOption osStop = new BatchStopServersOption();
        List<ServerId> serverIdList = getServerIdList(deployResourceEntityList);
        osStop.withServers(serverIdList).withType(TypeEnum.SOFT);
        body.withOsStop(osStop);
        request.withBody(body);
        return request;
    }

    /**
     * Build BatchRebootServersRequest Object.
     */
    public BatchRebootServersRequest buildBatchRebootServersRequest(
            List<DeployResourceEntity> deployResourceEntityList) {
        BatchRebootServersRequest request = new BatchRebootServersRequest();
        BatchRebootServersRequestBody body = new BatchRebootServersRequestBody();
        BatchRebootSeversOption reboot = new BatchRebootSeversOption();
        List<ServerId> serverIdList = getServerIdList(deployResourceEntityList);
        reboot.withServers(serverIdList).withType(BatchRebootSeversOption.TypeEnum.SOFT);
        body.withReboot(reboot);
        request.withBody(body);
        return request;

    }

    private List<ServerId> getServerIdList(List<DeployResourceEntity> deployResourceEntityList) {
        return deployResourceEntityList.stream()
                .map(DeployResourceEntity::getResourceId)
                .map(uuid -> new ServerId().withId(uuid))
                .collect(Collectors.toList());
    }

}