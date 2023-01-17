package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IPToolTest {

    @Test
    public void getExternalIpTest() {
        IPTool ipTool = new IPTool();
        String myIp = ipTool.probeExternalIp();

        Assertions.assertNotNull(myIp);
    }
}
