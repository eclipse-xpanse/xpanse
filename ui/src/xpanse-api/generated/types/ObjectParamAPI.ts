/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { ObservableAdminApi, ObservableServiceApi, ObservableServiceVendorApi } from './ObservableAPI';
import { AdminApiRequestFactory, AdminApiResponseProcessor } from '../apis/AdminApi';
import { ServiceApiRequestFactory, ServiceApiResponseProcessor } from '../apis/ServiceApi';
import { ServiceVendorApiRequestFactory, ServiceVendorApiResponseProcessor } from '../apis/ServiceVendorApi';
import { SystemStatus } from '../models/SystemStatus';
import { Configuration } from '../configuration';
import { Oclv2 } from '../models/Oclv2';
import { ServiceStatus } from '../models/ServiceStatus';

export interface AdminApiHealthRequest {
}

export class ObjectAdminApi {
  private api: ObservableAdminApi;

  public constructor(configuration: Configuration, requestFactory?: AdminApiRequestFactory, responseProcessor?: AdminApiResponseProcessor) {
    this.api = new ObservableAdminApi(configuration, requestFactory, responseProcessor);
  }

  /**
   * @param param the request object
   */
  public health(param: AdminApiHealthRequest = {}, options?: Configuration): Promise<SystemStatus> {
    return this.api.health(options).toPromise();
  }

}

export interface ServiceApiServicesRequest {
}

export interface ServiceApiStartRequest {
  /**
   *
   * @type string
   * @memberof ServiceApistart
   */
  managedServiceName: string;
}

export interface ServiceApiStateRequest {
  /**
   *
   * @type string
   * @memberof ServiceApistate
   */
  managedServiceName: string;
}

export interface ServiceApiStopRequest {
  /**
   *
   * @type string
   * @memberof ServiceApistop
   */
  managedServiceName: string;
}

export class ObjectServiceApi {
  private api: ObservableServiceApi;

  public constructor(configuration: Configuration, requestFactory?: ServiceApiRequestFactory, responseProcessor?: ServiceApiResponseProcessor) {
    this.api = new ObservableServiceApi(configuration, requestFactory, responseProcessor);
  }

  /**
   * @param param the request object
   */
  public services(param: ServiceApiServicesRequest = {}, options?: Configuration): Promise<Array<ServiceStatus>> {
    return this.api.services(options).toPromise();
  }

  /**
   * @param param the request object
   */
  public start(param: ServiceApiStartRequest, options?: Configuration): Promise<Response> {
    return this.api.start(param.managedServiceName, options).toPromise();
  }

  /**
   * @param param the request object
   */
  public state(param: ServiceApiStateRequest, options?: Configuration): Promise<ServiceStatus> {
    return this.api.state(param.managedServiceName, options).toPromise();
  }

  /**
   * @param param the request object
   */
  public stop(param: ServiceApiStopRequest, options?: Configuration): Promise<Response> {
    return this.api.stop(param.managedServiceName, options).toPromise();
  }

}

export interface ServiceVendorApiFetchRequest {
  /**
   *
   * @type string
   * @memberof ServiceVendorApifetch
   */
  oclLocation: string;
}

export interface ServiceVendorApiRegisterRequest {
  /**
   *
   * @type Oclv2
   * @memberof ServiceVendorApiregister
   */
  oclv2: Oclv2;
}

export interface ServiceVendorApiUnregisterRequest {
  /**
   *
   * @type string
   * @memberof ServiceVendorApiunregister
   */
  managedServiceName: string;
}

export interface ServiceVendorApiUpdate1Request {
  /**
   *
   * @type string
   * @memberof ServiceVendorApiupdate1
   */
  managedServiceName: string;
  /**
   *
   * @type string
   * @memberof ServiceVendorApiupdate1
   */
  oclLocation: string;
  /**
   *
   * @type Oclv2
   * @memberof ServiceVendorApiupdate1
   */
  oclv2: Oclv2;
}

export class ObjectServiceVendorApi {
  private api: ObservableServiceVendorApi;

  public constructor(configuration: Configuration, requestFactory?: ServiceVendorApiRequestFactory, responseProcessor?: ServiceVendorApiResponseProcessor) {
    this.api = new ObservableServiceVendorApi(configuration, requestFactory, responseProcessor);
  }

  /**
   * @param param the request object
   */
  public fetch(param: ServiceVendorApiFetchRequest, options?: Configuration): Promise<Response> {
    return this.api.fetch(param.oclLocation, options).toPromise();
  }

  /**
   * @param param the request object
   */
  public register(param: ServiceVendorApiRegisterRequest, options?: Configuration): Promise<Response> {
    return this.api.register(param.oclv2, options).toPromise();
  }

  /**
   * @param param the request object
   */
  public unregister(param: ServiceVendorApiUnregisterRequest, options?: Configuration): Promise<Response> {
    return this.api.unregister(param.managedServiceName, options).toPromise();
  }

  /**
   * @param param the request object
   */
  public update1(param: ServiceVendorApiUpdate1Request, options?: Configuration): Promise<Response> {
    return this.api.update1(param.managedServiceName, param.oclLocation, param.oclv2, options).toPromise();
  }

}
