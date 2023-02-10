---
sidebar_position: 1
---

# Introduction

Let's discover **Open Services Cloud in less than 5 minutes**.

## Getting Started

**Open Services Cloud** is composed by:

* an orchestrator responsible of the managed services (deployment, start, stop, ...) and loading
  plugins specific for each supported cloud provider
* a language describing managed services, called OCL (Open Services Cloud Configuration Language)
* a REST API to interact with the orchestrator
* runtime (eventually including cloud provider plugins) assemblying all components together in a
  running service

Let's get started by **launching OSC runtime** and **deploying a simple managed service**.

You will create a simple service descriptor and you will deploy to Open Services Cloud using the
orchestrator REST API.

### What you'll need

You need a OSC runtime:

* use the runtime provided by a cloud provider supporting Open Services Cloud (the cloud provider
  already have the runtime and you can directly use the APIs)
* launch the runtime of your cloud infrastructure (on a VM or Kubernetes cluster for instance)
* launch the runtime on your machine or cloud infrastructure.

#### Running locally or on VM

You can [download](/download) the OSC runtime or [build your own runtime](runtime).

In "exploded mode", you have a `runtime` folder, where you can easily launch with:

```shell
$ java -jar osc-runtime-1.0-SNAPSHOT.jar
```

You can copy the whole `runtime` folder on another VM or machine and launch the same way.

#### Running on Docker, Kubernetes

The runtime is also available as Docker images. You can run a Docker container with:

```shell
$ docker run -d --name my-runtime -p 8080:8080 osc
```

OS also provides Kubernetes manifest files allowing you to easily deploy on K8S
using `kubectl apply -f`.

Take a look on [runtime documentation](runtime) for details.

## Create service descriptor

A service is described with a **json file**. In this descriptor, you will define:

- the service components
- the integration of the service with the fundamental services (computing, billing, ...)

The descriptor can be created by hand, or using CLI or starter site (interactive mode).

For example, here's a very simple service descriptor:

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
        "image": "c7d4ff3e-a851-11ed-b9df-f329738732c0",
        "subnets": [
          "$.network.subnets[0]"
        ],
        "securityGroups": [
          "$.network.securityGroups[0]"
        ],
        "storages": [
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
      },
      {
        "name": "my-another-vpc",
        "cidr": "172.32.0.0/16"
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
            "name": "my-app-msg",
            "priority": 1,
            "protocol": "TCP",
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

## Deploy the service

To actually deploy the service, you have to submit the **json file** to the **Open Services Cloud
orchestrator**.

The orchestrator supports several channels:

- CLI
- REST
- SDK

For instance, you can deploy the service descriptor via REST:

```bash
curl -XPOST -d @service.yaml -H "Content-Type: application/json" http://osc.host/path
```
