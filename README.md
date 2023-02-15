# xpanse

Xpanse is an Open Source project allowing to easily implement native managed service on any cloud service
provider.

Xpanse unleash your cloud services by removing vendor lock-in and lock out. It standardizes and exposes
cloud service providers core services, meaning that your xpanse service is portable (multi-cloud) on any
cloud topology and provider.
It also avoids tight coupling of your service to other cloud service provider services.

## APIs (core services)

Xpanse interacts directly with the fundamental APIs used by the cloud service provider to create managed
service:

* **identity** dealing with access, users, groups, roles, ...
* **computing** abstracts the manipulation of virtual machines
* **storage** abstracts the manipulation of storage volumes
* **vpc** abstracts the manipulation of network devices
* **billing** registers the business model in the cloud provider billing system
* ...

## Configuration Language

A managed service is described using Open Services Cloud Configuration Language (OCL).

OCL is a json descriptor of a managed service, describing the expected final state of your service, interacting with the
fundamental APIs:

```json
{
  "name": "my-service",
  "category": "compute",
  "namespace": "my-namespace",
  "billing": {
    "model": "flat",
    "period": "monthly",
    "currency": "euro",
    "fixedPrice": 20,
    "variablePrice": 10
  },
  "compute": {
    "vms": [
      {
        "name": "my-vm",
        "type": "t2.large",
        "imageId": "c7d4ff3e-a851-11ed-b9df-f329738732c0",
        "subnet": [
          "$.network.subnets[0]"
        ],
        "securityGroups": [
          "$.network.securityGroups[0]"
        ],
        "storage": [
          "$.storages[0]"
        ],
        "publicly": true
      }
    ]
  },
  "network": {
    "vpc": [
      {
        "name": "my-vpc",
        "cidr": "172.31.0.0/16"
      }
    ],
    "subnets": [
      {
        "name": "my-subnet",
        "vpc": "$.network.vpc[0]",
        "cidr": "172.31.1.0/24"
      }
    ],
    "securityGroups": [
      {
        "name": "my-sg",
        "rules": [
          {
            "name": "my-remote-desktop",
            "priority": 1,
            "protocol": "tcp",
            "cidr": "172.31.2.0/24",
            "direction": "inbound",
            "ports": "3389",
            "action": "allow"
          }
        ]
      }
    ]
  },
  "storages": [
    {
      "name": "my-storage",
      "type": "ssd",
      "size": "80",
      "sizeUnit": "GB"
    }
  ]
}
```

## OCL loading

Xpanse provides different options to generate and provision OCL:

* REST API on the xpanse runtime
* CLI allowing to directly interact with xpanse via command line
* language frontend (SDL) for Java, Python, ...

## Orchestrator & binding

OCL descriptor is an abstract description of the final managed service state. It's generic enough to work with any cloud
service provider.

Xpanse runtime embeds an orchestrator responsible to delegate the services management to plugins.

Each plugin is dedicated to handle a cloud provider infrastructure and do actions required to actually deal with the
services' lifecycle:

1. to bind OCL to the concrete cloud provider internal APIs
2. to generate the graph of actions required to reach the final expected state, specifically for a target cloud provider

## Runtime

Xpanse runtime is the overall component running on the cloud provider.

The runtime embeds and run together:

1. the orchestrator with the different bindings
2. the OCL loader and parser
3. the frontends (REST API, ...)

## Database

The default database attached to the runtime is the H2 in-memory database. The same can be replaced with other production
ready database systems by replacing the configurations mentioned below and by adding relevant maven dependencies.

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

First, you can build the whole xpanse project, including all modules (orchestrator, OCL, runtime, plugins, etc), simply
with:

```shell
$ mvn clean install
```

### Run

By default, the application will not activate any plugins. They must be activated via spring profiles. Also ensure that
only one plugin is active at a time.

* for Huawei Cloud:

```shell
$ cd runtime/target
$ java -jar xpanse-runtime-1.0.0-SNAPSHOT.jar -Dspring.profiles.active=huaweicloud
```

* for Openstack:

```shell
$ cd runtime/target
$ java -jar xpanse-runtime-1.0.0-SNAPSHOT.jar -Dspring.profiles.active=openstack
```

By default, the runtime is built in "exploded mode". Additionally, you can also build a Docker image
adding `-Ddocker.skip=false` as build argument:

```shell
$ cd runtime
$ mvn clean install -Ddocker.skip=false
```

We can start xpanse runtime with a specific plugin by passing the plugin name in the profile name. For example to start
huaweicloud

```shell
$ docker run -e "SPRING_PROFILES_ACTIVE=huaweicloud" --name my-xpanse-runtime xpanse
```

### Static Code Analysis using CheckStyle
This project using `CheckStyle` framework to perform static code analysis. The configuration can be found in [CheckStyle](checkstyle.xml). The framework also checks the code format in accordance to `Google Java Format`.

The same file can also be imported in IDE CheckStyle plugins to get the analysis results directly in IDE and also to perform code formatting directly in IDE.

The framework is added as a maven plugin and is executed by default as part of the `verify` phase. Any violations will result in build failure.

### License/Copyright Configuration
All files in the repository must contain a license header in the format mentioned in [License Header](license.header).

The static code analysis framework will also validate if the license exists in the specified format.