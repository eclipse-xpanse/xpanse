import { Configuration } from '../configuration';

import { OrchestratorApiApiRequestFactory, OrchestratorApiApiResponseProcessor } from '../apis/OrchestratorApiApi';
import { Ocl } from '../models/Ocl';
import { ServiceStatus } from '../models/ServiceStatus';
import { SystemStatus } from '../models/SystemStatus';
import { ObservableOrchestratorApiApi } from './ObservableAPI';

export interface OrchestratorApiApiFetchRequest {
  /**
   *
   * @type string
   * @memberof OrchestratorApiApifetch
   */
  ocl: string;
}

export interface OrchestratorApiApiHealthRequest {}

export interface OrchestratorApiApiRegisterRequest {
  /**
   *
   * @type Ocl
   * @memberof OrchestratorApiApiregister
   */
  ocl: Ocl;
}

export interface OrchestratorApiApiServicesRequest {}

export interface OrchestratorApiApiStartRequest {
  /**
   *
   * @type string
   * @memberof OrchestratorApiApistart
   */
  managedServiceName: string;
}

export interface OrchestratorApiApiStateRequest {
  /**
   *
   * @type string
   * @memberof OrchestratorApiApistate
   */
  managedServiceName: string;
}

export interface OrchestratorApiApiStopRequest {
  /**
   *
   * @type string
   * @memberof OrchestratorApiApistop
   */
  managedServiceName: string;
}

export interface OrchestratorApiApiUpdateRequest {
  /**
   *
   * @type string
   * @memberof OrchestratorApiApiupdate
   */
  managedServiceName: string;
  /**
   *
   * @type Ocl
   * @memberof OrchestratorApiApiupdate
   */
  ocl: Ocl;
}

export interface OrchestratorApiApiUpdate1Request {
  /**
   *
   * @type string
   * @memberof OrchestratorApiApiupdate1
   */
  managedServiceName: string;
  /**
   *
   * @type string
   * @memberof OrchestratorApiApiupdate1
   */
  ocl: string;
}

export class ObjectOrchestratorApiApi {
  private api: ObservableOrchestratorApiApi;

  public constructor(
    configuration: Configuration,
    requestFactory?: OrchestratorApiApiRequestFactory,
    responseProcessor?: OrchestratorApiApiResponseProcessor
  ) {
    this.api = new ObservableOrchestratorApiApi(configuration, requestFactory, responseProcessor);
  }

  /**
   * @param param the request object
   */
  public fetch(param: OrchestratorApiApiFetchRequest, options?: Configuration): Promise<void> {
    return this.api.fetch(param.ocl, options).toPromise();
  }

  /**
   * @param param the request object
   */
  public health(param: OrchestratorApiApiHealthRequest = {}, options?: Configuration): Promise<SystemStatus> {
    return this.api.health(options).toPromise();
  }

  /**
   * @param param the request object
   */
  public register(param: OrchestratorApiApiRegisterRequest, options?: Configuration): Promise<void> {
    return this.api.register(param.ocl, options).toPromise();
  }

  /**
   * @param param the request object
   */
  public services(
    param: OrchestratorApiApiServicesRequest = {},
    options?: Configuration
  ): Promise<Array<ServiceStatus>> {
    return this.api.services(options).toPromise();
  }

  /**
   * @param param the request object
   */
  public start(param: OrchestratorApiApiStartRequest, options?: Configuration): Promise<void> {
    return this.api.start(param.managedServiceName, options).toPromise();
  }

  /**
   * @param param the request object
   */
  public state(param: OrchestratorApiApiStateRequest, options?: Configuration): Promise<ServiceStatus> {
    return this.api.state(param.managedServiceName, options).toPromise();
  }

  /**
   * @param param the request object
   */
  public stop(param: OrchestratorApiApiStopRequest, options?: Configuration): Promise<void> {
    return this.api.stop(param.managedServiceName, options).toPromise();
  }

  /**
   * @param param the request object
   */
  public update(param: OrchestratorApiApiUpdateRequest, options?: Configuration): Promise<void> {
    return this.api.update(param.managedServiceName, param.ocl, options).toPromise();
  }

  /**
   * @param param the request object
   */
  public update1(param: OrchestratorApiApiUpdate1Request, options?: Configuration): Promise<void> {
    return this.api.update1(param.managedServiceName, param.ocl, options).toPromise();
  }
}
