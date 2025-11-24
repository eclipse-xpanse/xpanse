/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.plugins.huaweicloud.monitor.constant;

import org.eclipse.xpanse.plugins.huaweicloud.common.HuaweiCloudConstants;

/** Huawei Monitor Constant. */
public class HuaweiCloudMonitorConstants extends HuaweiCloudConstants {

    /** Dimension name for identifying the instance ID of an ECS. */
    public static final String VM_DIMENSION_NAME = "instance_id";

    /** Dim0 prefix to query ECS data for monitoring resources. */
    public static final String DIM0_PREFIX = "instance_id,";

    /** Aggregation time period of indicator monitoring data: real-time. */
    public static final int PERIOD_REAL_TIME_INT = 1;

    /** Aggregation time period of indicator monitoring data: five minutes. */
    public static final int PERIOD_FIVE_MINUTES_INT = 300;

    /** Aggregation time period of indicator monitoring data: twenty minutes. */
    public static final int PERIOD_TWENTY_MINUTES_INT = 1200;

    /** Aggregation time period of indicator monitoring data: one hour. */
    public static final int PERIOD_ONE_HOUR_INT = 3600;

    /** Aggregation time period of indicator monitoring data: four hours. */
    public static final int PERIOD_FOUR_HOURS_INT = 14400;

    /** Aggregation time period of indicator monitoring data: one day. */
    public static final int PERIOD_ONE_DAY_INT = 86400;

    public static final long FIVE_MINUTES_MILLISECONDS = 5 * 60 * 1000;

    public static final long FOUR_HOUR_MILLISECONDS = 4 * 3600 * 1000L;

    public static final long ONE_DAY_MILLISECONDS = 24 * 3600 * 1000L;

    public static final long THREE_DAY_MILLISECONDS = 3 * 24 * 3600 * 1000L;

    public static final long TEN_DAY_MILLISECONDS = 10 * 24 * 3600 * 1000L;

    public static final long ONE_MONTH_MILLISECONDS = 30 * 24 * 3600 * 1000L;
}
