package org.eclipse.xpanse.modules.models.serviceconfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.UUID;
import org.eclipse.xpanse.modules.models.servicetemplate.AnsibleScriptConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

public class ServiceConfigurationChangeRequestTest {

    public static final UUID ORDER_ID = UUID.randomUUID();
    public static final Map<String, Object> configParameters = Map.of("orderId", ORDER_ID);
    public static final Map<String, Object> ansibleInventory = Map.of("orderId", ORDER_ID);
    public static final AnsibleScriptConfig ansibleScriptConfig= new AnsibleScriptConfig();
    private final ServiceConfigurationChangeRequest changeRequestForAgent = new ServiceConfigurationChangeRequest();

    @BeforeEach
    void setUp() {
        changeRequestForAgent.setOrderId(ORDER_ID);
        changeRequestForAgent.setConfigParameters(configParameters);
        changeRequestForAgent.setAnsibleInventory(ansibleInventory);
        changeRequestForAgent.setAnsibleScriptConfig(ansibleScriptConfig);
        changeRequestForAgent.setOrderId(ORDER_ID);
    }

    @Test
    void testGetters() {
        assertEquals(ORDER_ID, changeRequestForAgent.getOrderId());
        assertEquals(configParameters, changeRequestForAgent.getConfigParameters());
        assertEquals(ansibleInventory, changeRequestForAgent.getAnsibleInventory());
        assertEquals(ansibleScriptConfig, changeRequestForAgent.getAnsibleScriptConfig());
    }

    @Test
    void testEqualsAndHashCode() {
        Object obj = new Object();
        assertThat(changeRequestForAgent).isNotEqualTo(obj);
        assertThat(changeRequestForAgent.hashCode()).isNotEqualTo(obj.hashCode());
        ServiceConfigurationChangeRequest test1 = new ServiceConfigurationChangeRequest();
        assertThat(changeRequestForAgent).isNotEqualTo(test1);
        assertThat(changeRequestForAgent.hashCode()).isNotEqualTo(test1.hashCode());
        ServiceConfigurationChangeRequest test2 = new ServiceConfigurationChangeRequest();
        BeanUtils.copyProperties(changeRequestForAgent, test2);
        assertThat(changeRequestForAgent).isEqualTo(test2);
        assertThat(changeRequestForAgent.hashCode()).isEqualTo(test2.hashCode());
    }

    @Test
    void testCanEqual() {
        assertThat(changeRequestForAgent.canEqual("other")).isFalse();
        assertThat(changeRequestForAgent.canEqual(new ServiceConfigurationChangeRequest())).isTrue();
    }

    @Test
    void testToString() {
        String result = "ServiceConfigurationChangeRequest(orderId=" + ORDER_ID
                + ", configParameters=" + configParameters
                + ", ansibleScriptConfig=" + ansibleScriptConfig
                + ", ansibleInventory=" + ansibleInventory
                + ")";
        assertThat(changeRequestForAgent.toString()).isEqualTo(result);
    }
}
