/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import React from 'react';
import { Descriptions, Divider, Space, Tag } from 'antd';
import { CloudUploadOutlined, InfoCircleOutlined } from '@ant-design/icons';
import { OclDetailVo } from '../../../../xpanse-api/generated';

function ServiceDetail({
    serviceDetails,
    serviceRegions,
}: {
    serviceDetails: OclDetailVo;
    serviceRegions: string[];
}): JSX.Element {
    return (
        <>
            <div className={'catalog-detail-class'}>
                <h3>
                    <CloudUploadOutlined />
                    &nbsp;Available Regions
                </h3>
                <Space size={[0, 8]} wrap>
                    {serviceRegions.map((serviceRegion, index) => (
                        <Tag key={index} color='default'>
                            {serviceRegion}
                        </Tag>
                    ))}
                </Space>
                <Divider />
            </div>
            <h3>
                <InfoCircleOutlined />
                &nbsp;Basic Information
            </h3>
            <Descriptions bordered column={1}>
                <Descriptions.Item label='Property' labelStyle={{ width: '230px' }}>
                    Information
                </Descriptions.Item>
                <Descriptions.Item label='Category'>{serviceDetails ? serviceDetails.category : ''}</Descriptions.Item>
                <Descriptions.Item label='Provider'>
                    {serviceDetails && serviceDetails.cloudServiceProvider
                        ? serviceDetails.cloudServiceProvider.name
                        : ''}
                </Descriptions.Item>
                <Descriptions.Item label='Service Version'>
                    {serviceDetails ? serviceDetails.serviceVersion : ''}
                </Descriptions.Item>
                <Descriptions.Item label='Billing Mode'>
                    {serviceDetails && serviceDetails.billing ? serviceDetails.billing.model : ''}
                </Descriptions.Item>
                <Descriptions.Item label='Register Time'>
                    {serviceDetails ? serviceDetails.createTime?.toUTCString() : ''}
                </Descriptions.Item>
                <Descriptions.Item label='Update Time'>
                    {serviceDetails ? serviceDetails.lastModifiedTime?.toUTCString() : ''}
                </Descriptions.Item>
                <Descriptions.Item label='Status'>
                    {serviceDetails ? serviceDetails.serviceState : ''}
                </Descriptions.Item>
                <Descriptions.Item label='Flavors'>
                    {serviceDetails && serviceDetails.flavors
                        ? serviceDetails.flavors
                              .map((flavor) => {
                                  return flavor.name;
                              })
                              .join(',')
                        : ''}
                </Descriptions.Item>
            </Descriptions>
        </>
    );
}

export default ServiceDetail;
