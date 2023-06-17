/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.register.register.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.eclipse.xpanse.modules.models.service.register.Ocl;

/**
 * IconProcessorUtil.
 */
public class IconProcessorUtil {

    private static final String URL_REGEX = "^(http|https|ftp)://[\\w-]+(\\.[\\w-]+)+([\\w-.,"
            + "@?^=%&:/~+#]*[\\w@?^=%&/~+#])?$";
    private static final int MAX_SIZE = 200;
    private static final int MAX_WIDTH = 100;
    private static final int MAX_HEIGHT = 100;


    /**
     * This method get the base64 icon.
     *
     * @param ocl the Ocl model describing the register service.
     */
    public static String processImage(Ocl ocl) {

        String icon = ocl.getIcon();
        icon = icon.trim();
        if (icon.matches(URL_REGEX)) {
            try {
                URL url = new URL(icon);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(20000);
                InputStream inputStream = connection.getInputStream();
                BufferedImage image = ImageIO.read(inputStream);
                if (image == null) {
                    throw new IllegalArgumentException("URL is not an image link.");
                }
                int width = image.getWidth();
                int height = image.getHeight();
                int size = getImageSizeInBytes(image);
                if (width <= MAX_WIDTH && height <= MAX_HEIGHT && size <= MAX_SIZE * 1024) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", out);
                    return "data:image/png;base64," + Base64.getEncoder().withoutPadding()
                            .encodeToString(out.toByteArray());
                } else {
                    throw new IllegalArgumentException(String.format(
                            "The icon does not exceed %sx%spx, and the size does not exceed %skb",
                            MAX_WIDTH, MAX_HEIGHT, MAX_SIZE));
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(
                        "Icon parameter URL format is not supported or invalid.");
            }
        }
        return icon;

    }

    private static int getImageSizeInBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        out.flush();
        int size = out.size();
        out.close();
        return size;
    }

}
