# Eclipse Open Services Cloud

Eclipse Open Services Cloud is an Open Source project allowing to easily implement native managed service on any cloud service provider.

Eclipse Open Services Cloud unleash your cloud services by removing vendor lock-in and lock out. It standardizes and exposes cloud service providers core services, meaning that your Open Services Cloud service is portable (multi-cloud) on any cloud topology and provider.
It also avoids tight coupling of your service to other cloud service provider services.

## APIs & SDKs

Eclipse Open Services Cloud provides a set of core services APIs. Your managed service connector is basically an orchestration of these APIs
to "integrate" your managed service in the cloud service provider infrastructure.

Eclipse Open Services Cloud APIs are, for instance:

* **identity** dealing with access, users, groups, roles, etc
* **computing** abstracts the manipulation of virtual machines
* **billing** to "hook" into the cloud service provider billing system
* ...

Eclipse Open Services Cloud also provides SDKs to easily use (wrapping) the APIs:

* Java SDK is the first citizen SDK, allowing to implement your connector using Java
* Python SDK is the equivalent using Python language

## Orchestrator

Not all cloud providers natively supports Open Services Cloud APIs. Eclipse Open Services Cloud embeds an orchestrator "translating" OSC API calls to the cloud service provider core API calls.

The orchestrator is "pluggable" meaning you can add new cloud service provider plugin and your own plugin.

## Runtime

Eclipse Open Services Cloud also provide ready to use runtime that you can deploy on your infrastructure.

This runtime contains three components:

* API server exposes the APIs including OpenAPI descriptor
* Connectors container is where you can deploy and runtime your managed service connectors
* Orchestrator is behind the API server converting eventually the Open Services Cloud API calls to the cloud service provider native calls
