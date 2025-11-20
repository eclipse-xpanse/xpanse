/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.deployment.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** properties class. */
@Data
@ConfigurationProperties(prefix = "xpanse.deployer")
public class DeploymentProperties {

    private Integer lockFilePollingInSeconds;
    private Boolean supportOnlyDefaultVersionsEnabled;
    private Boolean cleanWorkspaceAfterDeploymentEnabled;
    private TerraformLocal terraformLocal;
    private OpentofuLocal opentofuLocal;
    private Helm helm;
    private TerraBoot terraBoot;
    private TofuMaker tofuMaker;

    /** Properties class. */
    @Data
    public static class TerraformLocal {
        private String installDir;
        private GitHub github;
        private String downloadBaseUrl;
        private List<String> defaultSupportedVersions;
        private Workspace workspace;
        private Debug debug;
    }

    /** Properties class. */
    @Data
    public static class OpentofuLocal {
        private String installDir;
        private List<String> defaultSupportedVersions;
        private GitHub github;
        private String downloadBaseUrl;
        private Workspace workspace;
        private Debug debug;
    }

    /** Properties class. */
    @Data
    public static class Helm {
        private String installDir;
        private GitHub github;
        private String downloadBaseUrl;
        private List<String> defaultSupportedVersions;
    }

    /** Properties class. */
    @Data
    public static class TerraBoot {
        private String webhookEndpoint;
        private String webhookCallbackUri;
        private String endpoint;
    }

    /** Properties class. */
    @Data
    public static class TofuMaker {
        private String webhookEndpoint;
        private String webhookCallbackUri;
        private String endpoint;
    }

    /** Properties class. */
    @Data
    public static class GitHub {
        private String apiEndpoint;
        private String repository;
    }

    /** Properties class. */
    @Data
    public static class Workspace {
        private String directory;
    }

    /** Properties class. */
    @Data
    public static class Debug {
        private Boolean enabled;
        private String levelValue;
    }
}
