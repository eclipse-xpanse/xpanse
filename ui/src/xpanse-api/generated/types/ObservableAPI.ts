/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

import { RequestContext, ResponseContext } from '../http/http';
import { Configuration } from '../configuration';
import { from, map, mergeMap, Observable, of } from '../rxjsStub';
import { CategoryOclVo } from '../models/CategoryOclVo';
import { CreateRequest } from '../models/CreateRequest';
import { DeployServiceEntity } from '../models/DeployServiceEntity';
import { Ocl } from '../models/Ocl';
import { RegisterServiceEntity } from '../models/RegisterServiceEntity';
import { Response } from '../models/Response';
import { ServiceVo } from '../models/ServiceVo';
import { SystemStatus } from '../models/SystemStatus';

import { AdminApiRequestFactory, AdminApiResponseProcessor } from '../apis/AdminApi';
import { ServiceApiRequestFactory, ServiceApiResponseProcessor } from '../apis/ServiceApi';
import { ServiceVendorApiRequestFactory, ServiceVendorApiResponseProcessor } from '../apis/ServiceVendorApi';
import { OclDetailVo } from '../models/OclDetailVo';

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
            middlewarePreObservable = middlewarePreObservable.pipe(
                mergeMap((ctx: RequestContext) => middleware.pre(ctx))
            );
        }

        return middlewarePreObservable
            .pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx)))
            .pipe(
                mergeMap((response: ResponseContext) => {
                    let middlewarePostObservable = of(response);
                    for (let middleware of this.configuration.middleware) {
                        middlewarePostObservable = middlewarePostObservable.pipe(
                            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
                        );
                    }
                    return middlewarePostObservable.pipe(
                        map((rsp: ResponseContext) => this.responseProcessor.health(rsp))
                    );
                })
            );
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
     * @param id
     */
    public serviceDetail(id: string, _options?: Configuration): Observable<DeployServiceEntity> {
        const requestContextPromise = this.requestFactory.serviceDetail(id, _options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(
                mergeMap((ctx: RequestContext) => middleware.pre(ctx))
            );
        }

        return middlewarePreObservable
            .pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx)))
            .pipe(
                mergeMap((response: ResponseContext) => {
                    let middlewarePostObservable = of(response);
                    for (let middleware of this.configuration.middleware) {
                        middlewarePostObservable = middlewarePostObservable.pipe(
                            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
                        );
                    }
                    return middlewarePostObservable.pipe(
                        map((rsp: ResponseContext) => this.responseProcessor.serviceDetail(rsp))
                    );
                })
            );
    }

    /**
     */
    public services(_options?: Configuration): Observable<Array<ServiceVo>> {
        const requestContextPromise = this.requestFactory.services(_options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(
                mergeMap((ctx: RequestContext) => middleware.pre(ctx))
            );
        }

        return middlewarePreObservable
            .pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx)))
            .pipe(
                mergeMap((response: ResponseContext) => {
                    let middlewarePostObservable = of(response);
                    for (let middleware of this.configuration.middleware) {
                        middlewarePostObservable = middlewarePostObservable.pipe(
                            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
                        );
                    }
                    return middlewarePostObservable.pipe(
                        map((rsp: ResponseContext) => this.responseProcessor.services(rsp))
                    );
                })
            );
    }

    /**
     * @param createRequest
     */
    public start(createRequest: CreateRequest, _options?: Configuration): Observable<string> {
        const requestContextPromise = this.requestFactory.start(createRequest, _options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(
                mergeMap((ctx: RequestContext) => middleware.pre(ctx))
            );
        }

        return middlewarePreObservable
            .pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx)))
            .pipe(
                mergeMap((response: ResponseContext) => {
                    let middlewarePostObservable = of(response);
                    for (let middleware of this.configuration.middleware) {
                        middlewarePostObservable = middlewarePostObservable.pipe(
                            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
                        );
                    }
                    return middlewarePostObservable.pipe(
                        map((rsp: ResponseContext) => this.responseProcessor.start(rsp))
                    );
                })
            );
    }

    /**
     * @param id
     */
    public stop(id: string, _options?: Configuration): Observable<Response> {
        const requestContextPromise = this.requestFactory.stop(id, _options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(
                mergeMap((ctx: RequestContext) => middleware.pre(ctx))
            );
        }

        return middlewarePreObservable
            .pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx)))
            .pipe(
                mergeMap((response: ResponseContext) => {
                    let middlewarePostObservable = of(response);
                    for (let middleware of this.configuration.middleware) {
                        middlewarePostObservable = middlewarePostObservable.pipe(
                            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
                        );
                    }
                    return middlewarePostObservable.pipe(
                        map((rsp: ResponseContext) => this.responseProcessor.stop(rsp))
                    );
                })
            );
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
    public detail(id: string, _options?: Configuration): Observable<OclDetailVo> {
        const requestContextPromise = this.requestFactory.detail(id, _options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(
                mergeMap((ctx: RequestContext) => middleware.pre(ctx))
            );
        }

        return middlewarePreObservable
            .pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx)))
            .pipe(
                mergeMap((response: ResponseContext) => {
                    let middlewarePostObservable = of(response);
                    for (let middleware of this.configuration.middleware) {
                        middlewarePostObservable = middlewarePostObservable.pipe(
                            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
                        );
                    }
                    return middlewarePostObservable.pipe(
                        map((rsp: ResponseContext) => this.responseProcessor.detail(rsp))
                    );
                })
            );
    }

    /**
     * Register new service with URL of Ocl file.
     * @param oclLocation URL of Ocl file
     */
    public fetch(oclLocation: string, _options?: Configuration): Observable<string> {
        const requestContextPromise = this.requestFactory.fetch(oclLocation, _options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(
                mergeMap((ctx: RequestContext) => middleware.pre(ctx))
            );
        }

        return middlewarePreObservable
            .pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx)))
            .pipe(
                mergeMap((response: ResponseContext) => {
                    let middlewarePostObservable = of(response);
                    for (let middleware of this.configuration.middleware) {
                        middlewarePostObservable = middlewarePostObservable.pipe(
                            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
                        );
                    }
                    return middlewarePostObservable.pipe(
                        map((rsp: ResponseContext) => this.responseProcessor.fetch(rsp))
                    );
                })
            );
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
            middlewarePreObservable = middlewarePreObservable.pipe(
                mergeMap((ctx: RequestContext) => middleware.pre(ctx))
            );
        }

        return middlewarePreObservable
            .pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx)))
            .pipe(
                mergeMap((response: ResponseContext) => {
                    let middlewarePostObservable = of(response);
                    for (let middleware of this.configuration.middleware) {
                        middlewarePostObservable = middlewarePostObservable.pipe(
                            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
                        );
                    }
                    return middlewarePostObservable.pipe(
                        map((rsp: ResponseContext) => this.responseProcessor.fetchUpdate(rsp))
                    );
                })
            );
    }

    /**
     * Get category list.
     */
    public listCategories(_options?: Configuration): Observable<Array<string>> {
        const requestContextPromise = this.requestFactory.listCategories(_options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(
                mergeMap((ctx: RequestContext) => middleware.pre(ctx))
            );
        }

        return middlewarePreObservable
            .pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx)))
            .pipe(
                mergeMap((response: ResponseContext) => {
                    let middlewarePostObservable = of(response);
                    for (let middleware of this.configuration.middleware) {
                        middlewarePostObservable = middlewarePostObservable.pipe(
                            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
                        );
                    }
                    return middlewarePostObservable.pipe(
                        map((rsp: ResponseContext) => this.responseProcessor.listCategories(rsp))
                    );
                })
            );
    }

    /**
     * List registered service with query params.
     * @param categoryName category of the service
     * @param cspName name of the service provider
     * @param serviceName name of the service
     * @param serviceVersion version of the service
     */
    public listRegisteredServices(
        categoryName?: string,
        cspName?: string,
        serviceName?: string,
        serviceVersion?: string,
        _options?: Configuration
    ): Observable<Array<RegisterServiceEntity>> {
        const requestContextPromise = this.requestFactory.listRegisteredServices(
            categoryName,
            cspName,
            serviceName,
            serviceVersion,
            _options
        );

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(
                mergeMap((ctx: RequestContext) => middleware.pre(ctx))
            );
        }

        return middlewarePreObservable
            .pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx)))
            .pipe(
                mergeMap((response: ResponseContext) => {
                    let middlewarePostObservable = of(response);
                    for (let middleware of this.configuration.middleware) {
                        middlewarePostObservable = middlewarePostObservable.pipe(
                            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
                        );
                    }
                    return middlewarePostObservable.pipe(
                        map((rsp: ResponseContext) => this.responseProcessor.listRegisteredServices(rsp))
                    );
                })
            );
    }

    /**
     * List registered service group by serviceName, serviceVersion, cspName with category.
     * @param categoryName category of the service
     */
    public listRegisteredServicesTree(
        categoryName: string,
        _options?: Configuration
    ): Observable<Array<CategoryOclVo>> {
        const requestContextPromise = this.requestFactory.listRegisteredServicesTree(categoryName, _options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(
                mergeMap((ctx: RequestContext) => middleware.pre(ctx))
            );
        }

        return middlewarePreObservable
            .pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx)))
            .pipe(
                mergeMap((response: ResponseContext) => {
                    let middlewarePostObservable = of(response);
                    for (let middleware of this.configuration.middleware) {
                        middlewarePostObservable = middlewarePostObservable.pipe(
                            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
                        );
                    }
                    return middlewarePostObservable.pipe(
                        map((rsp: ResponseContext) => this.responseProcessor.listRegisteredServicesTree(rsp))
                    );
                })
            );
    }

    /**
     * Register new service using ocl model.
     * @param ocl
     */
    public register(ocl: Ocl, _options?: Configuration): Observable<string> {
        const requestContextPromise = this.requestFactory.register(ocl, _options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(
                mergeMap((ctx: RequestContext) => middleware.pre(ctx))
            );
        }

        return middlewarePreObservable
            .pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx)))
            .pipe(
                mergeMap((response: ResponseContext) => {
                    let middlewarePostObservable = of(response);
                    for (let middleware of this.configuration.middleware) {
                        middlewarePostObservable = middlewarePostObservable.pipe(
                            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
                        );
                    }
                    return middlewarePostObservable.pipe(
                        map((rsp: ResponseContext) => this.responseProcessor.register(rsp))
                    );
                })
            );
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
            middlewarePreObservable = middlewarePreObservable.pipe(
                mergeMap((ctx: RequestContext) => middleware.pre(ctx))
            );
        }

        return middlewarePreObservable
            .pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx)))
            .pipe(
                mergeMap((response: ResponseContext) => {
                    let middlewarePostObservable = of(response);
                    for (let middleware of this.configuration.middleware) {
                        middlewarePostObservable = middlewarePostObservable.pipe(
                            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
                        );
                    }
                    return middlewarePostObservable.pipe(
                        map((rsp: ResponseContext) => this.responseProcessor.unregister(rsp))
                    );
                })
            );
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
            middlewarePreObservable = middlewarePreObservable.pipe(
                mergeMap((ctx: RequestContext) => middleware.pre(ctx))
            );
        }

        return middlewarePreObservable
            .pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx)))
            .pipe(
                mergeMap((response: ResponseContext) => {
                    let middlewarePostObservable = of(response);
                    for (let middleware of this.configuration.middleware) {
                        middlewarePostObservable = middlewarePostObservable.pipe(
                            mergeMap((rsp: ResponseContext) => middleware.post(rsp))
                        );
                    }
                    return middlewarePostObservable.pipe(
                        map((rsp: ResponseContext) => this.responseProcessor.update(rsp))
                    );
                })
            );
    }
}
