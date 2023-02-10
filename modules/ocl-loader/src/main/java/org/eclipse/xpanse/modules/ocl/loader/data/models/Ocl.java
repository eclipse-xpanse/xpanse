/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.Category;

/**
 * This is the parent class in the OCL data model.
 */
@Slf4j
@Data
public class Ocl {

    private static ObjectMapper theMapper = new ObjectMapper();

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "Name of the service to be managed by xpanse")
    private String name;

    @Valid
    @NotNull
    @Schema(description = "The category of the managed service")
    private Category category;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The namespace of the managed service")
    private String namespace;

    @Valid
    @Schema(description = "The billing of the managed service")
    private Billing billing;

    @Valid
    @Schema(description = "The compute resources for the managed service")
    private Compute compute;

    @Valid
    @Schema(description = "The network resources for the managed service")
    private Network network;

    @Valid
    @NotEmpty
    @Schema(description = "The storage resources for the managed service")
    private List<Storage> storages;

    /**
     * an OCL object might be passed to different plugins for processing, in case any plugin want to
     * change the property of Ocl, we should not change the original Object, we should change a deep
     * copy. The method is for deep copy
     *
     * @return copied Ocl object.
     */
    public Ocl deepCopy() {
        try {
            StringWriter out = new StringWriter();
            theMapper.writeValue(out, this);
            return theMapper.readValue(new StringReader(out.toString()), Ocl.class);
        } catch (IOException ex) {
            // Should not happen , since we don't actually touch any real I/O device
            throw new IllegalStateException("Deep copy failed", ex);

        }
    }

    /**
     * Method to get object the specified location from the JSON.
     *
     * @param jsonPath  Path to be retrieved from the JSON.
     * @param valueType Type of the value expected in the path.
     * @param <T>       Type of the value expected in the JSON path.
     * @return value found in the JSON path specified.
     */
    @JsonIgnore
    @SuppressWarnings("unchecked")
    public <T> Optional<T> referTo(String jsonPath, Class<T> valueType) {

        if (!jsonPath.startsWith("$.")) {
            log.warn("{} is not a valid JsonPath.", jsonPath);
            return Optional.empty();
        }

        Matcher matcher = Pattern.compile("([A-Za-z_0-9]+(?=[$\\.\\[\\]]{1}))").matcher(jsonPath);

        Object object = this;
        while (matcher.find()) {
            String matchStr = matcher.group();
            if (matchStr.equals("$")) {
                continue;
            }

            try {
                try {
                    Integer index = Integer.parseInt(matchStr);
                    Method getter = object.getClass().getDeclaredMethod("get", int.class);
                    object = getter.invoke(object, index);
                } catch (NumberFormatException e) {
                    Method getter = object.getClass().getDeclaredMethod(
                            "get"
                                    + matchStr.substring(0, 1).toUpperCase(Locale.ROOT)
                                    + matchStr.substring(1));
                    object = getter.invoke(object);
                }
            } catch (Exception ex) {

                log.warn("Refer failed", ex);
                return Optional.empty();
            }
        }

        if (object.getClass() == valueType) {
            return Optional.of((T) object);
        } else {
            log.warn("Not the same type. Please check your JsonPath.");
            return Optional.empty();
        }
    }

}
