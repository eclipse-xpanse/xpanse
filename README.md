<p align='center'>
    <img src="static/logo.png" alt='xpanse-logo' style='align-content: center'>
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

Here is an example to show how OCL can be used to provide a managed Kafka service on an Openstack based cloud.

```yaml
# The version of the OCL
version: 2.0
# The category of the service.
category: middleware
# The Service provided by the ISV, the name will be shown on the console as a service.
name: kafka-cluster
# The version of the service, if the end-user want to select the version when they want to deploy the service.
serviceVersion: v3.3.2
# For the users may have more than one service, the @namespace can be used to separate the clusters.
description: This is an ehanced Kafka cluster services by ISV-A.
namespace: ISV-A
# Icon for the service.
icon: |
  data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGQAAACRAQMAAAAPc4+9AAAAAXNSR0IB2cksfwAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAZQTFRF+/v7Hh8gVD0A0wAAAcVJREFUeJzNlc1twzAMhSX44KNH0CgaTd6gK3kUd4McDVTwq/hjiUyaIkV7qNA2/QCFIh+ppxB+svLNEqqBGTC0ANugBOwmCGDCFOAwIWGDOoqoODtN2BdL6wxD9NMTO9tXPa1PqL5M30W5p8lm5vNcF0t7ahSrVguqNqmMokRW4YQucVjBCBWH1Z2g3WDlW2skoYU+2x8JOtGedBF3k2iXMO0j16iUiI6gxzPdQhnU/s2G9pCO57QY2r6hvjPbKJHq7DRTRXT60avtuTRdbrFJI3mSZhNOqYjVbd99YyK1QKWzEqSWrE0k07U60uPaelflMzaaeu1KBuurHSsn572I1KWy2joX5ZBfWbS/VEt50H5P6aL4JxTuyJ/+QCNPX4PWF3Q8Xe1eF9FsLdD2VaOnaP2hWvs+zI58/7i3vH3nRFtDZpyTUNaZkON5XnBNsp8lrmDMrpvBr+b6pUl+4XbkQdndqnzYGzfuJm1JmIWimIbe6dndd/bk7gVce/cJdo3uIeLJl7+I2xTnPek67mjtDeppE7b03Ov+kSfDe3JweW53njxeGfXkaz28VeYd86+af/H8a7hgJKaebILaFzakLfxyfQLTxVB6K1K9KQAAAABJRU5ErkJggg==
# Reserved for CSP, aws,azure,ali,huawei and ...
cloudServiceProvider:
  name: openstack
  regions:
    - name: RegionOne
      area: Western Europe
    - name: RegionTwo
      area: Western Europe
billing:
  # The business model(`flat`, `exponential`, ...)
  model: flat
  # The rental period (`daily`, `weekly`, `monthly`, `yearly`)
  period: monthly
  # The billing currency (`euro`, `usd`, ...)
  currency: euro
# The flavor of the service, the @category/@name/@version/@flavor can locate the specific service to be deployed.
flavors:
  - name: 1-zookeeper-with-3-worker-nodes-normal
    # The fixed price during the period (the price applied one shot whatever is the service use)
    fixedPrice: 40
    # Properties for the service, which can be used by the deployment.
    property:
      worker_nodes_count: 3
      flavor_id: cirrors256
  - name: 1-zookeeper-with-3-worker-nodes-performance
    # The fixed price during the period (the price applied one shot whatever is the service use)
    fixedPrice: 60
    # Properties for the service, which can be used by the deployment.
    properties:
      worker_nodes_count: 3
      flavor_id: cirrors512
  - name: 1-zookeeper-with-5-worker-nodes-normal
    # The fixed price during the period (the price applied one shot whatever is the service use)
    fixedPrice: 60
    # Properties for the service, which can be used by the deployment.
    properties:
      worker_nodes_count: 5
      flavor_id: cirrors256
  - name: 1-zookeeper-with-5-worker-nodes-performance
    # The fixed price during the period (the price applied one shot whatever is the service use)
    fixedPrice: 80
    # Properties for the service, which can be used by the deployment.
    properties:
      worker_nodes_count: 5
      flavor_id: cirrors512
deployment:
  # kind, Supported values are terraform, pulumi, crossplane.
  kind: terraform
  # Context for deployment: the context including some kind of parameters for the deployment, such as fix,variable.
  # - env: The value of the fix parameters are defined by the ISV with the @value at the initial time.
  # - variable: The value of the variable parameters are defined by the user on the console.
  # The parameters will be used to generate the API of the managed service.
  variables:
    - name: OS_AUTH_URL
      description: The Identity authentication URL.
      kind: env
      dataType: string
      mandatory: true
    - name: OS_USER_NAME
      description: The User Name to login with.
      kind: env
      dataType: string
      mandatory: false
    - name: OS_PASSWORD
      description: The User password to login with.
      kind: env
      dataType: string
      mandatory: false
    - name: OS_REGION_NAME
      description: The region of the OpenStack cloud to use.
      kind: env
      dataType: string
      mandatory: false
    - name: OS_PROJECT_NAME
      description: The name of the Tenant (Identity v2) or Project (Identity v3) to login with.
      kind: env
      dataType: string
      mandatory: false
    - name: admin_passwd
      description: The admin password of all nodes in the Kafka cluster. If the value is empty, will create a random password.
      kind: variable
      dataType: string
      mandatory: false
      validator: minLength=8|maxLength=16|pattern=^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,16}$
    - name: vpc_name
      description: The vpc name of all nodes in the Kafka cluster. If the value is empty, will use the example value to find or create VPC.
      kind: variable
      dataType: string
      example: "kafka-vpc-default"
      mandatory: false
    - name: subnet_name
      description: The sub network name of all nodes in the Kafka cluster. If the value is empty, will use the example value to find or create subnet.
      kind: variable
      dataType: string
      example: "kafka-subnet-default"
      mandatory: false
    - name: secgroup_name
      description: The security group name of all nodes in the Kafka cluster. If the value is empty, will use the example value to find or create security group.
      kind: variable
      dataType: string
      example: "kafka-secgroup-default"
      mandatory: false
  deployer: |
    variable "flavor_id" {
      type        = string
      default     = "cirros256"
      description = "The flavor_id of all nodes in the Kafka cluster."
    }

    variable "worker_nodes_count" {
      type        = string
      default     = 3
      description = "The worker nodes count in the Kafka cluster."
    }

    variable "admin_passwd" {
      type= string
      default = ""
      description = "The root password of all nodes in the Kafka cluster."
    }

    variable "vpc_name" {
      type        = string
      default     = "kafka-vpc-default"
      description = "The vpc name of all nodes in the Kafka cluster."
    }

    variable "subnet_name" {
      type        = string
      default     = "kafka-subnet-default"
      description = "The subnet name of all nodes in the Kafka cluster."
    }

    variable "secgroup_name" {
      type        = string
      default     = "kafka-secgroup-default"
      description = "The security group name of all nodes in the Kafka cluster."
    }

    data "openstack_networking_network_v2" "existing" {
      name  = var.vpc_name
      count = length(data.openstack_networking_network_v2.existing)
    }

    data "openstack_networking_subnet_v2" "existing" {
      name  = var.subnet_name
      count = length(data.openstack_networking_subnet_v2.existing)
    }

    data "openstack_networking_secgroup_v2" "existing" {
      name  = var.secgroup_name
      count = length(data.openstack_networking_secgroup_v2.existing)
    }

    locals {
      admin_passwd  = var.admin_passwd == "" ? random_password.password.result : var.admin_passwd
      vpc_id        = length(data.openstack_networking_network_v2.existing) > 0 ? data.openstack_networking_network_v2.existing[0].id : openstack_networking_network_v2.new[0].id
      subnet_id     = length(data.openstack_networking_subnet_v2.existing) > 0 ? data.openstack_networking_subnet_v2.existing[0].id : openstack_networking_subnet_v2.new[0].id
      secgroup_id   = length(data.openstack_networking_secgroup_v2.existing) > 0 ? data.openstack_networking_secgroup_v2.existing[0].id : openstack_networking_secgroup_v2.new[0].id
      secgroup_name = length(data.openstack_networking_secgroup_v2.existing) > 0 ? data.openstack_networking_secgroup_v2.existing[0].name : openstack_networking_secgroup_v2.new[0].name
    }

    resource "openstack_networking_network_v2" "new" {
      count = length(data.openstack_networking_network_v2.existing) == 0 ? 1 : 0
      name  = "${var.vpc_name}-${random_id.new.hex}"
    }

    resource "openstack_networking_subnet_v2" "new" {
      count      = length(data.openstack_networking_subnet_v2.existing) == 0 ? 1 : 0
      network_id     = local.vpc_id
      name       = "${var.subnet_name}-${random_id.new.hex}"
      cidr       = "192.168.10.0/24"
      gateway_ip = "192.168.10.1"
    }

    resource "openstack_networking_secgroup_v2" "new" {
      count       = length(data.openstack_networking_secgroup_v2.existing) == 0 ? 1 : 0
      name        = "${var.secgroup_name}-${random_id.new.hex}"
      description = "Kafka cluster security group"
    }

    resource "openstack_networking_secgroup_rule_v2" "secgroup_rule_0" {
      count             = length(data.openstack_networking_secgroup_v2.existing) == 0 ? 1 : 0
      direction         = "ingress"
      ethertype         = "IPv4"
      protocol          = "tcp"
      port_range_min    = 22
      port_range_max    = 22
      remote_ip_prefix  = "121.37.117.211/32"
      security_group_id = local.secgroup_id
    }

    resource "openstack_networking_secgroup_rule_v2" "secgroup_rule_1" {
      count             = length(data.openstack_networking_secgroup_v2.existing) == 0 ? 1 : 0
      direction         = "ingress"
      ethertype         = "IPv4"
      protocol          = "tcp"
      port_range_min    = 2181
      port_range_max    = 2181
      remote_ip_prefix  = "121.37.117.211/32"
      security_group_id = local.secgroup_id
    }

    resource "openstack_networking_secgroup_rule_v2" "secgroup_rule_2" {
      count             = length(data.openstack_networking_secgroup_v2.existing) == 0 ? 1 : 0
      direction         = "ingress"
      ethertype         = "IPv4"
      protocol          = "tcp"
      port_range_min    = 9092
      port_range_max    = 9093
      remote_ip_prefix  = "121.37.117.211/32"
      security_group_id = local.secgroup_id
    }

    data "openstack_compute_availability_zones_v2" "osc-az" {}

    resource "random_id" "new" {
      byte_length = 4
    }

    resource "random_password" "password" {
      length           = 12
      upper            = true
      lower            = true
      numeric          = true
      special          = true
      min_special      = 1
      override_special = "#%@"
    }

    resource "openstack_compute_keypair_v2" "keypair" {
      name = "keypair-k8s-${random_id.new.hex}"
    }

    data "openstack_images_image_v2" "image" {
      name        = "cirros-0.5.2-x86_64-disk"
      most_recent = true
    }

    resource "openstack_compute_instance_v2" "zookeeper" {
      availability_zone  = data.openstack_compute_availability_zones_v2.osc-az.names[0]
      name               = "kafka-zookeeper-${random_id.new.hex}"
      flavor_name        = var.flavor_id
      security_groups    = [ local.secgroup_name ]
      image_id           = data.openstack_images_image_v2.image.id
      key_pair           = openstack_compute_keypair_v2.keypair.name
      network {
        uuid = local.vpc_id
      }
      user_data = <<EOF
        #!bin/bash
        echo root:${local.admin_passwd} | sudo chpasswd
        sudo systemctl start docker
        sudo systemctl enable docker
        sudo docker run -d --name zookeeper-server --privileged=true -p 2181:2181 -e ALLOW_ANONYMOUS_LOGIN=yes bitnami/zookeeper:3.8.1
      EOF
    }

    resource "openstack_compute_instance_v2" "kafka-broker" {
      count              = var.worker_nodes_count
      availability_zone  = data.openstack_compute_availability_zones_v2.osc-az.names[0]
      name               = "kafka-broker-${count.index}-${random_id.new.hex}"
      flavor_name        = var.flavor_id
      security_groups    = [ local.secgroup_name ]
      image_id           = data.openstack_images_image_v2.image.id
      key_pair           = openstack_compute_keypair_v2.keypair.name
      network {
        uuid = local.vpc_id
      }
      user_data = <<EOF
        #!bin/bash
        echo root:${local.admin_passwd} | sudo chpasswd
        sudo systemctl start docker
        sudo systemctl enable docker
        private_ip=$(ifconfig | grep -A1 "eth0" | grep 'inet' | awk -F ' ' ' {print $2}'|awk ' {print $1}')
        sudo docker run -d --name kafka-server --restart always -p 9092:9092 -p 9093:9093  -e KAFKA_BROKER_ID=${count.index}  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://$private_ip:9092 -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 -e ALLOW_PLAINTEXT_LISTENER=yes -e KAFKA_CFG_ZOOKEEPER_CONNECT=${openstack_compute_instance_v2.zookeeper.access_ip_v4}:2181 bitnami/kafka:3.3.2
      EOF
      depends_on = [
        openstack_compute_instance_v2.zookeeper
      ]
    }

    output "zookeeper_server" {
      value = "${openstack_compute_instance_v2.zookeeper.access_ip_v4}:2181"
    }

    output "admin_passwd" {
      value = var.admin_passwd == "" ? nonsensitive(local.admin_passwd) : local.admin_passwd
    }

```

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
