package org.eclipse.osc.orchestrator.plugin.huaweicloud.builders.packer;

import lombok.Data;

@Data
public class PackerVars {

    private String vpcId = "";
    private String subnetId = "";
    private String secGroupName = "";
}
