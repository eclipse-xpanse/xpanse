/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import React, { useEffect, useState } from 'react';
import '../../../styles/catalog.css';
import { DataNode } from 'antd/es/tree';
import ServiceTree from './services/ServiceTree';
import ServiceProvider from './services/ServiceProvider';
import { HomeOutlined } from '@ant-design/icons';
import { useLocation } from 'react-router-dom';
import { serviceVendorApi } from '../../../xpanse-api/xpanseRestApiClient';
import { CategoryOclVo, VersionOclVo } from '../../../xpanse-api/generated';

function Catalog(): JSX.Element {
    const [key, setKey] = useState<string>('');
    const [treeData, setTreeData] = useState<DataNode[]>([]);
    const [categoryOclData, setCategoryOclData] = useState<CategoryOclVo[]>([]);
    const location = useLocation();

    useEffect(() => {
        const path = location.hash.split('#')[1];
        if (!path) {
            return;
        }
        serviceVendorApi.listRegisteredServicesTree(path).then((data) => {
            setCategoryOclData(data);
            let tData: DataNode[] = [];
            data.forEach((service) => {
                let dn: DataNode = {
                    title: service.name,
                    key: service.name || '',
                    children: [],
                };
                const versionList: VersionOclVo[] = service.versions || [];
                versionList.forEach((v: VersionOclVo) => {
                    dn.children!.push({
                        title: v.version,
                        key: service.name + '@' + v.version,
                    });
                });
                tData.push(dn);
            });
            setTreeData(tData);
        });
    }, [location]);

    return (
        <div className={'catalog-middleware'}>
            <div className={'container'}>
                <div className={'left-class'}>
                    <div className={'left-title-class'}>
                        <HomeOutlined />
                        &ensp;Service Tree
                    </div>
                    <ServiceTree treeData={treeData} setKey={setKey} />
                </div>
                <div className={'middle-class'}></div>
                <div className={'right-class'}>
                    <div className={'left-title-class'}>Cloud Provider</div>
                    <ServiceProvider categoryOclData={categoryOclData} serviceName={key} />
                </div>
            </div>
        </div>
    );
}

export default Catalog;
