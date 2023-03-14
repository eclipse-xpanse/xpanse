/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { MenuProps } from 'antd';
import Logout from '../../content/login/Logout';

const items: MenuProps['items'] = [{ key: 'signout', label: <Logout /> }];

export default items;
