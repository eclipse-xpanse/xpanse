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
        "flavor_id",
        "image_ref",
        "host",
        "display_name",
        "server_group",
        "flavor_name",
        "launched_at",
        "created_at",
        "deleted_at",
        "availability_zone",
        "revision_end",
        "metrics",
        "created_by_user_id",
        "created_by_project_id"
})
public class InstanceResource {

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
    @JsonProperty("flavor_id")
    private String flavorId;
    @JsonProperty("image_ref")
    private String imageRef;
    @JsonProperty("host")
    private String host;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("server_group")
    private Object serverGroup;
    @JsonProperty("flavor_name")
    private String flavorName;
    @JsonProperty("launched_at")
    private String launchedAt;
    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("deleted_at")
    private Object deletedAt;
    @JsonProperty("availability_zone")
    private Object availabilityZone;
    @JsonProperty("revision_end")
    private Object revisionEnd;
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

    @JsonProperty("flavor_id")
    public String getFlavorId() {
        return flavorId;
    }

    @JsonProperty("flavor_id")
    public void setFlavorId(String flavorId) {
        this.flavorId = flavorId;
    }

    @JsonProperty("image_ref")
    public String getImageRef() {
        return imageRef;
    }

    @JsonProperty("image_ref")
    public void setImageRef(String imageRef) {
        this.imageRef = imageRef;
    }

    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    @JsonProperty("host")
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty("display_name")
    public String getDisplayName() {
        return displayName;
    }

    @JsonProperty("display_name")
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @JsonProperty("server_group")
    public Object getServerGroup() {
        return serverGroup;
    }

    @JsonProperty("server_group")
    public void setServerGroup(Object serverGroup) {
        this.serverGroup = serverGroup;
    }

    @JsonProperty("flavor_name")
    public String getFlavorName() {
        return flavorName;
    }

    @JsonProperty("flavor_name")
    public void setFlavorName(String flavorName) {
        this.flavorName = flavorName;
    }

    @JsonProperty("launched_at")
    public String getLaunchedAt() {
        return launchedAt;
    }

    @JsonProperty("launched_at")
    public void setLaunchedAt(String launchedAt) {
        this.launchedAt = launchedAt;
    }

    @JsonProperty("created_at")
    public String getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty("deleted_at")
    public Object getDeletedAt() {
        return deletedAt;
    }

    @JsonProperty("deleted_at")
    public void setDeletedAt(Object deletedAt) {
        this.deletedAt = deletedAt;
    }

    @JsonProperty("availability_zone")
    public Object getAvailabilityZone() {
        return availabilityZone;
    }

    @JsonProperty("availability_zone")
    public void setAvailabilityZone(Object availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    @JsonProperty("revision_end")
    public Object getRevisionEnd() {
        return revisionEnd;
    }

    @JsonProperty("revision_end")
    public void setRevisionEnd(Object revisionEnd) {
        this.revisionEnd = revisionEnd;
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

}
