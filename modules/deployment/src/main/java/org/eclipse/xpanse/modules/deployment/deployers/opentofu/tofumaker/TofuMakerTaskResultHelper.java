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
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Bean to manage task result via tofu-maker.
 */
@Slf4j
@Component
@Profile("tofu-maker")
public class TofuMakerTaskResultHelper {

    @Resource
    private RetrieveOpenTofuResultApi retrieveOpenTofuResultApi;

    public ResponseEntity<OpenTofuResult> retrieveOpenTofuResult(String requestId) {
        try {
            return retrieveOpenTofuResultApi.getStoredTaskResultByRequestIdWithHttpInfo(requestId);
        }catch (RestClientException e){
            log.error(e.getMessage());
            /*log.error("terraform-boot modify service failed. service id: {} , error:{} ",
                    deployTask.getServiceId(), e.getMessage());*/
            throw new OpenTofuMakerRequestFailedException(e.getMessage());
        }
    }

}
