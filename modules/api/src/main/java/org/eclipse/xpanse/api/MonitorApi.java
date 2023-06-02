/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.orchestrator.monitor.Monitor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Monitor Api.
 */
@Slf4j
@CrossOrigin
@RestController
@Validated
@RequestMapping("/xpanse")
public class MonitorApi {

    private final Monitor monitor;

    public MonitorApi(Monitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Get Monitor Metric.
     */
    @Tag(name = "Monitor",
            description = "APIs to get metrics of deployed services.")
    @Operation(description = "Get metric types.")
    @GetMapping(value = "/monitor/metric/types", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<MonitorResourceType> getMetricTypes() {
        return Arrays.asList(MonitorResourceType.values());
    }

    /**
     * Get Monitor Metric.
     *
     * @param id Service ID.
     */
    @Tag(name = "Monitor",
            description = "APIs to get metrics of deployed services.")
    @Operation(description = "Get metrics of the deployed service.")
    @GetMapping(value = "/monitor/metric/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<Metric> getMetrics(
            @Parameter(name = "id", description = "Id of the deployed service")
            @PathVariable(name = "id") String id,
            @Parameter(name = "monitorResourceType", description = "Types of the monitor resource")
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
                            + "metrics.")
            @RequestParam(name = "granularity", required = false, defaultValue = "1")
            Integer granularity) {

        return monitor.getMetrics(UUID.fromString(id), monitorResourceType, from, to, granularity);
    }

    /**
     * Get Monitor Metric.
     *
     * @param id Service ID.
     */
    @Tag(name = "Monitor",
            description = "APIs to get metrics of deployed services.")
    @Operation(description = "Get metrics of the deployed service.")
    @GetMapping(value = "/monitor/metric/service/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<Metric> getMetricsByServiceId(
            @Parameter(name = "id", description = "Id of the deployed service")
            @PathVariable(name = "id") String id,
            @Parameter(name = "monitorResourceType", description = "Types of the monitor resource.")
            @RequestParam(name = "monitorResourceType", required = false)
            MonitorResourceType monitorResourceType,
            @Parameter(name = "from", description = "Start UNIX timestamp in milliseconds.")
            @RequestParam(name = "from")
            @Min(value = 0, message = "The value cannot be less than 0") Long from,
            @Parameter(name = "to", description = "End UNIX timestamp in milliseconds.")
            @RequestParam(name = "to")
            @Min(value = 0, message = "The value cannot be less than 0") Long to,
            @Parameter(name = "granularity",
                    description = "Return metrics collected in provided time interval. This"
                            + " depends on how the source systems have generated/collected"
                            + "metrics.")
            @RequestParam(name = "granularity", required = false, defaultValue = "1")
            Integer granularity) {
        return monitor.getMetricsByServiceId(id, monitorResourceType, from, to,
                granularity);
    }


    /**
     * Get Monitor Metric.
     *
     * @param id Service ID.
     */
    @Tag(name = "Monitor",
            description = "APIs to get metrics of deployed services.")
    @Operation(description = "Get metrics of the deployed resource.")
    @GetMapping(value = "/monitor/metric/resource/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<Metric> getMetricsByResourceId(
            @Parameter(name = "id", description = "Id of the deployed resource.")
            @PathVariable(name = "id") String id,
            @Parameter(name = "monitorResourceType", description = "Types of the monitor resource.")
            @RequestParam(name = "monitorResourceType", required = false)
            MonitorResourceType monitorResourceType,
            @Parameter(name = "from", description = "Start UNIX timestamp in milliseconds.")
            @RequestParam(name = "from")
            @Min(value = 0, message = "The value cannot be less than 0") Long from,
            @Parameter(name = "to", description = "End UNIX timestamp in milliseconds.")
            @RequestParam(name = "to")
            @Min(value = 0, message = "The value cannot be less than 0") Long to,
            @Parameter(name = "granularity",
                    description = "Return metrics collected in provided time interval. This"
                            + " depends on how the source systems have generated/collected"
                            + "metrics.")
            @RequestParam(name = "granularity", required = false, defaultValue = "1")
            Integer granularity) {
        return monitor.getMetricsByResourceId(id, monitorResourceType, from, to,
                granularity);
    }

}
