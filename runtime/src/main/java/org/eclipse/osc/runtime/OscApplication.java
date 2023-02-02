/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.osc.runtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main entry class to OSC runtime. This class can be directly executed to start the server.
 */
@SpringBootApplication
@ComponentScan(basePackages = "org.eclipse.osc")
public class OscApplication {

    public static void main(String[] args) {
        SpringApplication.run(OscApplication.class, args);
    }

}