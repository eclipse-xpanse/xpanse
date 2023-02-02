/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.openstack;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.image.v2.Image;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GlanceManager {

    public String getImageId(OSClient.OSClientV3 osClient, String imageName) {
        Optional<? extends Image> image;
        image = osClient.imagesV2()
                .list()
                .stream()
                .filter(imageInfo -> imageInfo.getName().equalsIgnoreCase(imageName))
                .findAny();
        if (image.isEmpty()) {
            throw new RuntimeException("No image with name " + imageName + " found");
        }
        return image.get().getId();
    }
}
