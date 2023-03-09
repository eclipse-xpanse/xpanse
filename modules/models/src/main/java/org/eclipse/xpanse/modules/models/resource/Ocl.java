/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.models.enums.Category;

/**
 * Defines for OCLv2.
 */
@Valid
@Data
public class Ocl {

    private static ObjectMapper theMapper = new ObjectMapper();

    @NotNull
    @Schema(description = "The catalog of the service")
    private Category category;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The version of the Ocl")
    private String version;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The name of the managed service")
    private String name;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The version of the managed service")
    private String serviceVersion;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The description of the managed service")
    private String description;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The namespace of the managed service")
    private String namespace;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "The icon of the managed service")
    private String icon;

    @Valid
    @NotNull
    @Schema(description = "The cloud service provider of the managed service")
    private CloudServiceProvider cloudServiceProvider;

    @Valid
    @NotNull
    @Schema(description = "The deployment of the managed service")
    private Deployment deployment;

    @Valid
    @NotNull
    @Schema(description = "The flavors of the managed service")
    private List<Flavor> flavors;

    @Valid
    @NotNull
    @Schema(description = "The billing policy of the managed service")
    private Billing billing;

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
}
