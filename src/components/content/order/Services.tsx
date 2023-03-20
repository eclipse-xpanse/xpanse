/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { FormOutlined } from '@ant-design/icons';
import '../../../styles/services.css';
import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { createServicePageRoute } from '../../utils/constants';
import { getServiceList } from '../../../xpanse-api/service-vendor/api';

function Services(): JSX.Element {
    const [services, setServices] = useState<{ name: string; content: string }[]>([]);
    const [isSelected, setIsSelected] = useState<number>();
    const navigate = useNavigate();
    const location = useLocation();

    const onClicked = function (cfg: string) {
        console.log('cfg: ', cfg);
        navigate(
            createServicePageRoute
                .concat('?serviceName=', location.hash.split('#')[1])
                .concat('&name=', cfg.replace(' ', ''))
        );
    };

    useEffect(() => {
        getServiceList().then((rsp) => {
            let serviceList: { name: string; content: string }[] = [];
            rsp.data.data.forEach((item) => {
                let serviceItem = {
                    name: item.name,
                    content: item.content,
                };
                serviceList.push(serviceItem);
            });
            setServices(serviceList);
        });
    }, []);

    return (
        <div className={'services-content'}>
            <div className={'content-title'}>
                <FormOutlined />
                &nbsp;Select Service
            </div>
            <div className={'services-content-body'}>
                {services.map((item, index) => {
                    return (
                        <div
                            key={index}
                            className={
                                isSelected === index
                                    ? 'service-type-option-detail-selected'
                                    : 'service-type-option-detail'
                            }
                            onClick={(e) => onClicked(item.name)}
                            onMouseOver={() => {
                                setIsSelected(index);
                            }}
                        >
                            <div className='service-type-option-image'>
                                <img className='service-type-option-service-icon' />
                            </div>
                            <div className='service-type-option-info'>
                                <span className='service-type-option'>{item.name}</span>
                                <span className='service-type-option-description'>{item.content}</span>
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}

export default Services;
