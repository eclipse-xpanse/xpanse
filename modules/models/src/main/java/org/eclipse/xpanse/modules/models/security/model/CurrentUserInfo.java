/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.models.security.model;

import java.util.List;
import lombok.Data;

/**
 * Model of current user info.
 */
@Data
public class CurrentUserInfo {

    private String userId;

    private String userName;

    private List<String> roles;

}
