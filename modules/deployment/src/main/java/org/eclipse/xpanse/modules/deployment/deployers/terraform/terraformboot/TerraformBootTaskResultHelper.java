/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.exceptions.TerraformBootRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.api.RetrieveTerraformResultApi;
import org.eclipse.xpanse.modules.deployment.deployers.terraform.terraformboot.generated.model.TerraformResult;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Bean to manage task result via terraform-boot.
 */
@Slf4j
@Component
@Profile("terraform-boot")
public class TerraformBootTaskResultHelper {

    @Resource
    private RetrieveTerraformResultApi retrieveTerraformResultApi;

    public ResponseEntity<TerraformResult> retrieveTerraformResult(String requestId) {
        try {
            return retrieveTerraformResultApi.getStoredTaskResultByRequestIdWithHttpInfo(requestId);
        }catch (RestClientException e){
            log.error(e.getMessage());
            /*log.error("terraform-boot modify service failed. service id: {} , error:{} ",
                    deployTask.getServiceId(), e.getMessage());*/
            throw new TerraformBootRequestFailedException(e.getMessage());
        }
    }

}
