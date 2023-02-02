/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.terraform;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import org.eclipse.osc.modules.ocl.loader.data.models.OclResource;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TFResourcesTest {

    @Disabled
    @Test
    public void TFExecutorBasicTest() throws Exception {
        String content =
                Files.readString(new File("target/test-classes/tfstate.json").toPath());
        ObjectMapper objectMapper = new ObjectMapper();
        TfState tfState = objectMapper.readValue(content, TfState.class);

        TfResources tfResources = new TfResources();
        tfResources.update(tfState);
        List<OclResource> oclResourceList = tfResources.getResources();
    }
}
