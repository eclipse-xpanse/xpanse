package org.eclipse.xpanse.modules.models.serviceconfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.servicetemplate.AnsibleScriptConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

public class ServiceChangeRequestTest {

    public static final UUID CHANGE_ID = UUID.randomUUID();
    public static final Map<String, Object> configParameters = Map.of("changeId", CHANGE_ID);
    public static final Map<String, Object> ansibleInventory = Map.of("changeId", CHANGE_ID);
    public static final AnsibleScriptConfig ansibleScriptConfig = new AnsibleScriptConfig();
    private final ServiceChangeRequest changeRequestForAgent = new ServiceChangeRequest();

    @BeforeEach
    void setUp() {
        changeRequestForAgent.setChangeId(CHANGE_ID);
        changeRequestForAgent.setConfigParameters(configParameters);
        changeRequestForAgent.setAnsibleInventory(ansibleInventory);
        changeRequestForAgent.setAnsibleScriptConfig(ansibleScriptConfig);
        changeRequestForAgent.setChangeId(CHANGE_ID);
    }

    @Test
    void testGetters() {
        assertEquals(CHANGE_ID, changeRequestForAgent.getChangeId());
        assertEquals(configParameters, changeRequestForAgent.getConfigParameters());
        assertEquals(ansibleInventory, changeRequestForAgent.getAnsibleInventory());
        assertEquals(ansibleScriptConfig, changeRequestForAgent.getAnsibleScriptConfig());
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertThat(changeRequestForAgent).isNotEqualTo(obj);
        assertThat(changeRequestForAgent.hashCode()).isNotEqualTo(obj.hashCode());
        ServiceChangeRequest test1 = new ServiceChangeRequest();
        assertThat(changeRequestForAgent).isNotEqualTo(test1);
        assertThat(changeRequestForAgent.hashCode()).isNotEqualTo(test1.hashCode());
        ServiceChangeRequest test2 = new ServiceChangeRequest();
        BeanUtils.copyProperties(changeRequestForAgent, test2);
        assertThat(changeRequestForAgent).isEqualTo(test2);
        assertThat(changeRequestForAgent.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testCanEqual() {
        assertThat(changeRequestForAgent.canEqual("other")).isFalse();
        assertThat(changeRequestForAgent.canEqual(new ServiceChangeRequest())).isTrue();
    }

    @Test
    void testToString() {
        String result =
                "ServiceChangeRequest(changeId="
                        + CHANGE_ID
                        + ", configParameters="
                        + configParameters
                        + ", ansibleScriptConfig="
                        + ansibleScriptConfig
                        + ", ansibleInventory="
                        + ansibleInventory
                        + ")";
        assertThat(changeRequestForAgent.toString()).isEqualTo(result);
    }
}
