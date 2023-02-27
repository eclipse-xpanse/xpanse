/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.net.URL;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Ocl;
import org.eclipse.xpanse.modules.ocl.loader.data.models.Oclv2;
import org.springframework.stereotype.Component;

/**
 * Bean to deserialize Ocl data.
 */
@Component
public class OclLoader {

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper ymalMapper = new ObjectMapper(new YAMLFactory());

    public Ocl getOcl(URL url) throws Exception {
        return mapper.readValue(url, Ocl.class);
    }

    public Oclv2 getOclv2(URL url) throws Exception {
        return ymalMapper.readValue(url, Oclv2.class);
    }

}
