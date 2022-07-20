package org.eclipse.osc.core.ocl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;

import java.net.URL;

@Data
public class Ocl {

    private Osc osc;
    private Billing billing;
    private Network network;

    public static Ocl load(URL url) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(url, Ocl.class);
    }

}
