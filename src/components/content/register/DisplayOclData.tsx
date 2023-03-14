/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { Ocl } from '../../../xpanse-api/generated';
import { Button, Descriptions, Image, Popover } from 'antd';
import YAML from 'yaml';

function DisplayOclData({ ocl }: { ocl: Ocl }): JSX.Element | string {
    const PLACE_HOLDER_UNKNOWN_VALUE: string = 'NOT PROVIDED';
    const getFlavoursText = (ocl: Ocl): JSX.Element => {
        if (ocl.flavors) {
            const yamlDocument = new YAML.Document();
            // @ts-ignore
            yamlDocument.contents = ocl.flavors;
            return (
                <Popover content={<pre>{yamlDocument.toString()}</pre>} title={'Flavors'} trigger='hover'>
                    <Button
                        className={'ocl-data-hover'}
                        type={'link'}
                    >{`Available in ${ocl.flavors.length} flavor(s)`}</Button>
                </Popover>
            );
        }
        return <></>;
    };

    const getBillingText = (ocl: Ocl): JSX.Element => {
        if (ocl.billing) {
            const yamlDocument = new YAML.Document();
            // @ts-ignore
            yamlDocument.contents = ocl.billing;
            return (
                <Popover content={<pre>{yamlDocument.toString()}</pre>} title={'Billing'} trigger='hover'>
                    <Button className={'ocl-data-hover'} type={'link'}>{`${ocl.billing.model}`}</Button>
                </Popover>
            );
        }
        return <></>;
    };

    const getDeploymentText = (ocl: Ocl): JSX.Element => {
        if (ocl.deployment) {
            const yamlDocument = new YAML.Document();
            // @ts-ignore
            yamlDocument.contents = ocl.deployment;
            return (
                <Popover content={<pre>{yamlDocument.toString()}</pre>} title={'Deployment'} trigger='hover'>
                    <Button className={'ocl-data-hover'} type={'link'}>
                        {ocl.deployment.kind}
                    </Button>
                </Popover>
            );
        }
        return <></>;
    };

    try {
        return (
            <>
                <div className={'ocl-data-display'}>
                    <div className={'ocl-data-main-info'}>
                        <div>
                            {
                                <Image
                                    alt={'Service Icon'}
                                    width={100}
                                    height={100}
                                    src={ocl.icon}
                                    fallback='data:image/png;base64,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'
                                />
                            }
                        </div>
                        <div>
                            <div>
                                <br />
                                <b>Service Name</b>
                                <br />
                                {ocl.name}
                                <br />
                                <br />
                            </div>
                            <div>
                                <b>Cloud Service Provider</b>
                                <br />
                                {ocl.cloudServiceProvider.name}
                                <br />
                                <br />
                            </div>
                            <div>
                                <b>Available Regions</b>
                                <br />
                                {ocl.cloudServiceProvider && ocl.cloudServiceProvider.regions
                                    ? ocl.cloudServiceProvider.regions.join(', ')
                                    : PLACE_HOLDER_UNKNOWN_VALUE}
                                <br />
                                <br />
                            </div>
                        </div>
                    </div>
                    <div>
                        <Descriptions title={'Basic Information'} column={2} bordered className={'ocl-data-info-table'}>
                            <Descriptions.Item label='Category'>
                                {ocl.category ? ocl.category : PLACE_HOLDER_UNKNOWN_VALUE}
                            </Descriptions.Item>
                            <Descriptions.Item label='Version'>{ocl.serviceVersion}</Descriptions.Item>
                            <Descriptions.Item label='Namespace'>{ocl.namespace}</Descriptions.Item>
                            <Descriptions.Item label='Flavors'>{<pre>{getFlavoursText(ocl)}</pre>}</Descriptions.Item>
                            <Descriptions.Item label='Billing'>{<pre>{getBillingText(ocl)}</pre>}</Descriptions.Item>
                            <Descriptions.Item label='Deployment'>{getDeploymentText(ocl)}</Descriptions.Item>
                            <Descriptions.Item label='Description'>{ocl.description}</Descriptions.Item>
                        </Descriptions>
                    </div>
                </div>
            </>
        );
    } catch (error: any) {
        return error.message;
    }
}

export default DisplayOclData;
