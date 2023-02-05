/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.runtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main entry class to xpanse runtime. This class can be directly executed to start the server.
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.eclipse.xpanse")
public class XpanseApplication {

    public static void main(String[] args) {
        SpringApplication.run(XpanseApplication.class, args);
    }

}