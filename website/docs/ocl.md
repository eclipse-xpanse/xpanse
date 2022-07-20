---
sidebar_position: 2
---

# Configuration Language

The Open Services Cloud Configuration Language allows you to describe your service and interaction with fundamentals services (computing, network, billing, ...).

## Abstract

The abstract part of the configuration language is described in the `osc` element.

In this element, you can define:

- `osc_version` is the required Open Services Cloud to run and deploy the service. It could be an exact version (`osc_version = "0.0.1"`), a minimal version (`osc_version = ">= 0.0.1"`°, or a range (`osc_version = "[0.0.1,0.0.2]"`)
- `name` is the name of your service, used to identify the service in the different places
- `version` is the version of your service, used to identify the service (useful especially when you want to upgrade a service)
- `namespace` defines the namespace where the service will be located
- `region` defines the region where the service can be available (if not provided, it means all regions)

## Services Integration

### Billing

The `billing` component defines the integration into cloud provider billing system.

You can configure the business model associated to the service:

- `model`defines the business model (`renting` or `single`)
- `period` defines the rental period (`daily`, `weekly`, `monthly`, `yearly`)
- `fixed_price` is the fixed price during the period (the price applied one shot whatever is the service use)
- `variable_price` is the price depending of item volume
- `variable_item` is the item used to calculate the variable price on the period (for instance, the number of instances, the number of transactions, ...)

### Network

#### VPC

The `vpc` element defines the Virtual Private Cloud used by the service.

- `name` is the name of VPC
- `cidr` is the IP addresses range of the VPC

#### Subnet

The `subnet` element defines a subnet on the VPC.

- `vpc` is the VPC where the subnet is located
- `name` is the name of the subnet
- `cidr` is the IP addresses range of the subnet
- `gatteway` is the IP address of the gateway to route the subnet`

### Computing

The `computing` element defines the computing resources (virtual machines, kubernetes, ...) used by the service.

- `registry` is the computing resource registry (docker registry, ...). It could be a registry name or host
- `cluster` is the computing resource cluster name (useful when used with Kubernetes)
- `image` is the docker or virtual machine image name

### Storage

### Administration Console

### Observability & Tracing

#### Logging

#### Tracing

#### Metrics (Gauge, ...)

### Identity Management

### Baseline

## Example

```yaml
osc:
  osc_version: >=0.0.1
  name: my-service
  version: 1.0
  namespace: my-namespace
  region: eu-west-france
billing:
  model: renting
  period: monthly
  fixed_price: 20
  variable_price: 10
  variable_item: instance
network:
  vpc:
    name: my-vpc
    cidr: 192.168.1.0/24
  subnet:
    vpc: my-vpc
    name: mysubnet
    cidr: 192.168.1.0/26
    gateway: 192.168.1.1
  dns:
    - entry: a
      domain: my-domain
      nodes: mynode
computing:
  registry: docker-registry
  cluster: k8s-location
  image: mysoftware
storage:
  - blob: myblobstorage
container:
  cluster: my-cluster
  image: docker-image
  replica: 10
```

