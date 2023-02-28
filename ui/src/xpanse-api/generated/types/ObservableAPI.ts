/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { RequestContext, ResponseContext } from '../http/http';
import { Configuration } from '../configuration';
import { from, map, mergeMap, Observable, of } from '../rxjsStub';
import { ServiceApiRequestFactory, ServiceApiResponseProcessor } from '../apis/ServiceApi';
import { ServiceVendorApiRequestFactory, ServiceVendorApiResponseProcessor } from '../apis/ServiceVendorApi';
import { SystemStatus } from '../models/SystemStatus';
import { ServiceStatus } from '../models/ServiceStatus';
import { Oclv2 } from '../models/Oclv2';

import { AdminApiRequestFactory, AdminApiResponseProcessor } from '../apis/AdminApi';

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
   * @param oclLocation
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
   * @param oclv2
   */
  public register(oclv2: Oclv2, _options?: Configuration): Observable<Response> {
    const requestContextPromise = this.requestFactory.register(oclv2, _options);

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
   * @param managedServiceName
   */
  public unregister(managedServiceName: string, _options?: Configuration): Observable<Response> {
    const requestContextPromise = this.requestFactory.unregister(managedServiceName, _options);

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
   * @param managedServiceName
   * @param oclLocation
   * @param oclv2
   */
  public update1(managedServiceName: string, oclLocation: string, oclv2: Oclv2, _options?: Configuration): Observable<Response> {
    const requestContextPromise = this.requestFactory.update1(managedServiceName, oclLocation, oclv2, _options);

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
      return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.update1(rsp)));
    }));
  }

}
