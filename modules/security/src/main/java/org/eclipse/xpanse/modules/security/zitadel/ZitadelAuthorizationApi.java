/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.modules.security.zitadel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Service for authorization of IAM 'Zitadel'.
 */
@Slf4j
@Profile("zitadel")
@Service
public class ZitadelAuthorizationApi {


}
