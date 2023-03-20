/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { useEffect, useState } from 'react';
import { Tabs } from 'antd';
import { SelectRegion } from './SelectRegion';
import { Tab } from 'rc-tabs/lib/interface';

type TabPosition = 'left' | 'right' | 'top' | 'bottom';
export const SelectArea = ({
    cloudProviderValue,
    areaMapper,
}: {
    cloudProviderValue: string;
    areaMapper: Map<string, ServiceVendor.Area[]>;
}): JSX.Element => {
    const [tabPosition, setTabPosition] = useState<TabPosition>('bottom');
    const [areaValue, setAreaValue] = useState<string>('');
    const [areaList, setAreaList] = useState<ServiceVendor.Area[]>([]);
    const [items, setItems] = useState<Tab[]>([]);

    const onChange = (key: string) => {
        setAreaValue(key);
    };
    useEffect(() => {
        const areaList: ServiceVendor.Area[] = areaMapper.get(cloudProviderValue) || [];
        setAreaList(areaList);
        setItems(
            areaList.map((area: ServiceVendor.Area) => {
                if (!area.name) {
                    return { key: '', label: '' };
                }
                const name = area.name;
                return {
                    label: name,
                    key: name,
                    children: ['Area： '.concat(name)],
                };
            })
        );
    }, [cloudProviderValue, areaMapper]);

    return (
        <div>
            {/*<Tabs defaultActiveKey='1' items={items} onChange={onChange} />*/}
            <Tabs tabPosition={tabPosition} items={items} onChange={onChange} />
            <SelectRegion areaValue={areaValue} areaList={areaList} />
        </div>
    );
};
