/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { RocketOutlined, SettingOutlined } from '@ant-design/icons';
import { Card, Divider } from 'antd';
import Navigate from '../order/Navigate';

function WelcomeCard(): JSX.Element {
    return (
        <>
            <Card title='Welcome to Xpanse' bordered={true}>
                <RocketOutlined /> <a href='https://huaweicloud.github.io/xpanse-website/'>Getting started with OSC</a>
                <div>Learn the fundamentals about Open Services Cloud and cloud providers supporting it.</div>
                <Divider />
                <SettingOutlined />
                <a href='https://huaweicloud.github.io/xpanse-website/docs/ocl'>OSC Configuration Language</a>
                <div>Use the OSC Configuration Language to describe portable managed services.</div>
            </Card>
        </>
    );
}

export default WelcomeCard;
