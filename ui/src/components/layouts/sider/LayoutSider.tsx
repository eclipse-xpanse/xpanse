/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { Image, Menu } from 'antd';
import Sider from 'antd/es/layout/Sider';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { homePageRoute, usernameKey } from '../../utils/constants';
import registerPanelMenu from '../../content/register/registerPanelMenu';
import { MenuItemType } from 'antd/lib/menu/hooks/useItems';

function LayoutSider(): JSX.Element {
    const [collapsed, setCollapsed] = useState(false);
    const menuItems: MenuItemType[] = [];

    if (localStorage.getItem(usernameKey) === 'csp') {
        menuItems.push(registerPanelMenu());
    }

    return (
        <Sider collapsible collapsed={collapsed} onCollapse={(newValue) => setCollapsed(newValue)}>
            <div className='logo'>
                <Link to={homePageRoute}>
                    <Image width={50} src='./logo.png' preview={false} />
                </Link>
            </div>
            <Menu items={menuItems} mode='inline' theme='dark' />
        </Sider>
    );
}

export default LayoutSider;
