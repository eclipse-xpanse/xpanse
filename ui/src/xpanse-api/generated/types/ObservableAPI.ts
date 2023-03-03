/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { RequestContext, ResponseContext } from '../http/http';
import { Configuration } from '../configuration';
import { from, map, mergeMap, Observable, of } from '../rxjsStub';
import { Ocl } from '../models/Ocl';
import { Response } from '../models/Response';
import { ServiceStatus } from '../models/ServiceStatus';
import { SystemStatus } from '../models/SystemStatus';

import { AdminApiRequestFactory, AdminApiResponseProcessor } from '../apis/AdminApi';
import { ServiceApiRequestFactory, ServiceApiResponseProcessor } from '../apis/ServiceApi';
import { ServiceVendorApiRequestFactory, ServiceVendorApiResponseProcessor } from '../apis/ServiceVendorApi';

export class ObservableAdminApi {
  private requestFactory: AdminApiRequestFactory;
  private responseProcessor: AdminApiResponseProcessor;
  private configuration: Configuration;

  public constructor(
    configuration: Configuration,
    requestFactory?: AdminApiRequestFactory,
    responseProcessor?: AdminApiResponseProcessor
  ) {
    this.configuration = configuration;
    this.requestFactory = requestFactory || new AdminApiRequestFactory(configuration);
    this.responseProcessor = responseProcessor || new AdminApiResponseProcessor();
  }

  /**
   */
  public health(_options?: Configuration): Observable<SystemStatus> {
    const requestContextPromise = this.requestFactory.health(_options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(mergeMap((response: ResponseContext) => {
      let middlewarePostObservable = of(response);
      for (let middleware of this.configuration.middleware) {
        middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
      }
      return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.health(rsp)));
    }));
  }

}

export class ObservableServiceApi {
  private requestFactory: ServiceApiRequestFactory;
  private responseProcessor: ServiceApiResponseProcessor;
  private configuration: Configuration;

  public constructor(
    configuration: Configuration,
    requestFactory?: ServiceApiRequestFactory,
    responseProcessor?: ServiceApiResponseProcessor
  ) {
    this.configuration = configuration;
    this.requestFactory = requestFactory || new ServiceApiRequestFactory(configuration);
    this.responseProcessor = responseProcessor || new ServiceApiResponseProcessor();
  }

  /**
   */
  public services(_options?: Configuration): Observable<Array<ServiceStatus>> {
    const requestContextPromise = this.requestFactory.services(_options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(mergeMap((response: ResponseContext) => {
      let middlewarePostObservable = of(response);
      for (let middleware of this.configuration.middleware) {
        middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
      }
      return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.services(rsp)));
    }));
  }

  /**
   * @param managedServiceName
   */
  public start(managedServiceName: string, _options?: Configuration): Observable<Response> {
    const requestContextPromise = this.requestFactory.start(managedServiceName, _options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(mergeMap((response: ResponseContext) => {
      let middlewarePostObservable = of(response);
      for (let middleware of this.configuration.middleware) {
        middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
      }
      return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.start(rsp)));
    }));
  }

  /**
   * @param managedServiceName
   */
  public state(managedServiceName: string, _options?: Configuration): Observable<ServiceStatus> {
    const requestContextPromise = this.requestFactory.state(managedServiceName, _options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(mergeMap((response: ResponseContext) => {
      let middlewarePostObservable = of(response);
      for (let middleware of this.configuration.middleware) {
        middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
      }
      return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.state(rsp)));
    }));
  }

  /**
   * @param managedServiceName
   */
  public stop(managedServiceName: string, _options?: Configuration): Observable<Response> {
    const requestContextPromise = this.requestFactory.stop(managedServiceName, _options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(mergeMap((response: ResponseContext) => {
      let middlewarePostObservable = of(response);
      for (let middleware of this.configuration.middleware) {
        middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
      }
      return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.stop(rsp)));
    }));
  }

}

export class ObservableServiceVendorApi {
  private requestFactory: ServiceVendorApiRequestFactory;
  private responseProcessor: ServiceVendorApiResponseProcessor;
  private configuration: Configuration;

  public constructor(
    configuration: Configuration,
    requestFactory?: ServiceVendorApiRequestFactory,
    responseProcessor?: ServiceVendorApiResponseProcessor
  ) {
    this.configuration = configuration;
    this.requestFactory = requestFactory || new ServiceVendorApiRequestFactory(configuration);
    this.responseProcessor = responseProcessor || new ServiceVendorApiResponseProcessor();
  }

  /**
   * Get registered service using id.
   * @param id id of registered service
   */
  public detail(id: string, _options?: Configuration): Observable<Response> {
    const requestContextPromise = this.requestFactory.detail(id, _options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(mergeMap((response: ResponseContext) => {
      let middlewarePostObservable = of(response);
      for (let middleware of this.configuration.middleware) {
        middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
      }
      return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.detail(rsp)));
    }));
  }

  /**
   * Register new service with URL of Ocl file.
   * @param oclLocation URL of Ocl file
   */
  public fetch(oclLocation: string, _options?: Configuration): Observable<Response> {
    const requestContextPromise = this.requestFactory.fetch(oclLocation, _options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(mergeMap((response: ResponseContext) => {
      let middlewarePostObservable = of(response);
      for (let middleware of this.configuration.middleware) {
        middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
      }
      return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.fetch(rsp)));
    }));
  }

  /**
   * Update registered service using id and ocl file url.
   * @param id id of registered service
   * @param oclLocation URL of Ocl file
   */
  public fetchUpdate(id: string, oclLocation: string, _options?: Configuration): Observable<Response> {
    const requestContextPromise = this.requestFactory.fetchUpdate(id, oclLocation, _options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(mergeMap((response: ResponseContext) => {
      let middlewarePostObservable = of(response);
      for (let middleware of this.configuration.middleware) {
        middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
      }
      return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.fetchUpdate(rsp)));
    }));
  }

  /**
   * List registered service with query params.
   * @param cspName name of the service provider
   * @param serviceName name of the service
   * @param serviceVersion version of the service
   */
  public listRegisteredService(cspName?: string, serviceName?: string, serviceVersion?: string, _options?: Configuration): Observable<Response> {
    const requestContextPromise = this.requestFactory.listRegisteredService(cspName, serviceName, serviceVersion, _options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(mergeMap((response: ResponseContext) => {
      let middlewarePostObservable = of(response);
      for (let middleware of this.configuration.middleware) {
        middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
      }
      return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.listRegisteredService(rsp)));
    }));
  }

  /**
   * Register new service using ocl model.
   * @param ocl
   */
  public register(ocl: Ocl, _options?: Configuration): Observable<Response> {
    const requestContextPromise = this.requestFactory.register(ocl, _options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(mergeMap((response: ResponseContext) => {
      let middlewarePostObservable = of(response);
      for (let middleware of this.configuration.middleware) {
        middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
      }
      return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.register(rsp)));
    }));
  }

  /**
   * Unregister registered service using id.
   * @param id id of registered service
   */
  public unregister(id: string, _options?: Configuration): Observable<Response> {
    const requestContextPromise = this.requestFactory.unregister(id, _options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(mergeMap((response: ResponseContext) => {
      let middlewarePostObservable = of(response);
      for (let middleware of this.configuration.middleware) {
        middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
      }
      return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.unregister(rsp)));
    }));
  }

  /**
   * Update registered service using id and ocl model.
   * @param id id of registered service
   * @param ocl
   */
  public update(id: string, ocl: Ocl, _options?: Configuration): Observable<Response> {
    const requestContextPromise = this.requestFactory.update(id, ocl, _options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(mergeMap((response: ResponseContext) => {
      let middlewarePostObservable = of(response);
      for (let middleware of this.configuration.middleware) {
        middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
      }
      return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.update(rsp)));
    }));
  }

}
