/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.openstack;

import java.util.Optional;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.image.v2.Image;
import org.springframework.stereotype.Component;

/**
 * Bean to handle all requests to Glance APIs.
 */
@Component
public class GlanceManager {

    /**
     * Method to get the image ID based on the image name.
     *
     * @param osClient  Fully initialized client to connect to an Openstack installation.
     * @param imageName Name of the image in query.
     * @return ID of the image. This is a unique value allocated by Openstack for each image
     * uploaded.
     */
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
