/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

/**
 * OpenAPI definition
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * OpenAPI spec version: v0
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

import { DeployResource } from './DeployResource';

export class DeployResult {
    'id'?: string;
    'state'?: DeployResultStateEnum;
    'resources'?: Array<DeployResource>;
    'rawResources'?: { [key: string]: any };

    static readonly discriminator: string | undefined = undefined;

    static readonly attributeTypeMap: Array<{ name: string; baseName: string; type: string; format: string }> = [
        {
            name: 'id',
            baseName: 'id',
            type: 'string',
            format: 'uuid',
        },
        {
            name: 'state',
            baseName: 'state',
            type: 'DeployResultStateEnum',
            format: '',
        },
        {
            name: 'resources',
            baseName: 'resources',
            type: 'Array<DeployResource>',
            format: '',
        },
        {
            name: 'rawResources',
            baseName: 'rawResources',
            type: '{ [key: string]: any; }',
            format: '',
        },
    ];

    static getAttributeTypeMap() {
        return DeployResult.attributeTypeMap;
    }

    public constructor() {}
}

export type DeployResultStateEnum = 'initial' | 'success' | 'failed' | 'destroy_success' | 'destroy_failed';
