/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { Form, Switch } from 'antd';
import { OrderParam, SwitchOnChangeHandler } from './OrderCommon';

export function OrderSwitch({
    item,
    onChangeHandler,
}: {
    item: OrderParam;
    onChangeHandler: SwitchOnChangeHandler;
}): JSX.Element {
    return (
        <div className={'order-param-item-row'}>
            <div className={'order-param-item-left'} />
            <div className={'order-param-item-content'}>
                <Form.Item
                    name={item.name}
                    label={item.name + ':  (' + item.description + ')'}
                    rules={[{ type: 'boolean' }]}
                >
                    <Switch onChange={onChangeHandler} defaultChecked={false} />
                </Form.Item>
            </div>
            <div className={'order-param-item-right'} />
        </div>
    );
}