/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.modules.security.config;

import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.annotation.Validated;

/** properties class. */
@RefreshScope
@ConfigurationProperties(prefix = "xpanse.security")
@Validated
@Data
public class SecurityProperties {

    private boolean enableWebSecurity;
    private boolean enableRoleProtection;
    private String webhookHmacSigningKey;

    @Valid private SecretsEncryption secretsEncryption;

    @Valid private OAuth oauth;

    /** properties class. */
    @Data
    // CHECKSTYLE OFF: AbbreviationAsWordInName
    public static class OAuth {
        private String authProviderEndpoint;
        private String tokenType;
        private String authUrl;
        private String tokenUrl;

        @Valid private Server server;

        @Valid private Client client;

        @Valid private SwaggerUi swaggerUi;

        @Valid private Scopes scopes;

        @Valid private Claims claims;

        @Valid private MetaData metaData;

        private String defaultRole;
    }

    /** properties class. */
    @Data
    public static class Server {
        private String apiClientId;
        private String apiClientSecret;
    }

    /** properties class. */
    @Data
    public static class Client {
        private String clientId;
        private String clientSecret;
    }

    /** properties class. */
    @Data
    public static class SwaggerUi {
        private String clientId;
    }

    /** properties class. */
    @Data
    public static class Scopes {
        private String openid;
        private String profile;
        private String metadata;
        private String roles;
        private String requiredScopes;
    }

    /** properties class. */
    @Data
    public static class Claims {
        private String usernameKey;
        private String metaDataKey;
        private String grantedRolesKey;
        private String userIdKey;
    }

    /** properties class. */
    @Data
    public static class MetaData {
        private String isvKey;
        private String cspKey;
    }

    /** properties class. */
    @Data
    public static class SecretsEncryption {
        private String initialVector;
        private String algorithmName;
        private String algorithmMode;
        private String algorithmPadding;
        private String secretKeyValue;
        private String secretKeyFile;
    }
}
