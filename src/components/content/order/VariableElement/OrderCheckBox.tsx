/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { Checkbox, Form } from 'antd';
import { CheckBoxOnChangeHandler, OrderParam } from './OrderCommon';

export function OrderCheckBox({
    item,
    onChangeHandler,
}: {
    item: OrderParam;
    onChangeHandler: CheckBoxOnChangeHandler;
}): JSX.Element {
    return (
        <div className={'order-param-item-row'}>
            <div className={'order-param-item-left'} />
            <div className={'order-param-item-content'}>
                <Form.Item name={item.name} label={item.name + ' :  ' + item.description} rules={[{ type: 'boolean' }]}>
                    <Checkbox onChange={onChangeHandler}>
                        <span className={'order-param-item-tip'}> {item.name} </span>
                    </Checkbox>
                </Form.Item>
            </div>
            <div className={'order-param-item-right'} />
        </div>
    );
}