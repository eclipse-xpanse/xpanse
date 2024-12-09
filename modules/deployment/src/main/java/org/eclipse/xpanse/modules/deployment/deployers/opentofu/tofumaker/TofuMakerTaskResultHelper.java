/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.exceptions.OpenTofuMakerRequestFailedException;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.api.RetrieveOpenTofuResultApi;
import org.eclipse.xpanse.modules.deployment.deployers.opentofu.tofumaker.generated.model.OpenTofuResult;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Bean to manage task result via tofu-maker.
 */
@Slf4j
@Component
public class TofuMakerTaskResultHelper {

    @Resource
    private RetrieveOpenTofuResultApi retrieveOpenTofuResultApi;

    /**
     * retrieve openTofu result.
     */
    public ResponseEntity<OpenTofuResult> retrieveOpenTofuResult(String requestId) {
        try {
            return retrieveOpenTofuResultApi.getStoredTaskResultByRequestIdWithHttpInfo(requestId);
        } catch (RestClientException e) {
            log.error("terraform-boot modify service failed. orderId: {} , error:{} ",
                    requestId, e.getMessage());
            throw new OpenTofuMakerRequestFailedException(e.getMessage());
        }
    }

}