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

export interface ServiceVendorApiDetailRequest {
  /**
   * id of registered service
   * @type string
   * @memberof ServiceVendorApidetail
   */
  id: string;
}

export interface ServiceVendorApiFetchRequest {
  /**
   * URL of Ocl file
   * @type string
   * @memberof ServiceVendorApifetch
   */
  oclLocation: string;
}

export interface ServiceVendorApiFetchUpdateRequest {
  /**
   * id of registered service
   * @type string
   * @memberof ServiceVendorApifetchUpdate
   */
  id: string;
  /**
   * URL of Ocl file
   * @type string
   * @memberof ServiceVendorApifetchUpdate
   */
  oclLocation: string;
}

export interface ServiceVendorApiListRegisteredServiceRequest {
  /**
   * name of the service provider
   * @type string
   * @memberof ServiceVendorApilistRegisteredService
   */
  cspName?: string;
  /**
   * name of the service
   * @type string
   * @memberof ServiceVendorApilistRegisteredService
   */
  serviceName?: string;
  /**
   * version of the service
   * @type string
   * @memberof ServiceVendorApilistRegisteredService
   */
  serviceVersion?: string;
}

export interface ServiceVendorApiRegisterRequest {
  /**
   *
   * @type Ocl
   * @memberof ServiceVendorApiregister
   */
  ocl: Ocl;
}

export interface ServiceVendorApiUnregisterRequest {
  /**
   * id of registered service
   * @type string
   * @memberof ServiceVendorApiunregister
   */
  id: string;
}

export interface ServiceVendorApiUpdateRequest {
  /**
   * id of registered service
   * @type string
   * @memberof ServiceVendorApiupdate
   */
  id: string;
  /**
   *
   * @type Ocl
   * @memberof ServiceVendorApiupdate
   */
  ocl: Ocl;
}

export class ObjectServiceVendorApi {
  private api: ObservableServiceVendorApi;

  public constructor(configuration: Configuration, requestFactory?: ServiceVendorApiRequestFactory, responseProcessor?: ServiceVendorApiResponseProcessor) {
    this.api = new ObservableServiceVendorApi(configuration, requestFactory, responseProcessor);
  }

  /**
   * Get registered service using id.
   * @param param the request object
   */
  public detail(param: ServiceVendorApiDetailRequest, options?: Configuration): Promise<Response> {
    return this.api.detail(param.id, options).toPromise();
  }

  /**
   * Register new service with URL of Ocl file.
   * @param param the request object
   */
  public fetch(param: ServiceVendorApiFetchRequest, options?: Configuration): Promise<Response> {
    return this.api.fetch(param.oclLocation, options).toPromise();
  }

  /**
   * Update registered service using id and ocl file url.
   * @param param the request object
   */
  public fetchUpdate(param: ServiceVendorApiFetchUpdateRequest, options?: Configuration): Promise<Response> {
    return this.api.fetchUpdate(param.id, param.oclLocation, options).toPromise();
  }

  /**
   * List registered service with query params.
   * @param param the request object
   */
  public listRegisteredService(param: ServiceVendorApiListRegisteredServiceRequest = {}, options?: Configuration): Promise<Response> {
    return this.api.listRegisteredService(param.cspName, param.serviceName, param.serviceVersion, options).toPromise();
  }

  /**
   * Register new service using ocl model.
   * @param param the request object
   */
  public register(param: ServiceVendorApiRegisterRequest, options?: Configuration): Promise<Response> {
    return this.api.register(param.ocl, options).toPromise();
  }

  /**
   * Unregister registered service using id.
   * @param param the request object
   */
  public unregister(param: ServiceVendorApiUnregisterRequest, options?: Configuration): Promise<Response> {
    return this.api.unregister(param.id, options).toPromise();
  }

  /**
   * Update registered service using id and ocl model.
   * @param param the request object
   */
  public update(param: ServiceVendorApiUpdateRequest, options?: Configuration): Promise<Response> {
    return this.api.update(param.id, param.ocl, options).toPromise();
  }

}
