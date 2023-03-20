/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Button, Divider, Select } from 'antd';
import { LeftOutlined } from '@ant-design/icons';
import { SelectCloudProvider } from './SelectCloudProvider';
import { getVersionList } from '../../../xpanse-api/service-vendor/api';

function CreateService(): JSX.Element {
    const navigate = useNavigate();
    const [versionOptions, setVersionOptions] = useState<{ value: string; label: string }[]>([]);
    const [versionValue, setVersionValue] = useState<string>('');
    const [serviceName, setServiceName] = useState<string>('');
    const [versionList, setVersionList] = useState<ServiceVendor.Version[]>([]);
    const location = useLocation();

    const handleChangeVersion = (value: string) => {
        setVersionValue(value);
    };

    const goBackPage = function (cfg: any) {
        navigate(-1);
    };

    useEffect(() => {
        const serviceName = location.search.split('?')[1].split('&')[0].split('=')[1];
        const subServiceName = location.search.split('?')[1].split('&')[1].split('=')[1];
        setServiceName(serviceName);
        // TODO Two parameters need to be passed in
        // getVersionList(serviceName, subServiceName).then((rsp)=>{
        getVersionList().then((rsp) => {
            setVersionList(rsp.data.data);
            let versions: { value: string; label: string }[] = [];
            rsp.data.data.forEach((item) => {
                let versionItem = { value: item.version, label: item.version };
                versions.push(versionItem);
            });
            setVersionOptions(versions);
            if (versions.length > 0) {
                setVersionValue(versions[0].value);
            }
        });
    }, [location]);

    return (
        <div className={'services-content'}>
            <div className={'back-button-class'}>
                <Button type='text' onClick={goBackPage}>
                    <LeftOutlined />
                    Back
                </Button>
            </div>
            <div className={'content-title'}>
                Service: {serviceName}&nbsp;&nbsp;&nbsp;&nbsp; Version:&nbsp;
                <Select
                    value={versionValue}
                    style={{ width: 120 }}
                    onChange={handleChangeVersion}
                    options={versionOptions}
                />
            </div>
            <Divider />
            <SelectCloudProvider versionValue={versionValue} versionList={versionList} />
        </div>
    );
}

export default CreateService;
