/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime.modules.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.xpanse.common.security.config.SensitiveFieldEncoderConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {SensitiveFieldEncoderConfig.class})
@ExtendWith(SpringExtension.class)
class SensitiveFieldEncoderConfigTest {

    private static final String password = "123456@3452345#$%^";

    private static final String password_empty = "123456@3452345#$%^ ";

    @Autowired
    private SensitiveFieldEncoderConfig config;

    @Test
    void testPasswordEncoder() {
        String encode = config.passwordEncoder().encode(password);
        assertTrue(config.passwordEncoder().matches(password, encode));
    }

    @Test
    void test_PasswordEncoder() {
        String encode = config.passwordEncoder().encode(password);
        assertFalse(config.passwordEncoder().matches(password_empty, encode));
    }
}
