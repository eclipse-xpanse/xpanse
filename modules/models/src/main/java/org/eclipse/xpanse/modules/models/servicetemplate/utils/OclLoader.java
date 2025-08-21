/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import org.eclipse.xpanse.modules.models.common.exceptions.XpanseUnhandledException;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.springframework.stereotype.Component;

/** Bean to deserialize Ocl data. */
@Component
public class OclLoader {

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public Ocl getOcl(URL url) throws IOException {
        return mapper.readValue(url, Ocl.class);
    }

    /** Loads the OCL file from the provided location. * */
    public Ocl getOclByLocation(String oclLocationValue) {
        try {
            return getOcl(URI.create(oclLocationValue).toURL());
        } catch (Exception e) {
            throw new XpanseUnhandledException(e.getMessage());
        }
    }
}
