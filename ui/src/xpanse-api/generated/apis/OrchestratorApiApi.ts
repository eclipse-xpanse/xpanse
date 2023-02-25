import { SecurityAuthentication } from '../auth/auth';
import { Configuration } from '../configuration';
import { HttpMethod, RequestContext, ResponseContext } from '../http/http';
import { ErrResponse } from '../models/ErrResponse';
import { ObjectSerializer } from '../models/ObjectSerializer';
import { Ocl } from '../models/Ocl';
import { ServiceStatus } from '../models/ServiceStatus';
import { SystemStatus } from '../models/SystemStatus';
import { isCodeInRange } from '../util';
import { BaseAPIRequestFactory, RequiredError } from './baseapi';
import { ApiException } from './exception';

/**
 * no description
 */
export class OrchestratorApiApiRequestFactory extends BaseAPIRequestFactory {
  /**
   * @param ocl
   */
  public async fetch(ocl: string, _options?: Configuration): Promise<RequestContext> {
    let _config = _options || this.configuration;

    // verify required parameter 'ocl' is not null or undefined
    if (ocl === null || ocl === undefined) {
      throw new RequiredError('OrchestratorApiApi', 'fetch', 'ocl');
    }

    // Path Params
    const localVarPath = '/xpanse/register/fetch';

    // Make Request Context
    const requestContext = _config.baseServer.makeRequestContext(localVarPath, HttpMethod.POST);
    requestContext.setHeaderParam('Accept', 'application/json, */*;q=0.8');

    // Header Params
    requestContext.setHeaderParam('ocl', ObjectSerializer.serialize(ocl, 'string', ''));

    const defaultAuth: SecurityAuthentication | undefined =
      _options?.authMethods?.default || this.configuration?.authMethods?.default;
    if (defaultAuth?.applySecurityAuthentication) {
      await defaultAuth?.applySecurityAuthentication(requestContext);
    }

    return requestContext;
  }

  /**
   */
  public async health(_options?: Configuration): Promise<RequestContext> {
    let _config = _options || this.configuration;

    // Path Params
    const localVarPath = '/xpanse/health';

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
   * @param ocl
   */
  public async register(ocl: Ocl, _options?: Configuration): Promise<RequestContext> {
    let _config = _options || this.configuration;

    // verify required parameter 'ocl' is not null or undefined
    if (ocl === null || ocl === undefined) {
      throw new RequiredError('OrchestratorApiApi', 'register', 'ocl');
    }

    // Path Params
    const localVarPath = '/xpanse/register';

    // Make Request Context
    const requestContext = _config.baseServer.makeRequestContext(localVarPath, HttpMethod.POST);
    requestContext.setHeaderParam('Accept', 'application/json, */*;q=0.8');

    // Body Params
    const contentType = ObjectSerializer.getPreferredMediaType(['application/json']);
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
   */
  public async services(_options?: Configuration): Promise<RequestContext> {
    let _config = _options || this.configuration;

    // Path Params
    const localVarPath = '/xpanse/services';

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
   * @param managedServiceName
   */
  public async start(managedServiceName: string, _options?: Configuration): Promise<RequestContext> {
    let _config = _options || this.configuration;

    // verify required parameter 'managedServiceName' is not null or undefined
    if (managedServiceName === null || managedServiceName === undefined) {
      throw new RequiredError('OrchestratorApiApi', 'start', 'managedServiceName');
    }

    // Path Params
    const localVarPath = '/xpanse/start/{managedServiceName}'.replace(
      '{' + 'managedServiceName' + '}',
      encodeURIComponent(String(managedServiceName))
    );

    // Make Request Context
    const requestContext = _config.baseServer.makeRequestContext(localVarPath, HttpMethod.POST);
    requestContext.setHeaderParam('Accept', 'application/json, */*;q=0.8');

    const defaultAuth: SecurityAuthentication | undefined =
      _options?.authMethods?.default || this.configuration?.authMethods?.default;
    if (defaultAuth?.applySecurityAuthentication) {
      await defaultAuth?.applySecurityAuthentication(requestContext);
    }

    return requestContext;
  }

  /**
   * @param managedServiceName
   */
  public async state(managedServiceName: string, _options?: Configuration): Promise<RequestContext> {
    let _config = _options || this.configuration;

    // verify required parameter 'managedServiceName' is not null or undefined
    if (managedServiceName === null || managedServiceName === undefined) {
      throw new RequiredError('OrchestratorApiApi', 'state', 'managedServiceName');
    }

    // Path Params
    const localVarPath = '/xpanse/services/state/{managedServiceName}'.replace(
      '{' + 'managedServiceName' + '}',
      encodeURIComponent(String(managedServiceName))
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
   * @param managedServiceName
   */
  public async stop(managedServiceName: string, _options?: Configuration): Promise<RequestContext> {
    let _config = _options || this.configuration;

    // verify required parameter 'managedServiceName' is not null or undefined
    if (managedServiceName === null || managedServiceName === undefined) {
      throw new RequiredError('OrchestratorApiApi', 'stop', 'managedServiceName');
    }

    // Path Params
    const localVarPath = '/xpanse/stop/{managedServiceName}'.replace(
      '{' + 'managedServiceName' + '}',
      encodeURIComponent(String(managedServiceName))
    );

    // Make Request Context
    const requestContext = _config.baseServer.makeRequestContext(localVarPath, HttpMethod.POST);
    requestContext.setHeaderParam('Accept', 'application/json, */*;q=0.8');

    const defaultAuth: SecurityAuthentication | undefined =
      _options?.authMethods?.default || this.configuration?.authMethods?.default;
    if (defaultAuth?.applySecurityAuthentication) {
      await defaultAuth?.applySecurityAuthentication(requestContext);
    }

    return requestContext;
  }

  /**
   * @param managedServiceName
   * @param ocl
   */
  public async update(managedServiceName: string, ocl: Ocl, _options?: Configuration): Promise<RequestContext> {
    let _config = _options || this.configuration;

    // verify required parameter 'managedServiceName' is not null or undefined
    if (managedServiceName === null || managedServiceName === undefined) {
      throw new RequiredError('OrchestratorApiApi', 'update', 'managedServiceName');
    }

    // verify required parameter 'ocl' is not null or undefined
    if (ocl === null || ocl === undefined) {
      throw new RequiredError('OrchestratorApiApi', 'update', 'ocl');
    }

    // Path Params
    const localVarPath = '/xpanse/update/{managedServiceName}'.replace(
      '{' + 'managedServiceName' + '}',
      encodeURIComponent(String(managedServiceName))
    );

    // Make Request Context
    const requestContext = _config.baseServer.makeRequestContext(localVarPath, HttpMethod.PUT);
    requestContext.setHeaderParam('Accept', 'application/json, */*;q=0.8');

    // Body Params
    const contentType = ObjectSerializer.getPreferredMediaType(['application/json']);
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
   * @param managedServiceName
   * @param ocl
   */
  public async update1(managedServiceName: string, ocl: string, _options?: Configuration): Promise<RequestContext> {
    let _config = _options || this.configuration;

    // verify required parameter 'managedServiceName' is not null or undefined
    if (managedServiceName === null || managedServiceName === undefined) {
      throw new RequiredError('OrchestratorApiApi', 'update1', 'managedServiceName');
    }

    // verify required parameter 'ocl' is not null or undefined
    if (ocl === null || ocl === undefined) {
      throw new RequiredError('OrchestratorApiApi', 'update1', 'ocl');
    }

    // Path Params
    const localVarPath = '/xpanse/update/fetch/{managedServiceName}'.replace(
      '{' + 'managedServiceName' + '}',
      encodeURIComponent(String(managedServiceName))
    );

    // Make Request Context
    const requestContext = _config.baseServer.makeRequestContext(localVarPath, HttpMethod.PUT);
    requestContext.setHeaderParam('Accept', 'application/json, */*;q=0.8');

    // Header Params
    requestContext.setHeaderParam('ocl', ObjectSerializer.serialize(ocl, 'string', ''));

    const defaultAuth: SecurityAuthentication | undefined =
      _options?.authMethods?.default || this.configuration?.authMethods?.default;
    if (defaultAuth?.applySecurityAuthentication) {
      await defaultAuth?.applySecurityAuthentication(requestContext);
    }

    return requestContext;
  }
}

export class OrchestratorApiApiResponseProcessor {
  /**
   * Unwraps the actual response sent by the server from the response context and deserializes the response content
   * to the expected objects
   *
   * @params response Response returned by the server for a request to fetch
   * @throws ApiException if the response code was not in [200, 299]
   */
  public async fetch(response: ResponseContext): Promise<void> {
    const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
    if (isCodeInRange('400', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Bad Request', body, response.headers);
    }
    if (isCodeInRange('404', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Not Found', body, response.headers);
    }
    if (isCodeInRange('500', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
    }
    if (isCodeInRange('200', response.httpStatusCode)) {
      return;
    }

    // Work around for missing responses in specification, e.g. for petstore.yaml
    if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
      const body: void = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'void',
        ''
      ) as void;
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
   * @params response Response returned by the server for a request to health
   * @throws ApiException if the response code was not in [200, 299]
   */
  public async health(response: ResponseContext): Promise<SystemStatus> {
    const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
    if (isCodeInRange('400', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Bad Request', body, response.headers);
    }
    if (isCodeInRange('404', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Not Found', body, response.headers);
    }
    if (isCodeInRange('500', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
    }
    if (isCodeInRange('200', response.httpStatusCode)) {
      const body: SystemStatus = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'SystemStatus',
        ''
      ) as SystemStatus;
      return body;
    }

    // Work around for missing responses in specification, e.g. for petstore.yaml
    if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
      const body: SystemStatus = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'SystemStatus',
        ''
      ) as SystemStatus;
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
  public async register(response: ResponseContext): Promise<void> {
    const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
    if (isCodeInRange('400', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Bad Request', body, response.headers);
    }
    if (isCodeInRange('404', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Not Found', body, response.headers);
    }
    if (isCodeInRange('500', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
    }
    if (isCodeInRange('200', response.httpStatusCode)) {
      return;
    }

    // Work around for missing responses in specification, e.g. for petstore.yaml
    if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
      const body: void = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'void',
        ''
      ) as void;
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
   * @params response Response returned by the server for a request to services
   * @throws ApiException if the response code was not in [200, 299]
   */
  public async services(response: ResponseContext): Promise<Array<ServiceStatus>> {
    const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
    if (isCodeInRange('400', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Bad Request', body, response.headers);
    }
    if (isCodeInRange('404', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Not Found', body, response.headers);
    }
    if (isCodeInRange('500', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
    }
    if (isCodeInRange('200', response.httpStatusCode)) {
      const body: Array<ServiceStatus> = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Array<ServiceStatus>',
        ''
      ) as Array<ServiceStatus>;
      return body;
    }

    // Work around for missing responses in specification, e.g. for petstore.yaml
    if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
      const body: Array<ServiceStatus> = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'Array<ServiceStatus>',
        ''
      ) as Array<ServiceStatus>;
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
   * @params response Response returned by the server for a request to start
   * @throws ApiException if the response code was not in [200, 299]
   */
  public async start(response: ResponseContext): Promise<void> {
    const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
    if (isCodeInRange('400', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Bad Request', body, response.headers);
    }
    if (isCodeInRange('404', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Not Found', body, response.headers);
    }
    if (isCodeInRange('500', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
    }
    if (isCodeInRange('200', response.httpStatusCode)) {
      return;
    }

    // Work around for missing responses in specification, e.g. for petstore.yaml
    if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
      const body: void = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'void',
        ''
      ) as void;
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
   * @params response Response returned by the server for a request to state
   * @throws ApiException if the response code was not in [200, 299]
   */
  public async state(response: ResponseContext): Promise<ServiceStatus> {
    const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
    if (isCodeInRange('400', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Bad Request', body, response.headers);
    }
    if (isCodeInRange('404', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Not Found', body, response.headers);
    }
    if (isCodeInRange('500', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
    }
    if (isCodeInRange('200', response.httpStatusCode)) {
      const body: ServiceStatus = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ServiceStatus',
        ''
      ) as ServiceStatus;
      return body;
    }

    // Work around for missing responses in specification, e.g. for petstore.yaml
    if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
      const body: ServiceStatus = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ServiceStatus',
        ''
      ) as ServiceStatus;
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
   * @params response Response returned by the server for a request to stop
   * @throws ApiException if the response code was not in [200, 299]
   */
  public async stop(response: ResponseContext): Promise<void> {
    const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
    if (isCodeInRange('400', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Bad Request', body, response.headers);
    }
    if (isCodeInRange('404', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Not Found', body, response.headers);
    }
    if (isCodeInRange('500', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
    }
    if (isCodeInRange('200', response.httpStatusCode)) {
      return;
    }

    // Work around for missing responses in specification, e.g. for petstore.yaml
    if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
      const body: void = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'void',
        ''
      ) as void;
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
  public async update(response: ResponseContext): Promise<void> {
    const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
    if (isCodeInRange('400', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Bad Request', body, response.headers);
    }
    if (isCodeInRange('404', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Not Found', body, response.headers);
    }
    if (isCodeInRange('500', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
    }
    if (isCodeInRange('200', response.httpStatusCode)) {
      return;
    }

    // Work around for missing responses in specification, e.g. for petstore.yaml
    if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
      const body: void = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'void',
        ''
      ) as void;
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
   * @params response Response returned by the server for a request to update1
   * @throws ApiException if the response code was not in [200, 299]
   */
  public async update1(response: ResponseContext): Promise<void> {
    const contentType = ObjectSerializer.normalizeMediaType(response.headers['content-type']);
    if (isCodeInRange('400', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Bad Request', body, response.headers);
    }
    if (isCodeInRange('404', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Not Found', body, response.headers);
    }
    if (isCodeInRange('500', response.httpStatusCode)) {
      const body: ErrResponse = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'ErrResponse',
        ''
      ) as ErrResponse;
      throw new ApiException<ErrResponse>(response.httpStatusCode, 'Internal Server Error', body, response.headers);
    }
    if (isCodeInRange('200', response.httpStatusCode)) {
      return;
    }

    // Work around for missing responses in specification, e.g. for petstore.yaml
    if (response.httpStatusCode >= 200 && response.httpStatusCode <= 299) {
      const body: void = ObjectSerializer.deserialize(
        ObjectSerializer.parse(await response.body.text(), contentType),
        'void',
        ''
      ) as void;
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
