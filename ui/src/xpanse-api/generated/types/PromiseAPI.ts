/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { Configuration } from '../configuration';

import { ObservableAdminApi, ObservableServiceApi, ObservableServiceVendorApi } from './ObservableAPI';

import { AdminApiRequestFactory, AdminApiResponseProcessor } from '../apis/AdminApi';

import { ServiceApiRequestFactory, ServiceApiResponseProcessor } from '../apis/ServiceApi';

import { ServiceVendorApiRequestFactory, ServiceVendorApiResponseProcessor } from '../apis/ServiceVendorApi';
import { SystemStatus } from '../models/SystemStatus';
import { ServiceStatus } from '../models/ServiceStatus';
import { Oclv2 } from '../models/Oclv2';

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
   * @param oclLocation
   */
  public fetch(oclLocation: string, _options?: Configuration): Promise<Response> {
    const result = this.api.fetch(oclLocation, _options);
    return result.toPromise();
  }

  /**
   * @param oclv2
   */
  public register(oclv2: Oclv2, _options?: Configuration): Promise<Response> {
    const result = this.api.register(oclv2, _options);
    return result.toPromise();
  }

  /**
   * @param managedServiceName
   */
  public unregister(managedServiceName: string, _options?: Configuration): Promise<Response> {
    const result = this.api.unregister(managedServiceName, _options);
    return result.toPromise();
  }

  /**
   * @param managedServiceName
   * @param oclLocation
   * @param oclv2
   */
  public update1(managedServiceName: string, oclLocation: string, oclv2: Oclv2, _options?: Configuration): Promise<Response> {
    const result = this.api.update1(managedServiceName, oclLocation, oclv2, _options);
    return result.toPromise();
  }


}



