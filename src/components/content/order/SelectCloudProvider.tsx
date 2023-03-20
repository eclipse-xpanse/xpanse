/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { useEffect, useState } from 'react';
import { Tabs } from 'antd';
import { Tab } from 'rc-tabs/lib/interface';
import { SelectArea } from './SelectArea';

type TabPosition = 'left' | 'right' | 'top' | 'bottom';
export const SelectCloudProvider = ({
    versionValue,
    versionList,
}: {
    versionValue: string;
    versionList: ServiceVendor.Version[];
}): JSX.Element => {
    const [tabPosition, setTabPosition] = useState<TabPosition>('bottom');
    const [cloudProviderValue, setCloudProviderValue] = useState<string>('');
    const areaMapper: Map<string, ServiceVendor.Area[]> = new Map<string, ServiceVendor.Area[]>();
    const onChange = (key: string) => {
        setCloudProviderValue(key);
    };

    const items: Tab[] = versionList
        .filter((v) => (v as ServiceVendor.Version).version === versionValue)
        .flatMap((v) => {
            if (!v || !v.cloudProviderList) {
                return { key: '', label: '' };
            }
            return v.cloudProviderList.map((cloudProvider: ServiceVendor.CloudProvider) => {
                if (!cloudProvider.name) {
                    return { key: '', label: '' };
                }
                areaMapper.set(cloudProvider.name, cloudProvider.areaList || []);
                const name = cloudProvider.name!.toString();
                return {
                    label: name,
                    key: name,
                    children: ['CloudProvider： '.concat(name)],
                };
            });
        });

    console.log('areaMapper: ', areaMapper);

    return (
        <div>
            <Tabs tabPosition={tabPosition} items={items} onChange={onChange} />
            <SelectArea cloudProviderValue={cloudProviderValue} areaMapper={areaMapper} />
        </div>
    );
};
