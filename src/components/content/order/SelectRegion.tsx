/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { useEffect, useState } from 'react';
import { Select, Space, Tabs } from 'antd';
import { SelectFlavor } from './SelectFlavor';
import { Tab } from 'rc-tabs/lib/interface';

export const SelectRegion = ({
    areaValue,
    areaList,
}: {
    areaValue: string;
    areaList: ServiceVendor.Area[];
}): JSX.Element => {
    const [regionValue, setRegionValue] = useState<string>('');
    const handleChangeRegion = (value: string) => {
        console.log(`selected ${value}`);
        setRegionValue(value);
    };

    // @ts-ignore
    const items: OptionType[] = areaList
        .filter((v) => (v as ServiceVendor.Area).name === areaValue)
        .flatMap((v) => {
            if (!v || !v.regionList) {
                return { value: '', label: '' };
            }
            return v.regionList.map((region: ServiceVendor.Region) => {
                if (!region.name) {
                    return { value: '', label: '' };
                }
                const name = region.name!.toString();
                return {
                    label: name,
                    value: name,
                };
            });
        });

    return (
        <div>
            Region:&nbsp;
            <Space wrap>
                <Select
                    defaultValue='lucy'
                    style={{ width: 120 }}
                    onChange={handleChangeRegion}
                    options={[
                        { value: 'jack', label: 'Jack' },
                        { value: 'lucy', label: 'Lucy' },
                        { value: 'Yiminghe', label: 'yiminghe' },
                        { value: 'disabled', label: 'Disabled', disabled: true },
                    ]}
                />
            </Space>
            <SelectFlavor regionValue={regionValue} />
        </div>
    );
};
