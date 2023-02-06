/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.ocl.loader.data.models;

import java.util.List;
import lombok.Data;
import org.eclipse.xpanse.modules.ocl.loader.data.models.enums.UserDataType;

/**
 * user data for Vm.
 */
@Data
public class UserData {

    private UserDataType type;
    private List<String> commands;

}
