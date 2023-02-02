---
sidebar_position: 2
---

# Architecture

Open Services Cloud is a framework reshaping the cloud service ecosystem:

* the cloud users can find the same services across different cloud providers, exactly the same services provided by
  Open Services Cloud.
* the software vendors can easily create native and portable managed services for their software, including seamless and
  low level integration with cloud provider services. In addition of software artifacts, software vendor describe the
  service using the Open Services Cloud Configuration Language (OCL).
* the cloud providers can easily extend their services catalog by registering services described with the Open Services
  Cloud Configuration Language (OCL).

Open Services Cloud allows anyone to create managed services (not only the cloud provider), portable, and fully
integrated within the cloud provider infrastructure.

![Open Services Cloud Architecture](/img/osc_architecture.png "Open Services Cloud Architecture")

The [OCL descriptor](ocl) is a json fully describing the service. This descriptor is handled by the orchestrator.
The OCL descriptor can be wrapped as a terraform provider if needed. It's also possible to extend the OCL descriptor (
using extender).

The orchestrator marshalls the OCL descriptor and constructs an execution graph interacting with the cloud provider.

The interaction logic with the cloud provider is delegated to orchestrator plugins. The orchestrator can use one or more
plugins to deal with the underlying infrastructure services and create the service resources.

The orchestrator defines the following lifecycle for each service:

1. a service is registered in the orchestrator (in a persistent store). During service registration, the orchestrator is
   creating (thanks to the plugins) all resources needed for the service.
2. once registered, the orchestrator can start a service. If registration creates resources, the start phase actually
   starts the service resources.
3. the orchestrator can stop a service, stopping the corresponding service resources, but not removing them.
4. the orchestrator can remove a service, destroying all associated resources.

You can interact with the orchestrator with the OSC [REST API](api).

The OCL loader, orchestrator, plugins and API are all managed in the OSC [runtime](runtime).
The [runtime](runtime) is the glue between all components.