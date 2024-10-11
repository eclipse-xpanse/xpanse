/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.cache.consts;

/**
 * Define the constants for cache.
 */
public class CacheConstants {

    public static final String REGION_AZS_CACHE_NAME = "REGION_AZS_CACHE";

    public static final String SERVICE_FLAVOR_PRICE_CACHE_NAME = "SERVICE_FLAVOR_PRICE_CACHE";

    public static final String CREDENTIAL_CACHE_NAME = "CREDENTIAL_CACHE";

    public static final String MONITOR_METRICS_CACHE_NAME = "MONITOR_METRICS_CACHE";

    public static final String DEPLOYER_VERSIONS_CACHE_NAME = "DEPLOYER_VERSIONS_CACHE";

    public static final int DEFAULT_CACHE_EXPIRE_TIME_IN_MINUTES = 60;

    public static final int DEFAULT_CREDENTIAL_CACHE_EXPIRE_TIME_IN_SECONDS = 3600;

    public static final String CACHE_PROVIDER_CAFFEINE = "Caffeine";

    public static final String CACHE_PROVIDER_REDIS = "Redis";

    public static final String CACHE_PROVIDER_CAFFEINE_ENDPOINT = "local";

}
