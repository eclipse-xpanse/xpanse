/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.monitor.constant;

/**
 * Huawei Monitor Constant.
 */
public class HuaweiCloudMonitorConstants {

    public static final String IAM = "AK_SK";
    public static final String HW_ACCESS_KEY = "HW_ACCESS_KEY";
    public static final String HW_SECRET_KEY = "HW_SECRET_KEY";


    /**
     * Aggregation method of indicator monitoring data: average.
     */
    public static final String FILTER_AVERAGE = "average";

    /*
     * Aggregation method of indicator monitoring data: max.
     */
    public static final String FILTER_MAX = "max";

    /**
     * Dimension name for identifying the instance ID of an ECS.
     */
    public static final String VM_DIMENSION_NAME = "instance_id";

    /**
     * Dim0 prefix to query ECS data for monitoring resources.
     */
    public static final String DIM0_PREFIX = "instance_id,";

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
