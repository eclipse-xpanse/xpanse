package org.eclipse.xpanse.modules.billing.providers;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.database.service.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.DeployResourceKind;
import org.eclipse.xpanse.modules.models.service.BillingDataResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

@Slf4j
public class HuaweiBillingServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void onDemandBilling_null() throws IOException {
        DeployServiceEntity service = mapper.readValue(
                new File("target/test-classes/service-test.json"),
                DeployServiceEntity.class);

        HuaweiBillingService huaweiBillingService = new HuaweiBillingService();
        Assertions.assertThrows(NullPointerException.class,
                () -> huaweiBillingService.onDemandBilling(null, true));

    }

    @Test
    void onDemandBilling() throws IOException {
        DeployServiceEntity service = mapper.readValue(
                new File("target/test-classes/service-test-1.json"),
                DeployServiceEntity.class);
        List<DeployResourceEntity> deployResourceEntityList = new ArrayList<>();
        DeployResourceEntity deployResourceEntity1 = new DeployResourceEntity();
        deployResourceEntity1.setId(UUID.fromString("e97f9443-3850-40be-812b-c823fc652979"));
        deployResourceEntity1.setKind(DeployResourceKind.VM);
        deployResourceEntity1.setResourceId("d681840d-4f91-4148-b73d-ff2bc66786f5");
        deployResourceEntity1.setName("basic-wmm");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            deployResourceEntity1.setCreateTime(sdf.parse("2023-03-29 10:16:37.543"));
        } catch (ParseException e) {
            log.error("Error message.", e.getMessage());
        }
        Map<String, String> map1 = new HashMap<>();
        map1.put("service_type", "huaweicloud_compute_instance");
        map1.put("flavor_name", "s3.large.2");
        map1.put("system_disk_type", "GPSSD");
        map1.put("ip", "192.168.0.108");
        map1.put("create", "true");
        map1.put("region", "cn-southwest-2");
        map1.put("system_disk_size", "40");
        map1.put("image_id", "a8601887-81d5-4eed-9338-382cf5b6d80b");
        map1.put("image_name", "Ubuntu 18.04 server 64bit");
        deployResourceEntity1.setProperty(map1);
        deployResourceEntityList.add(deployResourceEntity1);
        DeployResourceEntity deployResourceEntity2 = new DeployResourceEntity();
        deployResourceEntity2.setId(UUID.fromString("4dbe2c13-5820-46c0-89d4-b3d5a6dfec9a"));
        deployResourceEntity2.setResourceId("c6e38620-1d1c-4537-8f61-fb4f30b85fd1");
        deployResourceEntity2.setName("volume");
        try {
            deployResourceEntity2.setCreateTime(sdf.parse("2023-03-29 10:16:37.543"));
        } catch (ParseException e) {
            log.error("Error message.", e.getMessage());
        }
        Map<String, String> map2 = new HashMap<>();
        map2.put("service_type", "huaweicloud_evs_volume");
        map2.put("size", "10");
        map2.put("create", "true");
        map2.put("type", "SAS");
        deployResourceEntity2.setProperty(map2);
        deployResourceEntityList.add(deployResourceEntity2);
        service.setDeployResourceList(deployResourceEntityList);
        HuaweiBillingService huaweiBillingService = new HuaweiBillingService();
        List<BillingDataResponse> billings = huaweiBillingService.onDemandBilling(service, true);
        System.out.println(billings);
        Assertions.assertFalse(CollectionUtils.isEmpty(billings));

    }

    @Test
    void getCsp() {
        HuaweiBillingService huaweiBillingService = new HuaweiBillingService();
        Assertions.assertEquals(Csp.HUAWEI, huaweiBillingService.getCsp());
    }
}