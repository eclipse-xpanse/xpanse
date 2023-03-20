/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { BarsOutlined } from '@ant-design/icons';
import { ItemType } from 'antd/es/menu/hooks/useItems';

export const OrderMenu = (): ItemType => {
    return {
        key: '/order',
        label: 'Order',
        icon: <BarsOutlined />
    };
};
