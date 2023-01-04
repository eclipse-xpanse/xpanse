---
sidebar_position: 5
---

# REST API

OSC Runtime includes a REST API allowing you to interact with the orchestrator.

By default, the REST API is using `/osc` as context path (it can be changed in the `rest.path` runtime configuration).

## `/osc/health`

The `/osc/health` endpoint is a very simple endpoint checking if the runtime is operating correctly.

It's a `GET` operation, just returning `ready` (raw text) if all is OK:

```shell
curl -XGET http://host/osc/health
ready
```

## `/osc/register`

The `/osc/register` endpoint allows you to register a service by providing a OCL json descriptor.

It's a `POST` operation expecting `application/json` as content type:

```shell
curl -XPOST -H "Content-Type: application/json" -d @my-osl.json http://host/osc/register
```

## `/osc/register/fetch`

The `/osc/register/fetch` endpoint allows you to provide the location of a OCL json descriptor (on HTTP URL for instance) as header.

The orchestrator will fetch the OCL json descriptor from the location and deploy the corresponding service.

It's a `POST` operation, the OCL location is passed with the `ocl` header:

```shell
curl -XPOST -H "ocl: http://foo.bar/ocl.json" http://host/osc/register/fetch
```

## `/osc/services`

The `/osc/services` endpoint lists all registered services.

It's a `GET` operation:

```shell
curl -XGET http://host/osc/services
my-service-1
my-service-2
...
```

## `/osc/start`

The `/osc/start` endpoint starts a registered service (you can list all services using `/osc/services` endpoint).

It's a `POST` operation, the service is passed with the `managedServiceName` header:

```shell
curl -XPOST -H "managedServiceName: my-service" http://host/osc/start
```

## `/osc/stop`

The `/osc/stop` endpoint stops a registered service.

It's a `POST` operation, the service is passed with the `managedServiceName` header:

```shell
curl -XPOST -H "managedServiceName: my-service" http://host/osc/stop
```

## `/osc/update`

The `/osc/update` endpoint updates a registered service with a new OCL json descriptor.

The `managedServiceName` header identifies the service to update, the "new" OCL json is passed directly on the stream:

```shell
curl -XPOST -H "managedServiceName: my-service" -H "Content-Type: application/json" -d @my-new-ocl.json http://host/osc/update
```

## `/osc/update/fetch`

The `/osc/update/fetch` endpoint updates a registered service with by fetching a new OCL json descriptor from a location.

The `managedServiceName` header identified the service to update, the `ocl` header defines the new OCL json descriptor location:

```shell
curl -XPOST -H "managedServiceName: my-service" -H "ocl: http://host/my-new-ocl.json" http://host/osc/update/fetch
```