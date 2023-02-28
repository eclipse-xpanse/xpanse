/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { AdminApi, createConfiguration, ServerConfiguration, ServiceApi, ServiceVendorApi } from './generated';
import { ConfigurationParameters } from './generated/configuration';

const customConfiguration: ConfigurationParameters = {};
customConfiguration.baseServer = new ServerConfiguration<{}>(process.env.REACT_APP_XPANSE_API_URL as string, {});

const configuration = createConfiguration(customConfiguration);

export const adminApi = new AdminApi(configuration);
export const serviceVendorApi = new ServiceVendorApi(configuration);
export const serviceApi = new ServiceApi(configuration);
