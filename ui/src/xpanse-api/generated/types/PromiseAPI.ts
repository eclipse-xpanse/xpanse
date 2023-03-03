/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { Configuration } from '../configuration';
import { Ocl } from '../models/Ocl';
import { Response } from '../models/Response';
import { ServiceStatus } from '../models/ServiceStatus';
import { SystemStatus } from '../models/SystemStatus';
import { ObservableAdminApi, ObservableServiceApi, ObservableServiceVendorApi } from './ObservableAPI';

import { AdminApiRequestFactory, AdminApiResponseProcessor } from '../apis/AdminApi';

import { ServiceApiRequestFactory, ServiceApiResponseProcessor } from '../apis/ServiceApi';

import { ServiceVendorApiRequestFactory, ServiceVendorApiResponseProcessor } from '../apis/ServiceVendorApi';

export class PromiseAdminApi {
  private api: ObservableAdminApi;

  public constructor(
    configuration: Configuration,
    requestFactory?: AdminApiRequestFactory,
    responseProcessor?: AdminApiResponseProcessor
  ) {
    this.api = new ObservableAdminApi(configuration, requestFactory, responseProcessor);
  }

  /**
   */
  public health(_options?: Configuration): Promise<SystemStatus> {
    const result = this.api.health(_options);
    return result.toPromise();
  }


}


export class PromiseServiceApi {
  private api: ObservableServiceApi;

  public constructor(
    configuration: Configuration,
    requestFactory?: ServiceApiRequestFactory,
    responseProcessor?: ServiceApiResponseProcessor
  ) {
    this.api = new ObservableServiceApi(configuration, requestFactory, responseProcessor);
  }

  /**
   */
  public services(_options?: Configuration): Promise<Array<ServiceStatus>> {
    const result = this.api.services(_options);
    return result.toPromise();
  }

  /**
   * @param managedServiceName
   */
  public start(managedServiceName: string, _options?: Configuration): Promise<Response> {
    const result = this.api.start(managedServiceName, _options);
    return result.toPromise();
  }

  /**
   * @param managedServiceName
   */
  public state(managedServiceName: string, _options?: Configuration): Promise<ServiceStatus> {
    const result = this.api.state(managedServiceName, _options);
    return result.toPromise();
  }

  /**
   * @param managedServiceName
   */
  public stop(managedServiceName: string, _options?: Configuration): Promise<Response> {
    const result = this.api.stop(managedServiceName, _options);
    return result.toPromise();
  }


}


export class PromiseServiceVendorApi {
  private api: ObservableServiceVendorApi;

  public constructor(
    configuration: Configuration,
    requestFactory?: ServiceVendorApiRequestFactory,
    responseProcessor?: ServiceVendorApiResponseProcessor
  ) {
    this.api = new ObservableServiceVendorApi(configuration, requestFactory, responseProcessor);
  }

  /**
   * Get registered service using id.
   * @param id id of registered service
   */
  public detail(id: string, _options?: Configuration): Promise<Response> {
    const result = this.api.detail(id, _options);
    return result.toPromise();
  }

  /**
   * Register new service with URL of Ocl file.
   * @param oclLocation URL of Ocl file
   */
  public fetch(oclLocation: string, _options?: Configuration): Promise<Response> {
    const result = this.api.fetch(oclLocation, _options);
    return result.toPromise();
  }

  /**
   * Update registered service using id and ocl file url.
   * @param id id of registered service
   * @param oclLocation URL of Ocl file
   */
  public fetchUpdate(id: string, oclLocation: string, _options?: Configuration): Promise<Response> {
    const result = this.api.fetchUpdate(id, oclLocation, _options);
    return result.toPromise();
  }

  /**
   * List registered service with query params.
   * @param cspName name of the service provider
   * @param serviceName name of the service
   * @param serviceVersion version of the service
   */
  public listRegisteredService(cspName?: string, serviceName?: string, serviceVersion?: string, _options?: Configuration): Promise<Response> {
    const result = this.api.listRegisteredService(cspName, serviceName, serviceVersion, _options);
    return result.toPromise();
  }

  /**
   * Register new service using ocl model.
   * @param ocl
   */
  public register(ocl: Ocl, _options?: Configuration): Promise<Response> {
    const result = this.api.register(ocl, _options);
    return result.toPromise();
  }

  /**
   * Unregister registered service using id.
   * @param id id of registered service
   */
  public unregister(id: string, _options?: Configuration): Promise<Response> {
    const result = this.api.unregister(id, _options);
    return result.toPromise();
  }

  /**
   * Update registered service using id and ocl model.
   * @param id id of registered service
   * @param ocl
   */
  public update(id: string, ocl: Ocl, _options?: Configuration): Promise<Response> {
    const result = this.api.update(id, ocl, _options);
    return result.toPromise();
  }


}



