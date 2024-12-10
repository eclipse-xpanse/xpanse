/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot;

import jakarta.annotation.Resource;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.ServiceDeploymentEntity;
import org.eclipse.xpanse.modules.database.serviceorder.ServiceOrderEntity;
import org.eclipse.xpanse.modules.deployment.DeployResultManager;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.RetrieveTerraformResultApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.Response;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformResult;
import org.eclipse.xpanse.modules.models.response.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Bean to manage task result via terraform-boot.
 */
@Slf4j
@Component
public class TerraformBootResultRefetchManager {

    @Resource
    private DeployResultManager deployResultManager;
    @Resource
    private RetrieveTerraformResultApi retrieveTerraformResultApi;

    /**
     * retrieve terraform result.
     */
    public void retrieveTerraformResult(ServiceDeploymentEntity serviceDeployment,
                                        ServiceOrderEntity serviceOrder) {
        try {
            ResponseEntity<TerraformResult> result = retrieveTerraformResultApi
                    .getStoredTaskResultByRequestIdWithHttpInfo(String
                            .valueOf(serviceOrder.getOrderId()));
            if (result.getStatusCode() == HttpStatus.NO_CONTENT) {
                return;
            }
            if (Objects.nonNull(result.getBody())
                    && result.getBody().getCommandSuccessful() != null) {
                deployResultManager.updateServiceDeploymentState(
                        result.getBody().getCommandSuccessful(), serviceDeployment);
            }
        } catch (HttpClientErrorException e) {
            Response response = e.getResponseBodyAs(Response.class);
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST
                    && Objects.nonNull(response)
                    && response.getResultType()
                    == Response.ResultTypeEnum.RESULT_ALREADY_RETURNED_OR_REQUEST_ID_INVALID) {
                deployResultManager
                        .updateServiceDeploymentStateAndServiceOrder(serviceDeployment,
                                serviceOrder, ErrorType.TERRAFORM_BOOT_REQUEST_FAILED, e);
            } else {
                log.error(String.format("Refetch terraform result failed. requestId %s,"
                                + " error %s ", serviceOrder.getOrderId(), e.getMessage()));
            }
        }
    }

}