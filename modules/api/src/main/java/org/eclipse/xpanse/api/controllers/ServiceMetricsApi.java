/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api.controllers;

import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_ADMIN;
import static org.eclipse.xpanse.modules.security.common.RoleConstants.ROLE_USER;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.api.config.AuditApiRequest;
import org.eclipse.xpanse.modules.models.monitor.Metric;
import org.eclipse.xpanse.modules.models.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.modules.monitor.ServiceMetricsAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API methods for monitoring deployed services.
 */
@Slf4j
@CrossOrigin
@RestController
@Validated
@RequestMapping("/xpanse")
@Secured({ROLE_ADMIN, ROLE_USER})
public class ServiceMetricsApi {

    private final ServiceMetricsAdapter serviceMetricsAdapter;

    public ServiceMetricsApi(ServiceMetricsAdapter serviceMetricsAdapter) {
        this.serviceMetricsAdapter = serviceMetricsAdapter;
    }

    /**
     * Get metrics of a deployed service or a resource.
     */
    @Tag(name = "Monitor",
            description = "APIs for getting metrics of deployed services.")
    @Operation(description = "Get metrics of a deployed service or a resource.")
    @GetMapping(value = "/metrics", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @AuditApiRequest(methodName = "getCspFromServiceId")
    public List<Metric> getMetrics(
            @Parameter(name = "serviceId", description = "Id of the deployed service")
            @RequestParam(name = "serviceId") String serviceId,
            @Parameter(name = "resourceId", description = "Id of resource in the deployed service")
            @RequestParam(name = "resourceId", required = false) String resourceId,
            @Parameter(name = "monitorResourceType", description = "Types of the monitor resource.")
            @RequestParam(name = "monitorResourceType", required = false)
                    MonitorResourceType monitorResourceType,
            @Parameter(name = "from", description = "Start UNIX timestamp in milliseconds. "
                    + "If no value filled,the default value is the UNIX timestamp in milliseconds"
                    + " of the five minutes ago.")
            @RequestParam(name = "from", required = false)
            @Min(value = 0, message = "The value cannot be less than 0") Long from,
            @Parameter(name = "to", description = "End UNIX timestamp in milliseconds. "
                    + "If no value filled,the default value is the UNIX timestamp in milliseconds "
                    + "of the current time.")
            @RequestParam(name = "to", required = false)
            @Min(value = 0, message = "The value cannot be less than 0") Long to,
            @Parameter(name = "granularity",
                    description = "Return metrics collected in provided time interval. This"
                            + " depends on how the source systems have generated/collected"
                            + " metrics.")
            @RequestParam(name = "granularity", required = false)
                    Integer granularity,
            @Parameter(name = "onlyLastKnownMetric",
                    description = "Returns only the last known metric. When this parameter is set "
                            + "then all other query parameters are ignored.")
            @RequestParam(name = "onlyLastKnownMetric", required = false, defaultValue = "false")
                    boolean onlyLastKnownMetric) {
        if (StringUtils.isNotBlank(resourceId)) {
            return serviceMetricsAdapter.getMetricsByResourceId(resourceId, monitorResourceType,
                    from, to, granularity, onlyLastKnownMetric);
        }
        return serviceMetricsAdapter.getMetricsByServiceId(serviceId, monitorResourceType, from, to,
                granularity, onlyLastKnownMetric);
    }

}
