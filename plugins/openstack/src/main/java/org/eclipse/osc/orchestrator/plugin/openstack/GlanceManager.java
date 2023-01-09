package org.eclipse.osc.orchestrator.plugin.openstack;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.image.v2.Image;

import java.util.Optional;

public class GlanceManager {

    public static String getImageId(OSClient.OSClientV3 osClient, String imageName) {
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
