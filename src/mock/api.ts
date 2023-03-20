// @ts-ignore
import Mock from 'mockjs';

Mock.mock(`/xpanse/versions/`, 'get', {
    data: ['3.1.0', '3.2.0', '3.3.0'],
});

Mock.mock(`/\\/xpanse\\/versions\\/*/`, 'get', {
    data: {
        name: 'kafka',
        versionList: ['1.0', '2.0', '3.0'],
    },
});

Mock.mock(`/xpanse/register/categories`, 'get', {
    data: ['ai', 'middleware', 'other'],
});

Mock.mock(`/xpanse/service`, 'get', {
    data: [
        {
            content: 'Kafka Info',
            name: 'kafka',
            versionList: [
                {
                    version: '1.2',
                    cloudProviderList: [
                        {
                            name: 'huaweicloud',
                            details: {
                                product: '@cname',
                                time: '@email',
                                amount: /^1[34578]\d{9}$/,
                                discount: '@city()',
                                official: '@zip()',
                            },
                            areaList: [
                                { name: 'Asia', region: ['cn-north-1', 'cn-north-2'] },
                                { name: 'Europe', region: ['cn-north-1', 'cn-north-2'] },
                            ],
                        },
                        {
                            name: 'aws',
                            details: {
                                product: '@cname',
                                time: '@email',
                                amount: /^1[34578]\d{9}$/,
                                discount: '@city()',
                                official: '@zip()',
                            },
                        },
                    ],
                },
                {
                    version: '1.3',
                    cloudProviderList: [
                        {
                            areaList: [
                                { name: 'Asia', region: ['cn-north-1', 'cn-north-2'] },
                                { name: 'Europe', region: ['cn-north-1', 'cn-north-2'] },
                            ],
                            name: 'huaweicloud',
                            details: {
                                product: '@cname',
                                time: '@email',
                                amount: /^1[34578]\d{9}$/,
                                discount: '@city()',
                                official: '@zip()',
                            },
                        },
                        {
                            areaList: [
                                { name: 'Asia', region: ['cn-north-1', 'cn-north-2'] },
                                { name: 'Europe', region: ['cn-north-1', 'cn-north-2'] },
                            ],
                            name: 'azure',
                            details: {
                                product: '@cname',
                                time: '@email',
                                amount: /^1[34578]\d{9}$/,
                                discount: '@city()',
                                official: '@zip()',
                            },
                        },
                    ],
                },
            ],
        },
        {
            content: 'Kafka Info',
            name: 'Rocket MQ',
            versionList: [
                {
                    version: '3.4',
                    cloudProviderList: [
                        {
                            areaList: [
                                { name: 'Asia', region: ['cn-north-1', 'cn-north-2'] },
                                { name: 'Europe', region: ['cn-north-1', 'cn-north-2'] },
                            ],
                            name: 'huaweicloud',
                            details: {
                                product: '@cname',
                                time: '@email',
                                amount: /^1[34578]\d{9}$/,
                                discount: '@city()',
                                official: '@zip()',
                            },
                        },
                        {
                            areaList: [
                                { name: 'Asia', region: ['cn-north-1', 'cn-north-2'] },
                                { name: 'Europe', region: ['cn-north-1', 'cn-north-2'] },
                            ],
                            name: 'AZure',
                            details: {
                                product: '@cname',
                                time: '@email',
                                amount: /^1[34578]\d{9}$/,
                                discount: '@city()',
                                official: '@zip()',
                            },
                        },
                    ],
                },
                {
                    version: '3.5',
                    cloudProviderList: [
                        {
                            areaList: [
                                { name: 'Asia', region: ['cn-north-1', 'cn-north-2'] },
                                { name: 'Europe', region: ['cn-north-1', 'cn-north-2'] },
                            ],
                            name: 'huaweicloud',
                            details: {
                                product: '@cname',
                                time: '@email',
                                amount: /^1[34578]\d{9}$/,
                                discount: '@city()',
                                official: '@zip()',
                            },
                        },
                        {
                            areaList: [
                                { name: 'Asia', region: ['cn-north-1', 'cn-north-2'] },
                                { name: 'Europe', region: ['cn-north-1', 'cn-north-2'] },
                            ],
                            name: 'AWS',
                            details: {
                                product: '@cname',
                                time: '@email',
                                amount: /^1[34578]\d{9}$/,
                                discount: '@city()',
                                official: '@zip()',
                            },
                        },
                        {
                            areaList: [
                                { name: 'Asia', region: ['cn-north-1', 'cn-north-2'] },
                                { name: 'Europe', region: ['cn-north-1', 'cn-north-2'] },
                            ],
                            name: 'AZure',
                            details: {
                                product: '@cname',
                                time: '@email',
                                amount: /^1[34578]\d{9}$/,
                                discount: '@city()',
                                official: '@zip()',
                            },
                        },
                    ],
                },
            ],
        },
    ],
});

Mock.mock(`/xpanse/serviceList`, 'get', {
    data: [
        {
            content: 'Kafka Info',
            name: 'kafka',
        },
        {
            content: 'Rocket MQ Info',
            name: 'Rocket MQ',
        },
    ],
});

Mock.mock(`/xpanse/versionList`, 'get', {
    data: [
        {
            version: '1.2',
            cloudProviderList: [
                {
                    name: 'huaweicloud',
                    details: {
                        product: '@cname',
                        'billing|18-60': 1,
                        time: '@email',
                        amount: /^1[34578]\d{9}$/,
                        discount: '@city()',
                        official: '@zip()',
                    },
                    areaList: [
                        { name: 'Asia111', region: ['cn-north-11', 'cn-north-12'] },
                        { name: 'Europe111', region: ['cn-north-1111', 'cn-north-1112'] },
                    ],
                },
                {
                    name: 'aws',
                    details: {
                        product: '@cname',
                        'billing|18-60': 1,
                        time: '@email',
                        amount: /^1[34578]\d{9}$/,
                        discount: '@city()',
                        official: '@zip()',
                    },
                    areaList: [
                        { name: 'Asia222', region: ['cn-north-21', 'cn-north-22'] },
                        { name: 'Europe222', region: ['cn-north-221', 'cn-north-221'] },
                    ],
                },
            ],
        },
        {
            version: '1.3',
            cloudProviderList: [
                {
                    areaList: [
                        { name: 'Asia333', region: ['cn-north-31', 'cn-north-32'] },
                        { name: 'Europe333', region: ['cn-north-331', 'cn-north-332'] },
                    ],
                    name: 'huaweicloud',
                    details: {
                        product: '@cname',
                        'billing|18-60': 1,
                        time: '@email',
                        amount: /^1[34578]\d{9}$/,
                        discount: '@city()',
                        official: '@zip()',
                    },
                },
                {
                    areaList: [
                        { name: 'Asia444', region: ['cn-north-41', 'cn-north-42'] },
                        { name: 'Europe444', region: ['cn-north-441', 'cn-north-442'] },
                    ],
                    name: 'azure',
                    details: {
                        product: '@cname',
                        'billing|18-60': 1,
                        time: '@email',
                        amount: /^1[34578]\d{9}$/,
                        discount: '@city()',
                        official: '@zip()',
                    },
                },
            ],
        },
    ],
});
