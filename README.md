# Open Services Cloud

Open Services Cloud is an Open Source project allowing to easily implement native managed service on any cloud service provider.

Open Services Cloud unleash your cloud services by removing vendor lock-in and lock out. It standardizes and exposes cloud service providers core services, meaning that your Open Services Cloud service is portable (multi-cloud) on any cloud topology and provider.
It also avoids tight coupling of your service to other cloud service provider services.

## APIs (core services)

Open Services Cloud interacts directly with the fundamental APIs used by the cloud service provider to create managed service:

* **identity** deling with access, users, groups, roles, ...
* **computing** abstracts the manipulation of virtual machines
* **storage** abstracts the manipulation of storage volumes
* **vpc** abstracts the manipulation of network devices
* **billing** registers the business model in the cloud provider billing system
* **console** plugin UI components for the service into the cloud provider console 
* ...

## Configuration Language

A managed service is described using Open Services Cloud Configuration Language (OCL).

OCL is a json descriptor of a managed service, describing the expected final state of your service, interacting with the fundamental APIs:

```json
{
  "name": "my-service",
  "category": "compute",
  "namespace": "my-namespace",
  "properties": {
    "meta": "data",
    "other": true
  },
  "image": {
    "provisioners": [
      {
        "name": "my-kafka-release",
        "type": "shell",
        "environments": [
          "WORK_HOME=/usr1/KAFKA/"
        ],
        "inline": [
          "cd ${WORK_HOME} && wget http://xxxx/kafka/release.jar"
        ]
      }
    ],
    "base": [
      {
        "name": "ubuntu-x64",
        "type": "t2.large",
        "filters": {
          "name": "ubuntu-for-osc-*"
        }
      }
    ],
    "artifacts": [
      {
        "name": "kafka_image",
        "base": "$.image.base[0]",
        "provisioners": [
          "$.image.provisioners[0]"
        ]
      }
    ]
  },
  "billing": {
    "model": "flat",
    "period": "monthly",
    "currency": "euro",
    "fixedPrice": 20,
    "variablePrice": 10,
    "variableItem": "instance",
    "backend": "https://software_provider/billing/backend",
    "properties": {
      "billing_prop": "value"
    }
  },
  "compute": {
    "vm": [
      {
        "name": "my-vm",
        "type": "t2.large",
        "image": "$.image.artifacts[0]",
        "subnet": [
          "$.network.subnet[0]"
        ],
        "security": [
          "$.network.security[0]"
        ],
        "storage": [
          "$.storage[0]"
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
    "subnet": [
      {
        "name": "my-subnet",
        "vpc": "$.network.vpc[0]",
        "cidr": "172.31.1.0/24"
      }
    ],
    "security": [
      {
        "name": "my-sg",
        "rules": [
          {
            "name": "my-remote-desktop",
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
  "storage": [{
    "name": "my-storage",
    "type": "ssd",
    "size": "8GiB"
  }],
  "console": {
    "backend": "https://...",
    "properties": {
      "one": "two"
    }
  }
}
```

## OCL loading

Open Services Cloud provides different options to generate and provision OCL:

* REST API on the Open Services Cloud runtime
* CLI allowing to directly interact with Open Services Cloud via command line
* language frontend (SDL) for Java, Python, ...

## Orchestrator & binding

OCL descriptor is an abstract description of the final managed service state. It's generic enough to work with any cloud service provider.

Open Services Cloud runtime embeds an orchestrator responsible:

1. to generate the graph of actions required to reach the final expected state
2. the graph generation depends on the underlying cloud provider
3. the orchestrator has different bindings (pluggable), each binding converts OCL definition to the concrete cloud provider APIs calls

## Runtime

Open Services CLoud runtime is the overall component running on the cloud provider.

The runtime embeds and run together:

1. the orchestrator with the different bindings
2. the OCL loader and parser
3. the frontends (REST API, ...)
