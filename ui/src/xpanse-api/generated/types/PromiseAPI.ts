import { Configuration } from '../configuration';

import { ObservableOrchestratorApiApi } from './ObservableAPI';

import { OrchestratorApiApiRequestFactory, OrchestratorApiApiResponseProcessor } from '../apis/OrchestratorApiApi';
import { Ocl } from '../models/Ocl';
import { ServiceStatus } from '../models/ServiceStatus';
import { SystemStatus } from '../models/SystemStatus';
export class PromiseOrchestratorApiApi {
  private api: ObservableOrchestratorApiApi;

  public constructor(
    configuration: Configuration,
    requestFactory?: OrchestratorApiApiRequestFactory,
    responseProcessor?: OrchestratorApiApiResponseProcessor
  ) {
    this.api = new ObservableOrchestratorApiApi(configuration, requestFactory, responseProcessor);
  }

  /**
   * @param ocl
   */
  public fetch(ocl: string, _options?: Configuration): Promise<void> {
    const result = this.api.fetch(ocl, _options);
    return result.toPromise();
  }

  /**
   */
  public health(_options?: Configuration): Promise<SystemStatus> {
    const result = this.api.health(_options);
    return result.toPromise();
  }

  /**
   * @param ocl
   */
  public register(ocl: Ocl, _options?: Configuration): Promise<void> {
    const result = this.api.register(ocl, _options);
    return result.toPromise();
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
  public start(managedServiceName: string, _options?: Configuration): Promise<void> {
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
  public stop(managedServiceName: string, _options?: Configuration): Promise<void> {
    const result = this.api.stop(managedServiceName, _options);
    return result.toPromise();
  }

  /**
   * @param managedServiceName
   * @param ocl
   */
  public update(managedServiceName: string, ocl: Ocl, _options?: Configuration): Promise<void> {
    const result = this.api.update(managedServiceName, ocl, _options);
    return result.toPromise();
  }

  /**
   * @param managedServiceName
   * @param ocl
   */
  public update1(managedServiceName: string, ocl: string, _options?: Configuration): Promise<void> {
    const result = this.api.update1(managedServiceName, ocl, _options);
    return result.toPromise();
  }
}
