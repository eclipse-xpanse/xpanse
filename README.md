<p align='center'>
    <img src="static/full-logo.png" alt='xpanse-logo' style='align-content: center'>
</p>
<p align='center'>
<a href="https://github.com/eclipse-xpanse/xpanse/actions/workflows/osc.yml" target="_blank">
    <img src="https://github.com/eclipse-xpanse/xpanse/actions/workflows/osc.yml/badge.svg" alt="build">
</a>
</p>

Xpanse is an Open Source project allowing to easily implement native managed
service on any cloud service provider. This project is part of the Open Services Cloud (OSC) charter.

Xpanse unleashes your cloud services by removing vendor lock-in and lock out.
It standardizes and exposes cloud service providers core services, meaning
that your xpanse service is portable (multi-cloud) on any cloud topology and
provider. It also avoids tight coupling of your service to other cloud service
provider services.

## Configuration Language

A managed service is described using Open Services Cloud Configuration Language
(OCL).

OCL is a yaml descriptor of a managed service, describing the expected final
state of your service, interacting with the fundamental APIs:

Examples of managed services described using OCL for multiple clouds can be found [here](samples).

### Deployment Scripts

In OCL, the deployer variable can contain the script that must be executed for provisioning the managed service.
Currently, the only allowed script is Terraform.

### Flavors

For each managed service, we can define different flavors of it. For example, different sizes of the VM, etc.
End user can then select the flavor of their preference for the service while ordering.

### Flavor properties

Flavors can have properties which can be simply declared and referred in the deployment script too with the same
property
names.
Runtime wil ensure tht these variables are automatically available for the deployment scripts

### Deployment Variables

As part of the OCL, the managed service provider can define variables that can be either entered by the user or
available as defaults.
All possible types of variables are defined
here [Deployment Variables](modules/models/src/main/java/org/eclipse/xpanse/modules/models/resource/DeployVariable.java)
The variables can then be used in the deployment scripts.

## OCL loading

Xpanse provides different options to generate and provision OCL:

* REST API on the xpanse runtime
* Xpanse UI

## Orchestrator & binding

OCL descriptor is an abstract description of the final managed service state.
It's generic enough to work with any cloud service provider.

Xpanse runtime embeds an orchestrator responsible to delegate the services
management to plugins.

Each plugin is dedicated to handle a cloud provider infrastructure and do
actions required to actually deal with the services' lifecycle:

1. to bind OCL to the concrete cloud provider internal APIs
2. to generate the graph of actions required to reach the final expected state,
   specifically for a target cloud provider

## Runtime

Xpanse runtime is the overall component running on the cloud provider.

The runtime embeds and run together:

1. the orchestrator with the different bindings
2. the OCL loader and parser
3. the frontends (REST API, ...)

## Database

The default database attached to the runtime is the H2 in-memory database.
The same can be replaced with other production ready database systems by
replacing the configurations mentioned below and by adding relevant maven
dependencies.

```
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto= update
```

### Build and Package

First, you can build the whole xpanse project, including all modules
(orchestrator, OCL, runtime, plugins, etc), simply with:

```shell
$ mvn clean install
```

### Run

By default, the application will not activate any plugins. They must be
activated via spring profiles. Also ensure that only one plugin is active at a
time. For example, openstack plugin can be activated as below

```shell
$ cd runtime/target
$ java -jar xpanse-runtime-1.0.0-SNAPSHOT.jar -Dspring.profiles.active=openstack
```

By default, the runtime is built in "exploded mode". Additionally, you can also
build a Docker image adding `-Ddocker.skip=false` as build argument:

```shell
$ cd runtime
$ mvn clean install -Ddocker.skip=false
```

We can start xpanse runtime with a specific plugin by passing the plugin name
in the profile name. For example to start openstack

```shell
$ docker run -e "SPRING_PROFILES_ACTIVE=openstack" --name my-xpanse-runtime xpanse
```

### Static Code Analysis using CheckStyle

This project using `CheckStyle` framework to perform static code analysis. The
configuration can be found in [CheckStyle](checkstyle.xml). The framework also
checks the code format in accordance to `Google Java Format`.

The same file can also be imported in IDE CheckStyle plugins to get the
analysis results directly in IDE and also to perform code formatting directly
in IDE.

The framework is added as a maven plugin and is executed by default as part of
the `verify` phase. Any violations will result in build failure.

### License/Copyright Configuration

All files in the repository must contain a license header in the format
mentioned in [License Header](license.header).

The static code analysis framework will also validate if the license exists in
the specified format.
