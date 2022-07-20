---
sidebar_position: 1
---

# Introduction

Let's discover **Open Services Cloud in less than 5 minutes**.

## Getting Started

Get started by **creating a simple managed service**.

You will create a service configuration descriptor and you will submit to Open Services Cloud using one supported channel (CLI, REST, ...).

### What you'll need

To actually create the service, you have to submit the service descriptor to **Open Services Cloud orchestrator**.

You can:
- install the orchestrator locally on your machine to test your service. This approach is mostly use for testing as some fundamental services can be mocked.
- use a cloud provider supporting Open Services Cloud, meaning that the orchestrator runs directly on the cloud provider

## Create service descriptor

A service is described with a **yaml file**. In this descriptor, you will define:

- the service components
- the integration of the service with the fundamental services (computing, billing, ...)

The yaml descriptor can be created by hand, or using CLI or starter site (interactive mode).


```bash
bin/ocl --create --name my-service --billing-model renting --billing-period monthly --network ...
```

For example, here's a very simple service descriptor:

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

## Deploy the service

To actually deploy the service, you have to submit the **yaml file** to the **Open Services Cloud orchestrator**.

The orchestrator supports several channels:

- CLI
- REST
- SDK

For instance, you can deploy the service descriptor via REST:

```bash
curl -XPOST -d @service.yaml -H "Content-Type: application/yaml" http://osc.orchestrator/path
```
