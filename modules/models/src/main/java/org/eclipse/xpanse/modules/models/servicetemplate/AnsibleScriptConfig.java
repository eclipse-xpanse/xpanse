/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.models.servicetemplate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/** Ansible script configuration details. */
@Valid
@Data
@Slf4j
public class AnsibleScriptConfig implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(
            description =
                    "name of the ansible playbook. Should be the fully qualified file name"
                            + " (including the directory path) in the repo.",
            example = "playbook-name.yml")
    private String playbookName;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "Path where the virtualenv must be created.", example = "/tmp/virtualEnv")
    private String virtualEnv;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(
            description =
                    "Version of the python. This version of python "
                            + "must be available in node which is acting as the configManager.",
            example = "3.10")
    private String pythonVersion;

    @NotNull
    @Schema(
            description =
                    "The agent will prepare the virtual environment if this true."
                            + "Otherwise it is assumed that the environment is already prepared "
                            + "as part of the resource creation or the VM base image build.")
    private Boolean isPrepareAnsibleEnvironment;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "Url of the script repo.")
    private String repoUrl;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "Branch of the git repo.")
    private String branch;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(
            description =
                    "the python module requirements file in GIT repo. Should be the fully qualified"
                            + " file name (including the directory path). ",
            example = "modules/requirements.txt")
    private String requirementsFile;

    @NotNull
    @NotBlank
    @NotEmpty
    @Schema(description = "Full path of the roles and collections requirements file in GIT repo.")
    private String galaxyFile;

    @Schema(
            description =
                    "Defines if the complete service inventory "
                            + "is necessary for running the ansible job.")
    private Boolean ansibleInventoryRequired;
}
