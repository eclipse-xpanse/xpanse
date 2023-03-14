/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { Image, Menu, Layout } from 'antd';
import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { homePageRoute, usernameKey } from '../../utils/constants';
import registerPanelMenu from '../../content/register/registerPanelMenu';
import { ItemType } from 'antd/es/menu/hooks/useItems';
import { serviceVendorApi } from '../../../xpanse-api/xpanseRestApiClient';
import { catalogMenu } from '../../content/catalog/services/catalogMenu';

function LayoutSider(): JSX.Element {
    const [collapsed, setCollapsed] = useState(false);
    const [items, setItems] = useState<ItemType[]>([]);
    const navigate = useNavigate();

    const onClicked = function (cfg: any) {
        navigate(cfg.key);
    };

    useEffect(() => {
        serviceVendorApi.listCategories().then((rsp) => {
            if (localStorage.getItem(usernameKey) === 'csp') {
                const data = rsp;
                setItems([catalogMenu(data), registerPanelMenu()]);
            }
        });
    }, []);

    return (
        <Layout.Sider collapsible collapsed={collapsed} onCollapse={(newValue) => setCollapsed(newValue)}>
            <div className={'logo'}>
                <Link to={homePageRoute}>
                    <Image width={50} src='logo.png' preview={false} />
                </Link>
            </div>
            <Menu items={items} mode='inline' theme='dark' onClick={onClicked} />
        </Layout.Sider>
    );
}

export default LayoutSider;
