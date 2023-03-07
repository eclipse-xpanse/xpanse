# xpanse

Xpanse is an Open Source project allowing to easily implement native managed service on any cloud
service provider.

Xpanse unleash your cloud services by removing vendor lock-in and lock out. It standardizes and
exposes cloud service providers core services, meaning that your xpanse service is portable (
multi-cloud) on any cloud topology and provider. It also avoids tight coupling of your service to
other cloud service provider services.

## APIs (core services)

Xpanse interacts directly with the fundamental APIs used by the cloud service provider to create
managed service:

* **identity** dealing with access, users, groups, roles, ...
* **computing** abstracts the manipulation of virtual machines
* **storage** abstracts the manipulation of storage volumes
* **vpc** abstracts the manipulation of network devices
* **billing** registers the business model in the cloud provider billing system
* ...

## Configuration Language

A managed service is described using Open Services Cloud Configuration Language (OCL).

OCL is a json descriptor of a managed service, describing the expected final state of your service,
interacting with the fundamental APIs:

```yaml
# The version of the OCL
version: 2.0
# The category of the service.
category: middleware
# The Service provided by the ISV, the name will be shown on the console as a service.
name: kafka
# The version of the service, if the end-user want to select the version when they want to deploy the service.
serviceVersion: v1.0
# For the users may have more than one service, the @namespace can be used to separate the clusters.
description: This is an ehanced kafka services by ISV-A.
namespace: ISV-A
# Icon for the service.
icon: |
  data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGQAAACRAQMAAAAPc4+9AAAAAXNSR0IB2cksfwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAZQTFRF+/v7Hh8gVD0A0wAAAcVJREFUeJzNlc1twzAMhSX44KNH0CgaTd6gK3kUd4McDVTwq/hjiUyaIk
  V7qNA2/QCFIh+ppxB+svLNEqqBGTC0ANugBOwmCGDCFOAwIWGDOoqoODtN2BdL6wxD9NMTO9tXPa1PqL5M30W5p8lm5vNcF0t7ahSrVguqNqmMokRW4YQucVjBCBWH1Z2g3WDlW2skoYU+2x8JOtGedBF3k2iXMO0j16iUiI6gxzPdQhnU/s2G9pCO57QY2r6hvj
  PbKJHq7DRTRXT60avtuTRdbrFJI3mSZhNOqYjVbd99YyK1QKWzEqSWrE0k07U60uPaelflMzaaeu1KBuurHSsn572I1KWy2joX5ZBfWbS/VEt50H5P6aL4JxTuyJ/+QCNPX4PWF3Q8Xe1eF9FsLdD2VaOnaP2hWvs+zI58/7i3vH3nRFtDZpyTUNaZkON5XnBNsp
  8lrmDMrpvBr+b6pUl+4XbkQdndqnzYGzfuJm1JmIWimIbe6dndd/bk7gVce/cJdo3uIeLJl7+I2xTnPek67mjtDeppE7b03Ov+kSfDe3JweW53njxeGfXkaz28VeYd86+af/H8a7hgJKaebILaFzakLfxyfQLTxVB6K1K9KQAAAABJRU5ErkJggg==
# Reserved for CSP, aws,azure,ali,huawei and ...
cloudServiceProvider:
  name: aws
  regions:
    - aws-ca-central-1
    - aws-us-east-2
billing:
  # The business model(`flat`, `exponential`, ...)
  model: flat
  # The rental period (`daily`, `weekly`, `monthly`, `yearly`)
  period: monthly
  # The billing currency (`euro`, `usd`, ...)
  currency: euro
# The flavor of the service, the @name/@version/@flavor can locate the specific service to be deployed.
flavors:
  - name: 3-node-without-zookeeper
    # The fixed price during the period (the price applied one shot whatever is the service use)
    fixedPrice: 20
    # Properties for the service, which can be used by the deployment.
    property:
      node: 3
      zookeeper: false
  - name: 5-node-with-zookeeper
    # The fixed price during the period (the price applied one shot whatever is the service use)
    fixedPrice: 30
    # Properties for the service, which can be used by the deployment.
    property:
      node: 5
      zookeeper: true
deployment:
  # kind, Supported values are terraform, pulumi, crossplane.
  kind: terraform
  # Context for deployment: the context including some kind of parameters for the deployment, such as fix,variable.
  # - env: This kind of variable will be injected as environment variables to the runtime of the deployment.
  # - variable: This kind of variable will be injected as parameters to the runtime of the deployment.
  # - fix_env: like the @env, but the value of the variable will be settled in the OCL template.
  # - fix_variable: like the @variable, but the value of the variable will be settled in the OCL template.
  # The parameters will be used to generate the API of the managed service.
  context:
    - name: AK
      description: Your access key.
      kind: env
      type: string
      mandatory: true
      validator: null
    - name: SK
      description: Your security key.
      kind: env
      type: string
      mandatory: true
      validator: null
    - name: VPC
      description: The vpc you want to deploy, If null, A new vpc will be created.
      kind: variable
      type: string
      mandatory: false
      validator: length(1-256)
  deployer: |
    data "aws_ami" "ubuntu" {
      most_recent = true

      filter {
        name   = "name"
        values = ["ubuntu/images/hvm-ssd/ubuntu-focal-20.04-amd64-server-*"]
      }

      filter {
        name   = "virtualization-type"
        values = ["hvm"]
      }

      owners = ["099720109477"] # Canonical
    }

    resource "aws_instance" "web" {
      ami           = data.aws_ami.ubuntu.id
      instance_type = "t3.micro"

      tags = {
        Name = "kafka"
      }
    }

```

## OCL loading

Xpanse provides different options to generate and provision OCL:

* REST API on the xpanse runtime
* CLI allowing to directly interact with xpanse via command line
* language frontend (SDL) for Java, Python, ...

## Orchestrator & binding

OCL descriptor is an abstract description of the final managed service state. It's generic enough to
work with any cloud service provider.

Xpanse runtime embeds an orchestrator responsible to delegate the services management to plugins.

Each plugin is dedicated to handle a cloud provider infrastructure and do actions required to
actually deal with the services' lifecycle:

1. to bind OCL to the concrete cloud provider internal APIs
2. to generate the graph of actions required to reach the final expected state, specifically for a
   target cloud provider

## Runtime

Xpanse runtime is the overall component running on the cloud provider.

The runtime embeds and run together:

1. the orchestrator with the different bindings
2. the OCL loader and parser
3. the frontends (REST API, ...)

## Database

The default database attached to the runtime is the H2 in-memory database. The same can be replaced
with other production ready database systems by replacing the configurations mentioned below and by
adding relevant maven dependencies.

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

First, you can build the whole xpanse project, including all modules (orchestrator, OCL, runtime,
plugins, etc), simply with:

```shell
$ mvn clean install
```

### Run

By default, the application will not activate any plugins. They must be activated via spring
profiles. Also ensure that only one plugin is active at a time.

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

We can start xpanse runtime with a specific plugin by passing the plugin name in the profile name.
For example to start huaweicloud

```shell
$ docker run -e "SPRING_PROFILES_ACTIVE=huaweicloud" --name my-xpanse-runtime xpanse
```

### Static Code Analysis using CheckStyle

This project using `CheckStyle` framework to perform static code analysis. The configuration can be
found in [CheckStyle](checkstyle.xml). The framework also checks the code format in accordance
to `Google Java Format`.

The same file can also be imported in IDE CheckStyle plugins to get the analysis results directly in
IDE and also to perform code formatting directly in IDE.

The framework is added as a maven plugin and is executed by default as part of the `verify` phase.
Any violations will result in build failure.

### License/Copyright Configuration

All files in the repository must contain a license header in the format mentioned
in [License Header](license.header).

The static code analysis framework will also validate if the license exists in the specified format.
