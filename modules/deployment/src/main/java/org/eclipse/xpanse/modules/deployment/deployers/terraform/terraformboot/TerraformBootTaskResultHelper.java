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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Bean to manage task result via terraform-boot.
 */
@Slf4j
@Component
public class TerraformBootTaskResultHelper {

    @Resource
    private RetrieveTerraformResultApi retrieveTerraformResultApi;

    /**
     * retrieve terraform result.
     */
    public ResponseEntity<TerraformResult> retrieveTerraformResult(String requestId) {
        try {
            return retrieveTerraformResultApi.getStoredTaskResultByRequestIdWithHttpInfo(requestId);
        } catch (RestClientException e) {
            log.error("terraform-boot modify service failed. orderId: {} , error:{} ",
                    requestId, e.getMessage());
            throw new TerraformBootRequestFailedException(e.getMessage());
        }
    }

}