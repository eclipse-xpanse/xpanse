/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import Navigate from './Navigate';
import '../../../styles/order.css';
import { To } from 'react-router-dom';
import { HomeOutlined } from '@ant-design/icons';
import { ChangeEvent, useState } from 'react';
import { OrderParam, OrderParamItemProps, ParamOnChangeHandler } from './VariableElement/OrderCommon';
import { OrderTextInput } from './VariableElement/OrderTextInput';
import { OrderNumberInput } from './VariableElement/OrderNumberInput';
import { OrderSwitch } from './VariableElement/OrderSwitch';
import { Alert, Button, Form } from 'antd';
import { CreateRequest, CreateRequestCategoryEnum, CreateRequestCspEnum } from '../../../xpanse-api/generated';
import { serviceApi } from '../../../xpanse-api/xpanseRestApiClient';

// 1 hour.
const deployTimeout: number = 3600000;
// 5 seconds.
const waitServicePeriod: number = 5000;

function OrderItem(props: OrderParamItemProps) {
    if (props.item.type === 'string') {
        return <OrderTextInput item={props.item} onChangeHandler={props.onChangeHandler} />;
    }
    if (props.item.type === 'number') {
        return <OrderNumberInput item={props.item} onChangeHandler={props.onChangeHandler} />;
    }
    if (props.item.type === 'boolean') {
        return <OrderSwitch item={props.item} onChangeHandler={props.onChangeHandler} />;
    }

    return <></>;
}

export interface OrderExtendProps {
    // The category of the service
    category: CreateRequestCategoryEnum;
    // The name of the service
    name: string;
    // The version of service
    version: string;
    // The region of the provider.
    region: string;
    // The csp of the Service.
    csp: CreateRequestCspEnum;
    // The flavor of the Service.
    flavor: string;
    // The deployment context
    params: OrderParam[];
}

function OrderSubmit(props: OrderExtendProps): JSX.Element {
    const [tip, setTip] = useState<JSX.Element | undefined>(undefined);
    const [parameters, setParameters] = useState<OrderParam[]>(props.params);
    const [deploying, setDeploying] = useState<boolean>(false);

    function Tip(type: 'error' | 'success', msg: string) {
        setTip(<Alert message={msg} description='' type={type} />);
    }

    function TipClear() {
        setTip(undefined);
    }

    function GetOnChangeHandler(parameter: OrderParam): ParamOnChangeHandler {
        console.log(parameters);
        if (parameter.type === 'string') {
            return (event: ChangeEvent<HTMLInputElement>) => {
                TipClear();
                setParameters(
                    parameters.map((item) => {
                        if (item.name === parameter.name) {
                            return { ...item, value: event.target.value };
                        }
                        return item;
                    })
                );
            };
        }
        if (parameter.type === 'number') {
            return (value: string | number | null) => {
                TipClear();
                setParameters(
                    parameters.map((item) => {
                        if (item.name === parameter.name) {
                            return { ...item, value: value as string };
                        }
                        return item;
                    })
                );
            };
        }
        if (parameter.type === 'boolean') {
            return (checked: boolean) => {
                TipClear();
                setParameters(
                    parameters.map((item) => {
                        if (item.name === parameter.name) {
                            return { ...item, value: checked };
                        }
                        return item;
                    })
                );
            };
        }
        return (value: any) => {
            console.log(value);
        };
    }

    function waitingServiceReady(uuid: string, timeout: number, date: Date) {
        Tip(
            'success',
            (('Deploying, Please wait... [' + Math.ceil((new Date().getTime() - date.getTime()) / 1000)) as string) +
                's]'
        );
        serviceApi
            .serviceDetail(uuid)
            .then((response) => {
                // success, exit from deploying.
                setDeploying(false);
                console.log('waitingServiceReady success', response);
            })
            .catch((error) => {
                console.log('waitingServiceReady error', error);
                if (timeout > 0) {
                    setTimeout(() => {
                        waitingServiceReady(uuid, timeout - waitServicePeriod, date);
                    }, waitServicePeriod);
                } else {
                    setDeploying(false);
                    TipClear();
                }
            })
            .finally(() => {});
    }

    function OnSubmit() {
        let createRequest = new CreateRequest();
        createRequest.name = props.name;
        createRequest.version = props.version;
        createRequest.category = props.category;
        createRequest.csp = props.csp;
        createRequest.region = props.region;
        createRequest.flavor = props.flavor;
        createRequest.property = {};
        for (let item of parameters) {
            createRequest.property[item.name] = item.value as string;
        }
        // Start deploying
        setDeploying(true);

        serviceApi
            .start(createRequest)
            .then((response) => {
                console.log('success ', response);
                Tip('success', response);
                waitingServiceReady(response, deployTimeout, new Date());
                // setUuid(response);
            })
            .catch((error) => {
                console.error(error);
                Tip('error', 'Create service deploy failed.');
                setDeploying(false);
                // setUuid(undefined);
            })
            .finally(() => {});
    }

    return (
        <>
            <div>
                <Navigate text={'<< Back'} to={-1 as To} />
                <div className={'Line'} />
                <HomeOutlined />
                <div className={'order-service-title'}>
                    Service:{' '}
                    <span className={'order-service-title-version'}>
                        {props.name}@{props.version}
                    </span>
                </div>
            </div>
            <div>{tip}</div>
            <Form
                layout='vertical'
                autoComplete='off'
                onFinish={OnSubmit}
                validateTrigger={['onSubmit', 'onBlur', 'onChange']}
            >
                <div className={deploying ? 'deploying order-param-item-row' : ''}>
                    {parameters.map((item) => (
                        <OrderItem key={item.name} item={item} onChangeHandler={GetOnChangeHandler(item)} />
                    ))}
                </div>
                <div className={'Line'} />
                <div className={'order-param-item-row'}>
                    <div className={'order-param-item-left'} />
                    <div className={'order-param-submit'}>
                        <Button type='primary' loading={deploying} htmlType='submit'>
                            Deploy
                        </Button>
                    </div>
                </div>
            </Form>
        </>
    );
}

export function DefaultOrderExtendParamsBak(): JSX.Element {
    return OrderSubmit({
        // The category of the service
        category: 'middleware',
        // The name of the service
        name: 'kubernetes',
        // The version of service
        version: 'v1.2',
        // The region of the provider.
        region: 'cn-north-4',
        // The csp of the Service.
        csp: 'huawei',
        // The flavor of the Service.
        flavor: '3-node-without-zookeeper',
        params: [
            {
                name: 'secgroupe_id',
                kind: 'variable',
                type: 'string',
                example: '43190ab2-c3d9-11ed-9f75-4b91f01c7f34',
                description: 'The uuid of the security group',
                value: '',
                mandatory: true,
                validator: 'string',
            },
            {
                name: 'gateway_id',
                kind: 'variable',
                type: 'number',
                example: '6766367e-c3d9-11ed-a09c-fbf1a38147fb',
                description: 'The uuid of the VPC',
                value: '',
                mandatory: false,
                validator: 'string',
            },
            {
                name: 'vpd_id',
                kind: 'variable',
                type: 'string',
                example: '6766367e-c3d9-11ed-a09c-fbf1a38147fb',
                description: 'The uuid of the VPC',
                value: '',
                mandatory: false,
                validator: 'string',
            },
            {
                name: 'gateway_id2',
                kind: 'variable',
                type: 'number',
                example: '6766367e-c3d9-11ed-a09c-fbf1a38147fb',
                description: 'The uuid of the VPC',
                value: '',
                mandatory: true,
                validator: 'string',
            },
            {
                name: 'gateway_boolean',
                kind: 'variable',
                type: 'boolean',
                example: '6766367e-c3d9-11ed-a09c-fbf1a38147fb',
                description: 'The uuid of the VPC',
                value: '',
                mandatory: true,
                validator: 'string',
            },
        ],
    });
}

export function DefaultOrderExtendParams(): JSX.Element {
    return OrderSubmit({
        // The category of the service
        category: 'middleware',
        // The name of the service
        name: 'kubernetes',
        // The version of service
        version: 'v1.2',
        // The region of the provider.
        region: 'cn-north-4',
        // The csp of the Service.
        csp: 'huawei',
        // The flavor of the Service.
        flavor: '3-node-without-zookeeper',
        params: [
            {
                name: 'secgroup_id',
                kind: 'variable',
                type: 'string',
                example: '43190ab2-c3d9-11ed-9f75-4b91f01c7f34',
                description: 'The uuid of the security group',
                value: '',
                mandatory: true,
                validator: 'string',
            },
        ],
    });
}

export default OrderSubmit;