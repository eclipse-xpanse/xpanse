package org.eclipse.xpanse.modules.billing.providers;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.junit.jupiter.api.Assertions;
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

    @Test
    void onDemandBilling() throws IOException {
        DeployServiceEntity service = mapper.readValue(
                new File("target/test-classes/service-test-1.json"),
                DeployServiceEntity.class);

//        List<BillingDataResponse> billings = huaweiBillingService.onDemandBilling(service, true);

//        Assertions.assertFalse(CollectionUtils.isEmpty(billings));

    }


    @Test
    void getCsp() {
    }
}