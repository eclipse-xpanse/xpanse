/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { UserOutlined } from '@ant-design/icons';
import { Dropdown, Space } from 'antd';
import { Header } from 'antd/es/layout/layout';
import { usernameKey } from '../../utils/constants';
import SystemStatusBar from '../../content/systemStatus/SystemStatusBar';
import items from './headerUserMenuItems';

function LayoutHeader(): JSX.Element {
    return (
        <Header style={{ width: '100%', background: '#ffffff' }}>
            <div className={'header-menu'}>
                <SystemStatusBar />
                <Dropdown menu={{ items }} placement='topRight'>
                    <Space>
                        <UserOutlined />
                        {localStorage.getItem(usernameKey)}
                    </Space>
                </Dropdown>
            </div>
        </Header>
    );
}

export default LayoutHeader;
