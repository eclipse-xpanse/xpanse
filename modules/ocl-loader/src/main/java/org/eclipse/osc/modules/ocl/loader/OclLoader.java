/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
public class OclLoader {

    private final ObjectMapper mapper = new ObjectMapper();

    public Ocl getOcl(URL url) throws Exception {
        return mapper.readValue(url, Ocl.class);
    }

}
