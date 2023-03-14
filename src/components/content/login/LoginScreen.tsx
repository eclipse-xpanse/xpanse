/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { Form, Image, Input, message, Modal, Space } from 'antd';
import { useLocation, useNavigate } from 'react-router-dom';
import { homePageRoute, isAuthenticatedKey, usernameKey } from '../../utils/constants';
import { useForm } from 'antd/es/form/Form';

function LoginScreen(): JSX.Element {
    const validUsers: Array<string> = ['csp', 'user', 'otc'];
    const [loginForm] = useForm();
    const location = useLocation();
    const origin = location.state?.from?.pathname || homePageRoute;
    const navigate = useNavigate();

    function validateUser(enteredUser: string): void {
        if (enteredUser && !validUsers.includes(enteredUser as string)) {
            message.error('Please use valid user from: ' + validUsers.join(','));
        } else {
            localStorage.setItem(isAuthenticatedKey, 'true');
            localStorage.setItem(usernameKey, enteredUser);
            navigate(origin);
        }
    }

    return (
        <>
            <Modal
                centered={true}
                mask={false}
                title={
                    <Space>
                        <Image width={50} src='./logo.png' preview={false} />
                        Welcome to Xpanse
                    </Space>
                }
                open={true}
                okText='Log In'
                cancelText='Reset'
                onOk={loginForm.submit}
                onCancel={() => loginForm.resetFields()}
            >
                <Form
                    name='login'
                    form={loginForm}
                    labelCol={{ span: 8 }}
                    wrapperCol={{ span: 16 }}
                    autoComplete='off'
                    onFinish={(values) => validateUser(values.username)}
                    preserve={false}
                >
                    <Form.Item
                        name='username'
                        label='Username'
                        rules={[{ required: true, message: 'Please input a persona username!' }]}
                    >
                        <Input placeholder='username' prefix={<UserOutlined className={'site-form-item-icon'} />} />
                    </Form.Item>
                    <Form.Item name='password' label='Password' rules={[{ required: true }]}>
                        <Input
                            type='password'
                            placeholder='password'
                            prefix={<LockOutlined className={'site-form-item-icon'} />}
                        />
                    </Form.Item>
                </Form>
                <Space>
                    <i>
                        You have three username depending on the persona you want to use:
                        <ul>
                            <li>
                                <b>csp</b> for cloud service provider admin user
                            </li>
                            <li>
                                <b>user</b> for Xpanse end user
                            </li>
                            <li>
                                <b>otc</b> for OTC end user
                            </li>
                        </ul>
                    </i>
                </Space>
            </Modal>
        </>
    );
}

export default LoginScreen;
