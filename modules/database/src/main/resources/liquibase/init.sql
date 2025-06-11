/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */
-- only a test database configuration.
CREATE DATABASE IF NOT EXISTS activiti;
CREATE USER 'activiti'@'%' IDENTIFIED BY 'Activiti@2023';
GRANT ALL PRIVILEGES ON activiti.* TO 'activiti'@'%';
FLUSH PRIVILEGES;