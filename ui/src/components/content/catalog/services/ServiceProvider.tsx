/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import React, { useEffect, useState } from 'react';
import { Tabs } from 'antd';
import ServiceDetail from './ServiceDetail';
import { CategoryOclVo, OclDetailVo, ProviderOclVo, VersionOclVo } from '../../../../xpanse-api/generated';
import { Tab } from 'rc-tabs/lib/interface';

let lastServiceName: string = '';

function ServiceProvider({
    categoryOclData,
    serviceName,
}: {
    categoryOclData: CategoryOclVo[];
    serviceName: string;
}): JSX.Element {
    const [activeKey, setActiveKey] = useState<string>('');
    const [serviceDetails, setServiceDetails] = useState<OclDetailVo>(new OclDetailVo());
    const [serviceRegions, setServiceRegions] = useState<string[]>([]);

    const detailMapper: Map<string, OclDetailVo> = new Map<string, OclDetailVo>();
    const regionMapper: Map<string, string[]> = new Map<string, string[]>();
    const [name, version] = serviceName.split('@');

    const items: Tab[] = categoryOclData
        .filter((service: CategoryOclVo) => service.name === name)
        .flatMap((service: CategoryOclVo) => service.versions)
        .filter((v) => (v as VersionOclVo).version === version)
        .flatMap((v) => {
            if (!v || !v.cloudProvider) {
                return { key: '', label: '' };
            }
            return v.cloudProvider.map((cloudProvider: ProviderOclVo) => {
                if (!cloudProvider.details) {
                    return { key: '', label: '' };
                }
                const key = serviceName + '@' + cloudProvider.name;
                detailMapper.set(key, cloudProvider.details[0]);
                regionMapper.set(key, cloudProvider.regions || []);
                const name = cloudProvider.name!.toString();
                return {
                    label: name,
                    key: name,
                    children: [],
                };
            });
        });

    useEffect(() => {
        if (items.length > 0 && lastServiceName !== serviceName) {
            const key = serviceName + '@' + items[0].key;
            const details = detailMapper.get(key);
            if (details) {
                setServiceDetails(details);
            }
            const regions = regionMapper.get(key);
            if (regions) {
                setServiceRegions(regions);
            }
            setActiveKey(items[0]!.key);
        }
        lastServiceName = serviceName;
    }, [items]);

    const onChange = (key: string) => {
        setActiveKey(key);
        const serviceKey = serviceName + '@' + key;
        const regions = regionMapper.get(serviceKey);
        const details = detailMapper.get(serviceKey);
        if (details) {
            setServiceDetails(details);
        }
        if (regions) {
            setServiceRegions(regions);
        }
    };

    return (
        <>
            {serviceName.length > 0 ? (
                <>
                    <Tabs items={items} onChange={onChange} activeKey={activeKey} className={'ant-tabs-tab-btn'} />
                    <ServiceDetail serviceDetails={serviceDetails} serviceRegions={serviceRegions} />
                </>
            ) : (
                <></>
            )}
        </>
    );
}

export default ServiceProvider;
