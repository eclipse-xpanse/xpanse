/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.orchestrator.plugin.flexibleengine.monitor.constant;

/**
 * Huawei Monitor Constant.
 */
public class FlexibleEngineMonitorConstants {

    public static final String PROTOCOL_HTTPS = "https://";
    public static final String CES_ENDPOINT_PREFIX = "ces.";
    public static final String IAM_ENDPOINT_PREFIX = "iam.";
    public static final String ENDPOINT_SUFFIX = ".prod-cloud-ocb.orange-business.com";
    public static final String OS_ACCESS_KEY = "OS_ACCESS_KEY";
    public static final String OS_SECRET_KEY = "OS_SECRET_KEY";
    public static final String IAM_API_VERSION = "v3";
    public static final String CES_API_VERSION = "V1.0";
    public static final String DIM0_PREFIX = "instance_id,";
    public static final String METRIC_PATH = "metric-data";
    public static final String PROJECTS_PATH = "projects";
    public static final String LIST_METRICS_PATH = "metrics";

    /**
     * Aggregation method of indicator monitoring data: average.
     */
    public static final String FILTER_AVERAGE = "average";

    /*
     * Aggregation method of indicator monitoring data: max.
     */
    public static final String FILTER_MAX = "max";

    /**
     * Aggregation time period of indicator monitoring data: real-time.
     */
    public static final int PERIOD_REAL_TIME_INT = 1;

    /**
     * Aggregation time period of indicator monitoring data: five minutes.
     */
    public static final int PERIOD_FIVE_MINUTES_INT = 300;

    /**
     * Aggregation time period of indicator monitoring data: twenty minutes.
     */
    public static final int PERIOD_TWENTY_MINUTES_INT = 1200;

    /**
     * Aggregation time period of indicator monitoring data: one hour.
     */
    public static final int PERIOD_ONE_HOUR_INT = 3600;

    /**
     * Aggregation time period of indicator monitoring data: four hours.
     */
    public static final int PERIOD_FOUR_HOURS_INT = 14400;

    /**
     * Aggregation time period of indicator monitoring data: one day.
     */
    public static final int PERIOD_ONE_DAY_INT = 86400;


    public static final long FIVE_MINUTES_MILLISECONDS = 5 * 60 * 1000;

}
