import { Configuration } from '../configuration';
import { RequestContext, ResponseContext } from '../http/http';
import { Observable, from, map, mergeMap, of } from '../rxjsStub';

import { OrchestratorApiApiRequestFactory, OrchestratorApiApiResponseProcessor } from '../apis/OrchestratorApiApi';
import { Ocl } from '../models/Ocl';
import { ServiceStatus } from '../models/ServiceStatus';
import { SystemStatus } from '../models/SystemStatus';
export class ObservableOrchestratorApiApi {
  private requestFactory: OrchestratorApiApiRequestFactory;
  private responseProcessor: OrchestratorApiApiResponseProcessor;
  private configuration: Configuration;

  public constructor(
    configuration: Configuration,
    requestFactory?: OrchestratorApiApiRequestFactory,
    responseProcessor?: OrchestratorApiApiResponseProcessor
  ) {
    this.configuration = configuration;
    this.requestFactory = requestFactory || new OrchestratorApiApiRequestFactory(configuration);
    this.responseProcessor = responseProcessor || new OrchestratorApiApiResponseProcessor();
  }

  /**
   * @param ocl
   */
  public fetch(ocl: string, _options?: Configuration): Observable<void> {
    const requestContextPromise = this.requestFactory.fetch(ocl, _options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(
      mergeMap((response: ResponseContext) => {
        let middlewarePostObservable = of(response);
        for (let middleware of this.configuration.middleware) {
          middlewarePostObservable = middlewarePostObservable.pipe(
            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
          );
        }
        return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.fetch(rsp)));
      })
    );
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

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(
      mergeMap((response: ResponseContext) => {
        let middlewarePostObservable = of(response);
        for (let middleware of this.configuration.middleware) {
          middlewarePostObservable = middlewarePostObservable.pipe(
            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
          );
        }
        return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.health(rsp)));
      })
    );
  }

  /**
   * @param ocl
   */
  public register(ocl: Ocl, _options?: Configuration): Observable<void> {
    const requestContextPromise = this.requestFactory.register(ocl, _options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(
      mergeMap((response: ResponseContext) => {
        let middlewarePostObservable = of(response);
        for (let middleware of this.configuration.middleware) {
          middlewarePostObservable = middlewarePostObservable.pipe(
            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
          );
        }
        return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.register(rsp)));
      })
    );
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

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(
      mergeMap((response: ResponseContext) => {
        let middlewarePostObservable = of(response);
        for (let middleware of this.configuration.middleware) {
          middlewarePostObservable = middlewarePostObservable.pipe(
            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
          );
        }
        return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.services(rsp)));
      })
    );
  }

  /**
   * @param managedServiceName
   */
  public start(managedServiceName: string, _options?: Configuration): Observable<void> {
    const requestContextPromise = this.requestFactory.start(managedServiceName, _options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(
      mergeMap((response: ResponseContext) => {
        let middlewarePostObservable = of(response);
        for (let middleware of this.configuration.middleware) {
          middlewarePostObservable = middlewarePostObservable.pipe(
            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
          );
        }
        return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.start(rsp)));
      })
    );
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

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(
      mergeMap((response: ResponseContext) => {
        let middlewarePostObservable = of(response);
        for (let middleware of this.configuration.middleware) {
          middlewarePostObservable = middlewarePostObservable.pipe(
            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
          );
        }
        return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.state(rsp)));
      })
    );
  }

  /**
   * @param managedServiceName
   */
  public stop(managedServiceName: string, _options?: Configuration): Observable<void> {
    const requestContextPromise = this.requestFactory.stop(managedServiceName, _options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(
      mergeMap((response: ResponseContext) => {
        let middlewarePostObservable = of(response);
        for (let middleware of this.configuration.middleware) {
          middlewarePostObservable = middlewarePostObservable.pipe(
            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
          );
        }
        return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.stop(rsp)));
      })
    );
  }

  /**
   * @param managedServiceName
   * @param ocl
   */
  public update(managedServiceName: string, ocl: Ocl, _options?: Configuration): Observable<void> {
    const requestContextPromise = this.requestFactory.update(managedServiceName, ocl, _options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(
      mergeMap((response: ResponseContext) => {
        let middlewarePostObservable = of(response);
        for (let middleware of this.configuration.middleware) {
          middlewarePostObservable = middlewarePostObservable.pipe(
            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
          );
        }
        return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.update(rsp)));
      })
    );
  }

  /**
   * @param managedServiceName
   * @param ocl
   */
  public update1(managedServiceName: string, ocl: string, _options?: Configuration): Observable<void> {
    const requestContextPromise = this.requestFactory.update1(managedServiceName, ocl, _options);

    // build promise chain
    let middlewarePreObservable = from<RequestContext>(requestContextPromise);
    for (let middleware of this.configuration.middleware) {
      middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
    }

    return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).pipe(
      mergeMap((response: ResponseContext) => {
        let middlewarePostObservable = of(response);
        for (let middleware of this.configuration.middleware) {
          middlewarePostObservable = middlewarePostObservable.pipe(
            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
          );
        }
        return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.update1(rsp)));
      })
    );
  }
}
