/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { BarsOutlined } from '@ant-design/icons';
import { ItemType } from 'antd/es/menu/hooks/useItems';
import { servicesLabelName, servicesPageRoute, servicesSubPageRoute } from '../../utils/constants';

export const servicesMenu = (data: string[]): ItemType => {
    const subMenuItems = data.map((subMenu: string) => {
        let subMenuLabelStr: string = '';
        subMenuLabelStr = subMenu.charAt(0).toUpperCase() + subMenu.substring(1, subMenu.length).replace('_', '');
        return {
            key: servicesSubPageRoute + subMenu,
            label: subMenuLabelStr,
        };
    });

    return {
        key: servicesPageRoute,
        label: servicesLabelName,
        icon: <BarsOutlined />,
        children: subMenuItems,
    };
};
