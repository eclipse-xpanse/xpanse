/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.monitor.Metric;
import org.eclipse.xpanse.modules.monitor.enums.MonitorResourceType;
import org.eclipse.xpanse.orchestrator.monitor.Monitor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RequestMapping("/xpanse")
public class MonitorApi {

    private final Monitor monitor;

    public MonitorApi(Monitor monitor) {
        this.monitor = monitor;
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
                    MonitorResourceType monitorResourceType) {
        return monitor.getMetrics(UUID.fromString(id), monitorResourceType);
    }
}
