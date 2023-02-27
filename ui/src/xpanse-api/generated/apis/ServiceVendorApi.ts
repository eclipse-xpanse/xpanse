/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { BaseAPIRequestFactory, RequiredError } from './baseapi';
import { Configuration } from '../configuration';
import { HttpMethod, RequestContext, ResponseContext } from '../http/http';
import { ObjectSerializer } from '../models/ObjectSerializer';
import { ApiException } from './exception';
import { isCodeInRange } from '../util';
import { SecurityAuthentication } from '../auth/auth';
import { Oclv2 } from '../models/Oclv2';


/**
 * no description
 */
export class ServiceVendorApiRequestFactory extends BaseAPIRequestFactory {

  /**
   * @param oclLocation
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


    const defaultAuth: SecurityAuthentication | undefined = _options?.authMethods?.default || this.configuration?.authMethods?.default;
    if (defaultAuth?.applySecurityAuthentication) {
      await defaultAuth?.applySecurityAuthentication(requestContext);
    }

    return requestContext;
  }

  /**
   * @param oclv2
   */
  public async register(oclv2: Oclv2, _options?: Configuration): Promise<RequestContext> {
    let _config = _options || this.configuration;

    // verify required parameter 'oclv2' is not null or undefined
    if (oclv2 === null || oclv2 === undefined) {
      throw new RequiredError('ServiceVendorApi', 'register', 'oclv2');
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

      'application/yaml'
    ]);
    requestContext.setHeaderParam('Content-Type', contentType);
    const serializedBody = ObjectSerializer.stringify(
      ObjectSerializer.serialize(oclv2, 'Oclv2', ''),
      contentType
    );
    requestContext.setBody(serializedBody);


    const defaultAuth: SecurityAuthentication | undefined = _options?.authMethods?.default || this.configuration?.authMethods?.default;
    if (defaultAuth?.applySecurityAuthentication) {
      await defaultAuth?.applySecurityAuthentication(requestContext);
    }

    return requestContext;
  }

  /**
   * @param managedServiceName
   */
  public async unregister(managedServiceName: string, _options?: Configuration): Promise<RequestContext> {
    let _config = _options || this.configuration;

    // verify required parameter 'managedServiceName' is not null or undefined
    if (managedServiceName === null || managedServiceName === undefined) {
      throw new RequiredError('ServiceVendorApi', 'unregister', 'managedServiceName');
    }


    // Path Params
    const localVarPath = '/xpanse/register/{managedServiceName}'
      .replace('{' + 'managedServiceName' + '}', encodeURIComponent(String(managedServiceName)));

    // Make Request Context
    const requestContext = _config.baseServer.makeRequestContext(localVarPath, HttpMethod.DELETE);
    requestContext.setHeaderParam('Accept', 'application/json, */*;q=0.8');


    const defaultAuth: SecurityAuthentication | undefined = _options?.authMethods?.default || this.configuration?.authMethods?.default;
    if (defaultAuth?.applySecurityAuthentication) {
      await defaultAuth?.applySecurityAuthentication(requestContext);
    }

    return requestContext;
  }

  /**
   * @param managedServiceName
   * @param oclLocation
   * @param oclv2
   */
  public async update1(managedServiceName: string, oclLocation: string, oclv2: Oclv2, _options?: Configuration): Promise<RequestContext> {
    let _config = _options || this.configuration;

    // verify required parameter 'managedServiceName' is not null or undefined
    if (managedServiceName === null || managedServiceName === undefined) {
      throw new RequiredError('ServiceVendorApi', 'update1', 'managedServiceName');
    }


    // verify required parameter 'oclLocation' is not null or undefined
    if (oclLocation === null || oclLocation === undefined) {
      throw new RequiredError('ServiceVendorApi', 'update1', 'oclLocation');
    }


    // verify required parameter 'oclv2' is not null or undefined
    if (oclv2 === null || oclv2 === undefined) {
      throw new RequiredError('ServiceVendorApi', 'update1', 'oclv2');
    }


    // Path Params
    const localVarPath = '/xpanse/register/{managedServiceName}'
      .replace('{' + 'managedServiceName' + '}', encodeURIComponent(String(managedServiceName)));

    // Make Request Context
    const requestContext = _config.baseServer.makeRequestContext(localVarPath, HttpMethod.PUT);
    requestContext.setHeaderParam('Accept', 'application/json, */*;q=0.8');

    // Query Params
    if (oclLocation !== undefined) {
      requestContext.setQueryParam('oclLocation', ObjectSerializer.serialize(oclLocation, 'string', ''));
    }


    // Body Params
    const contentType = ObjectSerializer.getPreferredMediaType([
      'application/x-yaml',

      'application/yml',

      'application/yaml'
    ]);
    requestContext.setHeaderParam('Content-Type', contentType);
    const serializedBody = ObjectSerializer.stringify(
      ObjectSerializer.serialize(oclv2, 'Oclv2', ''),
      contentType
    );
    requestContext.setBody(serializedBody);


    const defaultAuth: SecurityAuthentication | undefined = _options?.authMethods?.default || this.configuration?.authMethods?.default;
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
   * @params response Response returned by the server for a request to fetch
   * @throws ApiException if the response code was not in [200, 299]
   */
  public async fetch(response: ResponseContext): Promise<Response> {
    const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
    if (isCodeInRange('404', response.httpStatusCode)) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      throw new ApiException<Response>(response.httpStatusCode, 'Not Found', body, response.headers);
    }
    if (isCodeInRange('400', response.httpStatusCode)) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      throw new ApiException<Response>(response.httpStatusCode, 'Bad Request', body, response.headers);
    }
    if (isCodeInRange('500', response.httpStatusCode)) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      throw new ApiException<Response>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
    }
    if (isCodeInRange('200', response.httpStatusCode)) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      return body;
    }

    // Work around for missing responses in specification, e.g. for petstore.yaml
    if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      return body;
    }

    throw new ApiException<string | Blob | undefined>(response.httpStatusCode, 'Unknown API Status Code!', await response.getBodyAsAny(), response.headers);
  }

  /**
   * Unwraps the actual response sent by the server from the response context and deserializes the response content
   * to the expected objects
   *
   * @params response Response returned by the server for a request to register
   * @throws ApiException if the response code was not in [200, 299]
   */
  public async register(response: ResponseContext): Promise<Response> {
    const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
    if (isCodeInRange('404', response.httpStatusCode)) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      throw new ApiException<Response>(response.httpStatusCode, 'Not Found', body, response.headers);
    }
    if (isCodeInRange('400', response.httpStatusCode)) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      throw new ApiException<Response>(response.httpStatusCode, 'Bad Request', body, response.headers);
    }
    if (isCodeInRange('500', response.httpStatusCode)) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      throw new ApiException<Response>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
    }
    if (isCodeInRange('200', response.httpStatusCode)) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      return body;
    }

    // Work around for missing responses in specification, e.g. for petstore.yaml
    if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      return body;
    }

    throw new ApiException<string | Blob | undefined>(response.httpStatusCode, 'Unknown API Status Code!', await response.getBodyAsAny(), response.headers);
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
    if (isCodeInRange('404', response.httpStatusCode)) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      throw new ApiException<Response>(response.httpStatusCode, 'Not Found', body, response.headers);
    }
    if (isCodeInRange('400', response.httpStatusCode)) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      throw new ApiException<Response>(response.httpStatusCode, 'Bad Request', body, response.headers);
    }
    if (isCodeInRange('500', response.httpStatusCode)) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      throw new ApiException<Response>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
    }
    if (isCodeInRange('200', response.httpStatusCode)) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      return body;
    }

    // Work around for missing responses in specification, e.g. for petstore.yaml
    if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      return body;
    }

    throw new ApiException<string | Blob | undefined>(response.httpStatusCode, 'Unknown API Status Code!', await response.getBodyAsAny(), response.headers);
  }

  /**
   * Unwraps the actual response sent by the server from the response context and deserializes the response content
   * to the expected objects
   *
   * @params response Response returned by the server for a request to update1
   * @throws ApiException if the response code was not in [200, 299]
   */
  public async update1(response: ResponseContext): Promise<Response> {
    const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
    if (isCodeInRange('404', response.httpStatusCode)) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      throw new ApiException<Response>(response.httpStatusCode, 'Not Found', body, response.headers);
    }
    if (isCodeInRange('400', response.httpStatusCode)) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      throw new ApiException<Response>(response.httpStatusCode, 'Bad Request', body, response.headers);
    }
    if (isCodeInRange('500', response.httpStatusCode)) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      throw new ApiException<Response>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
    }
    if (isCodeInRange('200', response.httpStatusCode)) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      return body;
    }

    // Work around for missing responses in specification, e.g. for petstore.yaml
    if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
      const body: Response = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Response', ''
      ) as Response;
      return body;
    }

    throw new ApiException<string | Blob | undefined>(response.httpStatusCode, 'Unknown API Status Code!', await response.getBodyAsAny(), response.headers);
  }

}
