package org.eclipse.xpanse.modules.billing.providers;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.xpanse.modules.database.service.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.enums.DeployResourceKind;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class HuaweiBillingServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    HuaweiBillingService huaweiBillingService;

    @Test
    void onDemandBilling_null() throws IOException {
        DeployServiceEntity service = mapper.readValue(
                new File("target/test-classes/service-test.json"),
                DeployServiceEntity.class);

//        List<BillingDataResponse> billings = huaweiBillingService.onDemandBilling(service, true);

        Assertions.assertThrows(NullPointerException.class,
                () -> huaweiBillingService.onDemandBilling(null, true));

//        Assertions.assertFalse(CollectionUtils.isEmpty(billings));

    }

    @Disabled
    @Test
    void onDemandBilling() throws IOException {
        DeployServiceEntity service = mapper.readValue(
                new File("target/test-classes/service-test-1.json"),
                DeployServiceEntity.class);

        for (DeployResourceEntity deployResourceEntity : service.getDeployResourceList()) {
            if (DeployResourceKind.VM.equals(deployResourceEntity.getKind())) {
                deployResourceEntity.setResourceId("8**");
                deployResourceEntity.setName("8**");
                Map<String, String> map = new HashMap<>();
                map.put("", "");
                deployResourceEntity.setProperty(map);
            }
            if (DeployResourceKind.DISK.equals(deployResourceEntity.getKind())) {
                deployResourceEntity.setResourceId("8**");
                deployResourceEntity.setName("8**");
                Map<String, String> map = new HashMap<>();
                map.put("", "");
                deployResourceEntity.setProperty(map);
            }

        }

//        List<BillingDataResponse> billings = huaweiBillingService.onDemandBilling(service, true);

//        Assertions.assertFalse(CollectionUtils.isEmpty(billings));

    }


    @Test
    void getCsp() {
    }
}