/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { Link } from 'react-router-dom';
import { MenuItemType } from 'antd/lib/menu/hooks/useItems';
import { AppstoreAddOutlined } from '@ant-design/icons';
import { registerPageRoute } from '../../utils/constants';

function registerPanelMenu(): MenuItemType {
    return {
        key: registerPageRoute,
        label: <Link to={registerPageRoute}>Register Panel</Link>,
        icon: <AppstoreAddOutlined />,
        title: 'RegisterPanel',
    };
}

export default registerPanelMenu;
