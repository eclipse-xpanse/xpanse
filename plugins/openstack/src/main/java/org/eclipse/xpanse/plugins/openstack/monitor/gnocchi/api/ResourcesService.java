/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api;

import java.util.Arrays;
import java.util.List;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.models.resources.InstanceNetworkResource;
import org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.models.resources.InstanceResource;
import org.springframework.stereotype.Component;

/**
 * Class to handle all Gnocchi resources services - <a href="https://gnocchi.osci.io/rest.html#resources">Resources</a>.
 */
@Component
public class ResourcesService extends BaseGnocchiServices {

    public InstanceResource getInstanceResourceInfoById(String resourceId) {
        return get(InstanceResource.class, uri("/v1/resource/instance/%s", resourceId)).execute();
    }

    /**
     * Gets the Network Resource information for Openstack instances.
     *
     * @param instanceId ID of an Openstack instance.
     * @return InstanceNetworkResource
     */
    public InstanceNetworkResource getInstanceNetworkResourceInfoByInstanceId(String instanceId) {
        List<InstanceNetworkResource> instanceNetworkResources = Arrays.stream(get(
                InstanceNetworkResource[].class,
                uri("/v1/resource/instance_network_interface")).execute()).toList();
        return instanceNetworkResources.stream()
                .filter(instanceNetworkResource ->
                        instanceNetworkResource.getInstanceId().equals(instanceId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("No network resource for instance id %s  found",
                                instanceId)));

    }
}
