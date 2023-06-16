/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.plugins.openstack.monitor.gnocchi.api;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.openstack.internal.BaseOpenStackService;

/**
 * Base class for all Gnocchi APIs.
 */
public class BaseGnocchiServices extends BaseOpenStackService {

    protected BaseGnocchiServices() {
        super(ServiceType.TELEMETRY, EndpointFunction.instance);
    }

    /**
     * Sometimes the endpoint does not contain the API version which is required.  This insures that
     */
    private static class EndpointFunction implements Function<String, String> {

        static final EndpointFunction
                instance = new EndpointFunction();

        @Override
        public String apply(String input) {
            return input;
        }
    }

    protected <T> List<T> wrapList(T[] type) {
        if (type != null) {
            return Lists.newArrayList(type);
        }
        return Collections.emptyList();

    }
}
