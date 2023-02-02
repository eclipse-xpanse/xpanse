/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.modules.ocl.loader.data.models;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Base class which holds the security rule information.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Security extends RuntimeBase {

    private String name;
    private List<SecurityRule> rules;

}
