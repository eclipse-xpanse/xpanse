/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.billing.providers;

import com.huaweicloud.sdk.bss.v2.BssClient;
import com.huaweicloud.sdk.bss.v2.model.DemandProductInfo;
import com.huaweicloud.sdk.bss.v2.model.DemandProductRatingResult;
import com.huaweicloud.sdk.bss.v2.model.ListOnDemandResourceRatingsRequest;
import com.huaweicloud.sdk.bss.v2.model.ListOnDemandResourceRatingsResponse;
import com.huaweicloud.sdk.bss.v2.model.RateOnDemandReq;
import com.huaweicloud.sdk.bss.v2.region.BssRegion;
import com.huaweicloud.sdk.core.auth.GlobalCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.exception.ConnectionException;
import com.huaweicloud.sdk.core.exception.RequestTimeoutException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.iam.v3.IamClient;
import com.huaweicloud.sdk.iam.v3.model.KeystoneListProjectsRequest;
import com.huaweicloud.sdk.iam.v3.model.KeystoneListProjectsResponse;
import com.huaweicloud.sdk.iam.v3.model.ProjectResult;
import com.huaweicloud.sdk.iam.v3.region.IamRegion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.billing.BillingService;
import org.eclipse.xpanse.modules.database.service.DeployResourceEntity;
import org.eclipse.xpanse.modules.database.service.DeployServiceEntity;
import org.eclipse.xpanse.modules.models.enums.Csp;
import org.eclipse.xpanse.modules.models.enums.DeployVariableKind;
import org.eclipse.xpanse.modules.models.resource.DeployVariable;
import org.eclipse.xpanse.modules.models.service.BillingDataResponse;
import org.eclipse.xpanse.modules.models.service.BillingProductResult;
import org.springframework.stereotype.Component;

/**
 * Huawei cloud resources billing.
 */
@Component
@Slf4j
public class HuaweiBillingService implements BillingService {

    @Override
    public List<BillingDataResponse> onDemandBilling(DeployServiceEntity deployServiceEntity,
            Boolean unit) {

        HuaweiBillingHandler huaweiBillingHandler = new HuaweiBillingHandler();
        BillingDataResponse billingDataResponse = new BillingDataResponse();
        List<BillingDataResponse> billingDataResponseList = new ArrayList<>();
        Map<String, String> variables = this.getEnv(deployServiceEntity);
        ICredential auth = new GlobalCredentials()
                .withAk(variables.get("HW_ACCESS_KEY"))
                .withSk(variables.get("HW_SECRET_KEY"));
        BssClient client = BssClient.newBuilder()
                .withCredential(auth)
                .withRegion(BssRegion.valueOf("cn-north-1"))
                .build();
        RateOnDemandReq body = new RateOnDemandReq();

        List<DemandProductInfo> listbodyProductInfos = new ArrayList<>();
        String region = deployServiceEntity.getCreateRequest().getRegion();
        for (DeployResourceEntity deployResourceEntity :
                deployServiceEntity.getDeployResourceList()) {
            if (deployResourceEntity.getId() != null) {
                List<DemandProductInfo> demandProductInfoList =
                        huaweiBillingHandler.handler(deployResourceEntity, region, unit);
                if (demandProductInfoList != null) {
                    for (DemandProductInfo demandProductInfo : demandProductInfoList) {
                        listbodyProductInfos.add(demandProductInfo);
                    }
                }
            }
        }
        body.withProductInfos(listbodyProductInfos);
        body.withProjectId(this.getProjectId(deployServiceEntity));
        ListOnDemandResourceRatingsRequest request = new ListOnDemandResourceRatingsRequest();
        request.withBody(body);
        try {
            ListOnDemandResourceRatingsResponse response =
                    client.listOnDemandResourceRatings(
                            request);
            billingDataResponse.setServiceId(deployServiceEntity.getId());
            billingDataResponse.setAmount(response.getAmount());
            billingDataResponse.setDiscountAmount(response.getDiscountAmount());
            billingDataResponse.setOfficialWebsiteAmount(
                    response.getOfficialWebsiteAmount());
            billingDataResponse.setMeasureId(response.getMeasureId());
            billingDataResponse.setCurrency(response.getCurrency());
            List<BillingProductResult> billingProductResultList = new ArrayList<>();
            for (DemandProductRatingResult product : response.getProductRatingResults()) {
                BillingProductResult billingProductResult = new BillingProductResult();
                billingProductResult.setProductId(product.getProductId());
                billingProductResult.setAmount(product.getAmount());
                billingProductResult.setDiscountAmount(product.getDiscountAmount());
                billingProductResult.setMeasureId(product.getMeasureId());
                billingProductResult.setId(product.getId());
                billingProductResult.setOfficialWebsiteAmount(
                        product.getOfficialWebsiteAmount());
                billingProductResultList.add(billingProductResult);
                billingDataResponse.setBillingProductResults(billingProductResultList);
            }
            billingDataResponseList.add(billingDataResponse);
        } catch (ConnectionException | RequestTimeoutException e) {
            log.error("Connection exception.", e.getMessage());
        } catch (ServiceResponseException e) {
            log.error("Service response exception.", e.getMessage());
            log.error("Error message.", e.getErrorMsg());
        }
        return billingDataResponseList;
    }

    @Override
    public Csp getCsp() {
        return Csp.HUAWEI;
    }

    /**
     * Get project id for billing.
     *
     * @param deployServiceEntity the deploy service entity.
     */
    private String getProjectId(DeployServiceEntity deployServiceEntity) {

        Map<String, String> variables = this.getEnv(deployServiceEntity);
        ICredential auth = new GlobalCredentials()
                .withAk(variables.get("HW_ACCESS_KEY"))
                .withSk(variables.get("HW_SECRET_KEY"));
        IamClient client = IamClient.newBuilder()
                .withCredential(auth)
                .withRegion(IamRegion.valueOf("cn-north-1"))
                .build();
        KeystoneListProjectsRequest projectsRequest = new KeystoneListProjectsRequest();
        try {
            KeystoneListProjectsResponse response = client.keystoneListProjects(projectsRequest);
            for (ProjectResult projectResult : response.getProjects()) {
                if (projectResult.getName()
                        .equals(deployServiceEntity.getCreateRequest().getRegion())) {
                    return projectResult.getId();
                }
            }
        } catch (ConnectionException | ServiceResponseException | RequestTimeoutException e) {
            log.error("Get project_id exception.", e.getMessage());
        }
        return null;
    }

    /**
     * Get environment variable for billing.
     *
     * @param deployServiceEntity the deploy service entity.
     */
    private Map<String, String> getEnv(DeployServiceEntity deployServiceEntity) {
        Map<String, String> variables = new HashMap<>();
        Map<String, String> request = deployServiceEntity.getCreateRequest().getProperty();
        for (DeployVariable variable : deployServiceEntity.getCreateRequest().getOcl()
                .getDeployment()
                .getContext()) {
            if (variable.getKind() == DeployVariableKind.ENV) {
                if (request.containsKey(variable.getName())) {
                    variables.put(variable.getName(), request.get(variable.getName()));
                } else {
                    variables.put(variable.getName(), System.getenv(variable.getName()));
                }
            }
            if (variable.getKind() == DeployVariableKind.FIX_ENV) {
                variables.put(variable.getName(), request.get(variable.getValue()));
            }
        }
        return variables;
    }

}
