/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.common.systemcmd;

import org.eclipse.xpanse.common.proxy.ProxyConfigurationManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class ProxyConfigurationManagerTest {

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = {ProxyConfigurationManager.class, MockEnvironment.class})
    @TestPropertySource(
            properties = {
                "http_proxy=http://username:dummyPassword@test.domain.com:8080",
                "https_proxy=http://usernames:dummyPasswords@tests.domain.com:8083",
                "no_proxy=localhost"
            })
    class AllProxyProvidedTests {
        @Autowired ProxyConfigurationManager proxyConfigurationManager;

        @Test
        void allVariablesAreReadFromEnvironment() {
            Assertions.assertEquals(
                    "test.domain.com",
                    proxyConfigurationManager.getHttpProxyDetails().getProxyHost());
            Assertions.assertEquals(
                    "tests.domain.com",
                    proxyConfigurationManager.getHttpsProxyDetails().getProxyHost());
            Assertions.assertEquals(
                    8083, proxyConfigurationManager.getHttpsProxyDetails().getProxyPort());
            Assertions.assertEquals(
                    8080, proxyConfigurationManager.getHttpProxyDetails().getProxyPort());
            Assertions.assertEquals(
                    "username", proxyConfigurationManager.getHttpProxyDetails().getProxyUsername());
            Assertions.assertEquals(
                    "usernames",
                    proxyConfigurationManager.getHttpsProxyDetails().getProxyUsername());
            Assertions.assertEquals(
                    "dummyPassword",
                    proxyConfigurationManager.getHttpProxyDetails().getProxyPassword());
            Assertions.assertEquals(
                    "dummyPasswords",
                    proxyConfigurationManager.getHttpsProxyDetails().getProxyPassword());
            Assertions.assertEquals("localhost", proxyConfigurationManager.getNonProxyHosts());
            Assertions.assertEquals(
                    "http://username:dummyPassword@test.domain.com:8080",
                    proxyConfigurationManager.getHttpProxyDetails().getProxyUrl());
            Assertions.assertEquals(
                    "http://usernames:dummyPasswords@tests.domain.com:8083",
                    proxyConfigurationManager.getHttpsProxyDetails().getProxyUrl());
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = {ProxyConfigurationManager.class, MockEnvironment.class})
    @TestPropertySource(
            properties = {
                "http_proxy=http://username:dummyPassword@test.domain.com",
                "no_proxy=localhost",
                "https_proxy="
            })
    class SomeProxyProvidedTests {
        @Autowired ProxyConfigurationManager proxyConfigurationManager;

        @Test
        void allVariablesAreReadFromEnvironment() {
            Assertions.assertEquals(
                    "test.domain.com",
                    proxyConfigurationManager.getHttpProxyDetails().getProxyHost());
            Assertions.assertEquals(
                    80, proxyConfigurationManager.getHttpProxyDetails().getProxyPort());
            Assertions.assertEquals(
                    "username", proxyConfigurationManager.getHttpProxyDetails().getProxyUsername());
            Assertions.assertEquals(
                    "dummyPassword",
                    proxyConfigurationManager.getHttpProxyDetails().getProxyPassword());
            Assertions.assertEquals("localhost", proxyConfigurationManager.getNonProxyHosts());
            Assertions.assertEquals(
                    "http://username:dummyPassword@test.domain.com",
                    proxyConfigurationManager.getHttpProxyDetails().getProxyUrl());
            Assertions.assertNull(proxyConfigurationManager.getHttpsProxyDetails());
        }
    }
}
