package org.eclipse.xpanse.modules.servicetemplate.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.xpanse.modules.models.service.common.enums.Category;
import org.eclipse.xpanse.modules.models.servicetemplate.Ocl;
import org.eclipse.xpanse.modules.models.servicetemplate.exceptions.IconProcessingFailedException;
import org.junit.jupiter.api.Test;

class IconProcessorUtilTest {

    @Test
    void testProcessImage() {
        // Setup
        final Ocl ocl = new Ocl();
        ocl.setCategory(Category.AI);
        ocl.setVersion("version");
        ocl.setName("name");
        ocl.setServiceVersion("serviceVersion");
        ocl.setIcon("icon");

        // Run the test
        final String result = IconProcessorUtil.processImage(ocl);

        // Verify the results
        assertThat(result).isEqualTo("icon");
    }

    @Test
    void testProcessImageWithUrl() {
        // Setup
        String iconUrl = "https://avatars.githubusercontent.com/u/127229590?s=48&v=4";
        // Setup
        final Ocl ocl = new Ocl();
        ocl.setCategory(Category.AI);
        ocl.setVersion("version");
        ocl.setName("name");
        ocl.setServiceVersion("serviceVersion");
        ocl.setIcon(iconUrl);

        // Run the test
        final String result;
        try {
            result = IconProcessorUtil.processImage(ocl);
            // Verify the results
            assertNotNull(result);
            assertTrue(result.startsWith("data:image/png;base64,"));
        } catch (Exception e) {
            assertTrue(e instanceof IconProcessingFailedException);
        }

    }

    @Test
    void testProcessImageWithUrlException() {
        // Setup
        String iconUrl =
                "https://raw.githubusercontent.com/eclipse-xpanse/xpanse/main/static/full-logo.png";
        final Ocl ocl = new Ocl();
        ocl.setCategory(Category.AI);
        ocl.setVersion("version");
        ocl.setName("name");
        ocl.setServiceVersion("serviceVersion");
        ocl.setIcon(iconUrl);

        // Verify the results
        assertThrows(IconProcessingFailedException.class,
                () -> IconProcessorUtil.processImage(ocl));
    }


}
