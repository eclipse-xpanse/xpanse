/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.packer;

import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.osc.modules.ocl.loader.data.models.Artifact;
import org.eclipse.osc.modules.ocl.loader.data.models.BaseImage;
import org.eclipse.osc.modules.ocl.loader.data.models.Ocl;
import org.eclipse.osc.modules.ocl.loader.data.models.Provisioner;

@Slf4j
class Ocl2Packer {

    private final Ocl ocl;
    private final Artifact artifact;

    Ocl2Packer(Ocl ocl, Artifact artifact) {
        this.ocl = ocl;
        this.artifact = artifact;
    }

    public String getInstallScript() {
        StringBuilder installScript = new StringBuilder();
        // Only support shell type currently.
        installScript.append("#! /bin/bash");

        for (var artifactProvisioner : artifact.getProvisioners()) {
            Optional<Provisioner> provisioner = ocl.referTo(artifactProvisioner, Provisioner.class);

            if (provisioner.isPresent()) {
                // TODO: support specifying install script path in ocl besides inline script.
                if (provisioner.get().getInline() == null) {
                    continue;
                }

                String type = provisioner.get().getType();
                if (!Objects.equals(type, "shell")) {
                    throw new IllegalArgumentException(
                            "Ocl for image provisioner type is invalid.");
                }

                for (String inline : provisioner.get().getInline()) {
                    installScript.append(String.format("%n%s", inline));
                }
            }
        }

        return installScript.toString();
    }

    public String getHclImages(PackerVars packerVars) {
        StringBuilder hcl = new StringBuilder();
        Optional<BaseImage> baseImage = ocl.referTo(artifact.getBase(), BaseImage.class);

        hcl.append(String.format("%nvariable \"region_name\" {"
                        + "%n  type = string"
                        + "%n  default = env(\"HW_REGION_NAME\")"
                        + "%n}"
                        + "%nvariable \"secret_key\" {"
                        + "%n  type = string"
                        + "%n  default = env(\"HW_SECRET_KEY\")"
                        + "%n}"
                        + "%nvariable \"access_key\" {"
                        + "%n  type = string"
                        + "%n  default = env(\"HW_ACCESS_KEY\")"
                        + "%n}"
                        + "%nsource \"huaweicloud-ecs\" \"%s\" {"
                        + "%n  region             = var.region_name"
                        + "%n  image_name         = \"%s\""
                        + "%n  access_key         = var.access_key"
                        + "%n  secret_key         = var.secret_key"
                        + "%n  eip_bandwidth_size = \"5\""
                        + "%n  eip_type           = \"5_bgp\""
                        + "%n  flavor             = \"s6.large.2\""
                        + "%n  instance_name      = \"%s\""
                        + "%n  vpc_id             = \"%s\""
                        + "%n  subnets            = [\"%s\"]"
                        + "%n  security_groups    = [\"%s\"]"
                        + "%n  source_image_filter {"
                        + "%n    filters {"
                        + "%n      name = \"%s\""
                        + "%n      visibility = \"public\""
                        + "%n    }"
                        + "%n    most_recent = true"
                        + "%n  }"
                        + "%n  ssh_ip_version     = \"4\""
                        + "%n  ssh_username       = \"root\""
                        + "%n}%n",
                artifact.getName(), artifact.getName(), artifact.getName(),
                packerVars.getVpcId(), packerVars.getSubnetId(), packerVars.getSecGroupId(),
                baseImage.get().getName()));

        hcl.append(String.format("%nbuild {"
                        + "%n  sources = [\"source.huaweicloud-ecs.%s\"]"
                        + "%n  provisioner \"shell\" {"
                        + "%n    pause_before = \"30s\""
                        + "%n    script       = \"install_script.sh\""
                        + "%n  }"
                        + "%n}%n",
                artifact.getName()));

        return hcl.toString();
    }
}
