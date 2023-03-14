/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { catalogLabelName, catalogPageRoute, catalogSubPageRoute } from '../../../utils/constants';
import { BarsOutlined } from '@ant-design/icons';
import { ItemType } from 'antd/es/menu/hooks/useItems';

export const catalogMenu = (data: string[]): ItemType => {
    const subMenuItems = data.map((subMenu: string) => {
        let subMenuLabelStr: string = '';
        subMenuLabelStr = subMenu.charAt(0).toUpperCase() + subMenu.substring(1, subMenu.length).replace('_', '');
        return {
            key: catalogSubPageRoute + subMenu,
            label: subMenuLabelStr,
        };
    });

    return {
        key: catalogPageRoute,
        label: catalogLabelName,
        icon: <BarsOutlined />,
        children: subMenuItems,
    };
};
