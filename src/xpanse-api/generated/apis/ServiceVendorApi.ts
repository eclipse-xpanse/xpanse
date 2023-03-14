/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { BaseAPIRequestFactory, RequiredError } from './baseapi';
import { Configuration } from '../configuration';
import { HttpMethod, RequestContext, ResponseContext } from '../http/http';
import { ObjectSerializer, OclDetailVo } from '../models/ObjectSerializer';
import { ApiException } from './exception';
import { isCodeInRange } from '../util';
import { SecurityAuthentication } from '../auth/auth';
import { Ocl } from '../models/Ocl';
import { CategoryOclVo } from '../models/CategoryOclVo';
import { RegisterServiceEntity } from '../models/RegisterServiceEntity';

/**
 * no description
 */
export class ServiceVendorApiRequestFactory extends BaseAPIRequestFactory {
    /**
     * Get registered service using id.
     * @param id id of registered service
     */
    public async detail(id: string, _options?: Configuration): Promise<RequestContext> {
        let _config = _options || this.configuration;

        // verify required parameter 'id' is not null or undefined
        if (id === null || id === undefined) {
            throw new RequiredError('ServiceVendorApi', 'detail', 'id');
        }

        // Path Params
        const localVarPath = '/xpanse/register/{id}'.replace('{' + 'id' + '}', encodeURIComponent(String(id)));

        // Make Request Context
        const requestContext = _config.baseServer.makeRequestContext(localVarPath, HttpMethod.GET);
        requestContext.setHeaderParam('Accept', 'application/json, */*;q=0.8');

        const defaultAuth: SecurityAuthentication | undefined =
            _options?.authMethods?.default || this.configuration?.authMethods?.default;
        if (defaultAuth?.applySecurityAuthentication) {
            await defaultAuth?.applySecurityAuthentication(requestContext);
        }

        return requestContext;
    }

    /**
     * Register new service with URL of Ocl file.
     * @param oclLocation URL of Ocl file
     */
    public async fetch(oclLocation: string, _options?: Configuration): Promise<RequestContext> {
        let _config = _options || this.configuration;

        // verify required parameter 'oclLocation' is not null or undefined
        if (oclLocation === null || oclLocation === undefined) {
            throw new RequiredError('ServiceVendorApi', 'fetch', 'oclLocation');
        }

        // Path Params
        const localVarPath = '/xpanse/register/file';

        // Make Request Context
        const requestContext = _config.baseServer.makeRequestContext(localVarPath, HttpMethod.POST);
        requestContext.setHeaderParam('Accept', 'application/json, */*;q=0.8');

        // Query Params
        if (oclLocation !== undefined) {
            requestContext.setQueryParam('oclLocation', ObjectSerializer.serialize(oclLocation, 'string', ''));
        }

        const defaultAuth: SecurityAuthentication | undefined =
            _options?.authMethods?.default || this.configuration?.authMethods?.default;
        if (defaultAuth?.applySecurityAuthentication) {
            await defaultAuth?.applySecurityAuthentication(requestContext);
        }

        return requestContext;
    }

    /**
     * Update registered service using id and ocl file url.
     * @param id id of registered service
     * @param oclLocation URL of Ocl file
     */
    public async fetchUpdate(id: string, oclLocation: string, _options?: Configuration): Promise<RequestContext> {
        let _config = _options || this.configuration;

        // verify required parameter 'id' is not null or undefined
        if (id === null || id === undefined) {
            throw new RequiredError('ServiceVendorApi', 'fetchUpdate', 'id');
        }

        // verify required parameter 'oclLocation' is not null or undefined
        if (oclLocation === null || oclLocation === undefined) {
            throw new RequiredError('ServiceVendorApi', 'fetchUpdate', 'oclLocation');
        }

        // Path Params
        const localVarPath = '/xpanse/register/file/{id}'.replace('{' + 'id' + '}', encodeURIComponent(String(id)));

        // Make Request Context
        const requestContext = _config.baseServer.makeRequestContext(localVarPath, HttpMethod.PUT);
        requestContext.setHeaderParam('Accept', 'application/json, */*;q=0.8');

        // Query Params
        if (oclLocation !== undefined) {
            requestContext.setQueryParam('oclLocation', ObjectSerializer.serialize(oclLocation, 'string', ''));
        }

        const defaultAuth: SecurityAuthentication | undefined =
            _options?.authMethods?.default || this.configuration?.authMethods?.default;
        if (defaultAuth?.applySecurityAuthentication) {
            await defaultAuth?.applySecurityAuthentication(requestContext);
        }

        return requestContext;
    }

    /**
     * Get category list.
     */
    public async listCategories(_options?: Configuration): Promise<RequestContext> {
        let _config = _options || this.configuration;

        // Path Params
        const localVarPath = '/xpanse/register/categories';

        // Make Request Context
        const requestContext = _config.baseServer.makeRequestContext(localVarPath, HttpMethod.GET);
        requestContext.setHeaderParam('Accept', 'application/json, */*;q=0.8');

        const defaultAuth: SecurityAuthentication | undefined =
            _options?.authMethods?.default || this.configuration?.authMethods?.default;
        if (defaultAuth?.applySecurityAuthentication) {
            await defaultAuth?.applySecurityAuthentication(requestContext);
        }

        return requestContext;
    }

    /**
     * List registered service with query params.
     * @param categoryName category of the service
     * @param cspName name of the service provider
     * @param serviceName name of the service
     * @param serviceVersion version of the service
     */
    public async listRegisteredServices(
        categoryName?: string,
        cspName?: string,
        serviceName?: string,
        serviceVersion?: string,
        _options?: Configuration
    ): Promise<RequestContext> {
        let _config = _options || this.configuration;

        // Path Params
        const localVarPath = '/xpanse/register';

        // Make Request Context
        const requestContext = _config.baseServer.makeRequestContext(localVarPath, HttpMethod.GET);
        requestContext.setHeaderParam('Accept', 'application/json, */*;q=0.8');

        // Query Params
        if (categoryName !== undefined) {
            requestContext.setQueryParam('categoryName', ObjectSerializer.serialize(categoryName, 'string', ''));
        }

        // Query Params
        if (cspName !== undefined) {
            requestContext.setQueryParam('cspName', ObjectSerializer.serialize(cspName, 'string', ''));
        }

        // Query Params
        if (serviceName !== undefined) {
            requestContext.setQueryParam('serviceName', ObjectSerializer.serialize(serviceName, 'string', ''));
        }

        // Query Params
        if (serviceVersion !== undefined) {
            requestContext.setQueryParam('serviceVersion', ObjectSerializer.serialize(serviceVersion, 'string', ''));
        }

        const defaultAuth: SecurityAuthentication | undefined =
            _options?.authMethods?.default || this.configuration?.authMethods?.default;
        if (defaultAuth?.applySecurityAuthentication) {
            await defaultAuth?.applySecurityAuthentication(requestContext);
        }

        return requestContext;
    }

    /**
     * List registered service group by serviceName, serviceVersion, cspName with category.
     * @param categoryName category of the service
     */
    public async listRegisteredServicesTree(categoryName: string, _options?: Configuration): Promise<RequestContext> {
        let _config = _options || this.configuration;

        // verify required parameter 'categoryName' is not null or undefined
        if (categoryName === null || categoryName === undefined) {
            throw new RequiredError('ServiceVendorApi', 'listRegisteredServicesTree', 'categoryName');
        }

        // Path Params
        const localVarPath = '/xpanse/register/category/{categoryName}'.replace(
            '{' + 'categoryName' + '}',
            encodeURIComponent(String(categoryName))
        );

        // Make Request Context
        const requestContext = _config.baseServer.makeRequestContext(localVarPath, HttpMethod.GET);
        requestContext.setHeaderParam('Accept', 'application/json, */*;q=0.8');

        const defaultAuth: SecurityAuthentication | undefined =
            _options?.authMethods?.default || this.configuration?.authMethods?.default;
        if (defaultAuth?.applySecurityAuthentication) {
            await defaultAuth?.applySecurityAuthentication(requestContext);
        }

        return requestContext;
    }

    /**
     * Register new service using ocl model.
     * @param ocl
     */
    public async register(ocl: Ocl, _options?: Configuration): Promise<RequestContext> {
        let _config = _options || this.configuration;

        // verify required parameter 'ocl' is not null or undefined
        if (ocl === null || ocl === undefined) {
            throw new RequiredError('ServiceVendorApi', 'register', 'ocl');
        }

        // Path Params
        const localVarPath = '/xpanse/register';

        // Make Request Context
        const requestContext = _config.baseServer.makeRequestContext(localVarPath, HttpMethod.POST);
        requestContext.setHeaderParam('Accept', 'application/json, */*;q=0.8');

        // Body Params
        const contentType = ObjectSerializer.getPreferredMediaType([
            'application/x-yaml',

            'application/yml',

            'application/yaml',
        ]);
        requestContext.setHeaderParam('Content-Type', contentType);
        const serializedBody = ObjectSerializer.stringify(ObjectSerializer.serialize(ocl, 'Ocl', ''), contentType);
        requestContext.setBody(serializedBody);

        const defaultAuth: SecurityAuthentication | undefined =
            _options?.authMethods?.default || this.configuration?.authMethods?.default;
        if (defaultAuth?.applySecurityAuthentication) {
            await defaultAuth?.applySecurityAuthentication(requestContext);
        }

        return requestContext;
    }

    /**
     * Unregister registered service using id.
     * @param id id of registered service
     */
    public async unregister(id: string, _options?: Configuration): Promise<RequestContext> {
        let _config = _options || this.configuration;

        // verify required parameter 'id' is not null or undefined
        if (id === null || id === undefined) {
            throw new RequiredError('ServiceVendorApi', 'unregister', 'id');
        }

        // Path Params
        const localVarPath = '/xpanse/register/{id}'.replace('{' + 'id' + '}', encodeURIComponent(String(id)));

        // Make Request Context
        const requestContext = _config.baseServer.makeRequestContext(localVarPath, HttpMethod.DELETE);
        requestContext.setHeaderParam('Accept', 'application/json, */*;q=0.8');

        const defaultAuth: SecurityAuthentication | undefined =
            _options?.authMethods?.default || this.configuration?.authMethods?.default;
        if (defaultAuth?.applySecurityAuthentication) {
            await defaultAuth?.applySecurityAuthentication(requestContext);
        }

        return requestContext;
    }

    /**
     * Update registered service using id and ocl model.
     * @param id id of registered service
     * @param ocl
     */
    public async update(id: string, ocl: Ocl, _options?: Configuration): Promise<RequestContext> {
        let _config = _options || this.configuration;

        // verify required parameter 'id' is not null or undefined
        if (id === null || id === undefined) {
            throw new RequiredError('ServiceVendorApi', 'update', 'id');
        }

        // verify required parameter 'ocl' is not null or undefined
        if (ocl === null || ocl === undefined) {
            throw new RequiredError('ServiceVendorApi', 'update', 'ocl');
        }

        // Path Params
        const localVarPath = '/xpanse/register/{id}'.replace('{' + 'id' + '}', encodeURIComponent(String(id)));

        // Make Request Context
        const requestContext = _config.baseServer.makeRequestContext(localVarPath, HttpMethod.PUT);
        requestContext.setHeaderParam('Accept', 'application/json, */*;q=0.8');

        // Body Params
        const contentType = ObjectSerializer.getPreferredMediaType([
            'application/x-yaml',

            'application/yml',

            'application/yaml',
        ]);
        requestContext.setHeaderParam('Content-Type', contentType);
        const serializedBody = ObjectSerializer.stringify(ObjectSerializer.serialize(ocl, 'Ocl', ''), contentType);
        requestContext.setBody(serializedBody);

        const defaultAuth: SecurityAuthentication | undefined =
            _options?.authMethods?.default || this.configuration?.authMethods?.default;
        if (defaultAuth?.applySecurityAuthentication) {
            await defaultAuth?.applySecurityAuthentication(requestContext);
        }

        return requestContext;
    }
}

export class ServiceVendorApiResponseProcessor {
    /**
     * Unwraps the actual response sent by the server from the response context and deserializes the response content
     * to the expected objects
     *
     * @params response Response returned by the server for a request to detail
     * @throws ApiException if the response code was not in [200, 299]
     */
    public async detail(response: ResponseContext): Promise<OclDetailVo> {
        const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
        if (isCodeInRange('400', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Bad Request', body, response.headers);
        }
        if (isCodeInRange('500', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
        }
        if (isCodeInRange('404', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Not Found', body, response.headers);
        }
        if (isCodeInRange('200', response.httpStatusCode)) {
            const body: OclDetailVo = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'OclDetailVo',
                ''
            ) as OclDetailVo;
            return body;
        }

        // Work around for missing responses in specification, e.g. for petstore.yaml
        if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
            const body: OclDetailVo = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'OclDetailVo',
                ''
            ) as OclDetailVo;
            return body;
        }

        throw new ApiException<string | Blob | undefined>(
            response.httpStatusCode,
            'Unknown API Status Code!',
            await response.getBodyAsAny(),
            response.headers
        );
    }

    /**
     * Unwraps the actual response sent by the server from the response context and deserializes the response content
     * to the expected objects
     *
     * @params response Response returned by the server for a request to fetch
     * @throws ApiException if the response code was not in [200, 299]
     */
    public async fetch(response: ResponseContext): Promise<string> {
        const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
        if (isCodeInRange('400', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                'uuid'
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Bad Request', body, response.headers);
        }
        if (isCodeInRange('500', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                'uuid'
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
        }
        if (isCodeInRange('404', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                'uuid'
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Not Found', body, response.headers);
        }
        if (isCodeInRange('200', response.httpStatusCode)) {
            const body: string = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'string',
                'uuid'
            ) as string;
            return body;
        }

        // Work around for missing responses in specification, e.g. for petstore.yaml
        if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
            const body: string = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'string',
                'uuid'
            ) as string;
            return body;
        }

        throw new ApiException<string | Blob | undefined>(
            response.httpStatusCode,
            'Unknown API Status Code!',
            await response.getBodyAsAny(),
            response.headers
        );
    }

    /**
     * Unwraps the actual response sent by the server from the response context and deserializes the response content
     * to the expected objects
     *
     * @params response Response returned by the server for a request to fetchUpdate
     * @throws ApiException if the response code was not in [200, 299]
     */
    public async fetchUpdate(response: ResponseContext): Promise<Response> {
        const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
        if (isCodeInRange('400', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Bad Request', body, response.headers);
        }
        if (isCodeInRange('500', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
        }
        if (isCodeInRange('404', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Not Found', body, response.headers);
        }
        if (isCodeInRange('200', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            return body;
        }

        // Work around for missing responses in specification, e.g. for petstore.yaml
        if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            return body;
        }

        throw new ApiException<string | Blob | undefined>(
            response.httpStatusCode,
            'Unknown API Status Code!',
            await response.getBodyAsAny(),
            response.headers
        );
    }

    /**
     * Unwraps the actual response sent by the server from the response context and deserializes the response content
     * to the expected objects
     *
     * @params response Response returned by the server for a request to listCategories
     * @throws ApiException if the response code was not in [200, 299]
     */
    public async listCategories(response: ResponseContext): Promise<Array<string>> {
        const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
        if (isCodeInRange('400', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Bad Request', body, response.headers);
        }
        if (isCodeInRange('500', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
        }
        if (isCodeInRange('404', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Not Found', body, response.headers);
        }
        if (isCodeInRange('200', response.httpStatusCode)) {
            const body: Array<string> = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Array<string>',
                ''
            ) as Array<string>;
            return body;
        }

        // Work around for missing responses in specification, e.g. for petstore.yaml
        if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
            const body: Array<string> = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Array<string>',
                ''
            ) as Array<string>;
            return body;
        }

        throw new ApiException<string | Blob | undefined>(
            response.httpStatusCode,
            'Unknown API Status Code!',
            await response.getBodyAsAny(),
            response.headers
        );
    }

    /**
     * Unwraps the actual response sent by the server from the response context and deserializes the response content
     * to the expected objects
     *
     * @params response Response returned by the server for a request to listRegisteredServices
     * @throws ApiException if the response code was not in [200, 299]
     */
    public async listRegisteredServices(response: ResponseContext): Promise<Array<RegisterServiceEntity>> {
        const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
        if (isCodeInRange('400', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Bad Request', body, response.headers);
        }
        if (isCodeInRange('500', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
        }
        if (isCodeInRange('404', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Not Found', body, response.headers);
        }
        if (isCodeInRange('200', response.httpStatusCode)) {
            const body: Array<RegisterServiceEntity> = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Array<RegisterServiceEntity>',
                ''
            ) as Array<RegisterServiceEntity>;
            return body;
        }

        // Work around for missing responses in specification, e.g. for petstore.yaml
        if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
            const body: Array<RegisterServiceEntity> = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Array<RegisterServiceEntity>',
                ''
            ) as Array<RegisterServiceEntity>;
            return body;
        }

        throw new ApiException<string | Blob | undefined>(
            response.httpStatusCode,
            'Unknown API Status Code!',
            await response.getBodyAsAny(),
            response.headers
        );
    }

    /**
     * Unwraps the actual response sent by the server from the response context and deserializes the response content
     * to the expected objects
     *
     * @params response Response returned by the server for a request to listRegisteredServicesTree
     * @throws ApiException if the response code was not in [200, 299]
     */
    public async listRegisteredServicesTree(response: ResponseContext): Promise<Array<CategoryOclVo>> {
        const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
        if (isCodeInRange('400', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Bad Request', body, response.headers);
        }
        if (isCodeInRange('500', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
        }
        if (isCodeInRange('404', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Not Found', body, response.headers);
        }
        if (isCodeInRange('200', response.httpStatusCode)) {
            const body: Array<CategoryOclVo> = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Array<CategoryOclVo>',
                ''
            ) as Array<CategoryOclVo>;
            return body;
        }

        // Work around for missing responses in specification, e.g. for petstore.yaml
        if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
            const body: Array<CategoryOclVo> = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Array<CategoryOclVo>',
                ''
            ) as Array<CategoryOclVo>;
            return body;
        }

        throw new ApiException<string | Blob | undefined>(
            response.httpStatusCode,
            'Unknown API Status Code!',
            await response.getBodyAsAny(),
            response.headers
        );
    }

    /**
     * Unwraps the actual response sent by the server from the response context and deserializes the response content
     * to the expected objects
     *
     * @params response Response returned by the server for a request to register
     * @throws ApiException if the response code was not in [200, 299]
     */
    public async register(response: ResponseContext): Promise<string> {
        const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
        if (isCodeInRange('400', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                'uuid'
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Bad Request', body, response.headers);
        }
        if (isCodeInRange('500', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                'uuid'
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
        }
        if (isCodeInRange('404', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                'uuid'
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Not Found', body, response.headers);
        }
        if (isCodeInRange('200', response.httpStatusCode)) {
            const body: string = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'string',
                'uuid'
            ) as string;
            return body;
        }

        // Work around for missing responses in specification, e.g. for petstore.yaml
        if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
            const body: string = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'string',
                'uuid'
            ) as string;
            return body;
        }

        throw new ApiException<string | Blob | undefined>(
            response.httpStatusCode,
            'Unknown API Status Code!',
            await response.getBodyAsAny(),
            response.headers
        );
    }

    /**
     * Unwraps the actual response sent by the server from the response context and deserializes the response content
     * to the expected objects
     *
     * @params response Response returned by the server for a request to unregister
     * @throws ApiException if the response code was not in [200, 299]
     */
    public async unregister(response: ResponseContext): Promise<Response> {
        const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
        if (isCodeInRange('400', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Bad Request', body, response.headers);
        }
        if (isCodeInRange('500', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
        }
        if (isCodeInRange('404', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Not Found', body, response.headers);
        }
        if (isCodeInRange('200', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            return body;
        }

        // Work around for missing responses in specification, e.g. for petstore.yaml
        if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            return body;
        }

        throw new ApiException<string | Blob | undefined>(
            response.httpStatusCode,
            'Unknown API Status Code!',
            await response.getBodyAsAny(),
            response.headers
        );
    }

    /**
     * Unwraps the actual response sent by the server from the response context and deserializes the response content
     * to the expected objects
     *
     * @params response Response returned by the server for a request to update
     * @throws ApiException if the response code was not in [200, 299]
     */
    public async update(response: ResponseContext): Promise<Response> {
        const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
        if (isCodeInRange('400', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Bad Request', body, response.headers);
        }
        if (isCodeInRange('500', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
        }
        if (isCodeInRange('404', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            throw new ApiException<Response>(response.httpStatusCode, 'Not Found', body, response.headers);
        }
        if (isCodeInRange('200', response.httpStatusCode)) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            return body;
        }

        // Work around for missing responses in specification, e.g. for petstore.yaml
        if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
            const body: Response = ObjectSerializer.deserialize(
                ObjectSerializer.parse(await response.body.text(), contentType),
                'Response',
                ''
            ) as Response;
            return body;
        }

        throw new ApiException<string | Blob | undefined>(
            response.httpStatusCode,
            'Unknown API Status Code!',
            await response.getBodyAsAny(),
            response.headers
        );
    }
}
