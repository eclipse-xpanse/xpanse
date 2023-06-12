/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.models.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Map;

/**
 * Data model for Resource objects.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "creator",
        "started_at",
        "revision_start",
        "ended_at",
        "user_id",
        "project_id",
        "original_resource_id",
        "id",
        "type",
        "name",
        "instance_id",
        "revision_end",
        "metrics",
        "created_by_user_id",
        "created_by_project_id"
})
public class InstanceNetworkResource {

    @JsonProperty("creator")
    private String creator;
    @JsonProperty("started_at")
    private String startedAt;
    @JsonProperty("revision_start")
    private String revisionStart;
    @JsonProperty("ended_at")
    private Object endedAt;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("project_id")
    private String projectId;
    @JsonProperty("original_resource_id")
    private String originalResourceId;
    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private String type;

    @JsonProperty("name")
    private String name;
    @JsonProperty("instance_id")
    private String instanceId;
    @JsonProperty("revision_end")
    private String revisionEnd;
    @JsonProperty("metrics")
    private Map<String, String> metrics;
    @JsonProperty("created_by_user_id")
    private String createdByUserId;
    @JsonProperty("created_by_project_id")
    private String createdByProjectId;

    @JsonProperty("creator")
    public String getCreator() {
        return creator;
    }

    @JsonProperty("creator")
    public void setCreator(String creator) {
        this.creator = creator;
    }

    @JsonProperty("started_at")
    public String getStartedAt() {
        return startedAt;
    }

    @JsonProperty("started_at")
    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    @JsonProperty("revision_start")
    public String getRevisionStart() {
        return revisionStart;
    }

    @JsonProperty("revision_start")
    public void setRevisionStart(String revisionStart) {
        this.revisionStart = revisionStart;
    }

    @JsonProperty("ended_at")
    public Object getEndedAt() {
        return endedAt;
    }

    @JsonProperty("ended_at")
    public void setEndedAt(Object endedAt) {
        this.endedAt = endedAt;
    }

    @JsonProperty("user_id")
    public String getUserId() {
        return userId;
    }

    @JsonProperty("user_id")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @JsonProperty("project_id")
    public String getProjectId() {
        return projectId;
    }

    @JsonProperty("project_id")
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @JsonProperty("original_resource_id")
    public String getOriginalResourceId() {
        return originalResourceId;
    }

    @JsonProperty("original_resource_id")
    public void setOriginalResourceId(String originalResourceId) {
        this.originalResourceId = originalResourceId;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("metrics")
    public Map<String, String> getMetrics() {
        return metrics;
    }

    @JsonProperty("metrics")
    public void setMetrics(Map<String, String> metrics) {
        this.metrics = metrics;
    }

    @JsonProperty("created_by_user_id")
    public String getCreatedByUserId() {
        return createdByUserId;
    }

    @JsonProperty("created_by_user_id")
    public void setCreatedByUserId(String createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    @JsonProperty("created_by_project_id")
    public String getCreatedByProjectId() {
        return createdByProjectId;
    }

    @JsonProperty("created_by_project_id")
    public void setCreatedByProjectId(String createdByProjectId) {
        this.createdByProjectId = createdByProjectId;
    }

    @JsonProperty("instance_id")
    public String getInstanceId() {
        return instanceId;
    }

    @JsonProperty("instance_id")
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty(value = "name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("revision_end")
    public String getRevisionEnd() {
        return revisionEnd;
    }

    @JsonProperty("revision_end")
    public void setRevisionEnd(String revisionEnd) {
        this.revisionEnd = revisionEnd;
    }

}
