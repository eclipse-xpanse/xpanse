/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.api;

import org.eclipse.xpanse.orchestrator.plugin.openstack.monitor.gnocchi.models.resources.Resource;
import org.springframework.stereotype.Component;

/**
 * Class to handle all Gnocchi resources services - <a href="https://gnocchi.osci.io/rest.html#resources">Resources</a>.
 */
@Component
public class ResourcesService extends BaseGnocchiServices {

    public Resource getInstanceResourceInfoById(String resourceId) {
        return get(Resource.class, uri("/v1/resource/instance/%s", resourceId)).execute();
    }
}
