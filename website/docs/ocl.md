---
sidebar_position: 2
---

# Configuration Language

The Open Services Cloud Configuration Language allows you to describe your service and interaction with fundamentals services (computing, network, billing, ...).

## Metadata

The first part of the OCL service descriptor is basically service metadata:

* `name` is the name of your service, used to identify the service in the CSP catalog, user console, etc
* `category` is the overall category of the service. It's especially use to integrate the service in the right menu of the user console.
* `namespace` is the location of the service. It could be in a CSP subdomain, in a region, and any kind of CSP classification.
* `properties` is all metadata (key/value pairs) for the managed services. These properties are used by the OSC Orchestrator.
* `artifacts` is the list of artifacts used to bootstrap the managed service.

An artifact is described with:
* `name` identifies the artifact
* `type` defines the artifact type (jar, deb, docker, rpm, ...)
* `url` is the location (URL) of the artifact (https, file, mvn, ...)
* `properties` is a list of properties (metadata) used for bootstrapping and deployment

## Services Integration

### Image

#### Provisioners

This is the list of provisioners used by the `image.artifacts`. Each provisioner has:

* `name` - (Required) The name of the provisioner.
* `type` - (Required) The type of the provisioner. Possible values include: `shell`(more type will be coming).
* `environments` - (Required) The variables for provisioner.
* `inline` - (Required) The inline attributes for provisioner.

```json
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
```

This block defines a `shell` provisioner which set an environment variable named `WORK_HOME` in the shell execution environment and runs the commands in the `inline` attribute.

#### `image.base`

This is the list of base image used by the `image.artifacts`. Each `image.base` has:

* `name` - (Required) The name of the `image.base`.
* `type` - (Required) The type(flavor) of vm, which the image will be used by.
* `filters` - (Required) The filters to lock the image.

##### `image.base.filters`

* `name` - (Optional) The name to filter the image.
* `id` - (Optional) The identifier to lock the image.

#### Artifacts

This is the list of artifact image. Each `image.base` has:

* `name` - (Required) The name of the artifact image.
* `base` - (Required) The `JsonPath` of the base images, which the artifact image will be based.
* `provisioners` - (Required) The `JsonPath` list of the provisioners to build the artifact image.

### Billing

The `billing` component defines the integration into cloud provider billing system.

You can configure the business model associated to the service:

* `model` defines the business model (`flat`, `exponential`, ...)
* `period` defines the rental period (`daily`, `weekly`, `monthly`, `yearly`)
* `currency` defines the billing currency (`euro`, `usd`, ...)
* `fixedPrice` is the fixed price during the period (the price applied one shot whatever is the service use)
* `variablePrice` is the price depending of item volume
* `variableItem` is the item used to calculate the variable price on the period (for instance, the number of instances, the number of transactions, ...)
* `backend` is the software provider (managed service provider) billing backend system used for payback.
* `properties` is all properties related to billing, they are used by OSC for payback and bootstrapping.

### Compute

#### VM

This is the list of VMs used by the service. Each VM has:

* `name` - (Required) The name of the VM.
* `type` - (Required) The VM type, like `t2.large` for example.
* `subnet` - (Required) A list of `JsonPath` of the `subnet`. The subnet for main Nic must be in the first place.
* `security` - (Required) A list of `JsonPath` of `security`.
* `storage` - (Required) A list of `JsonPath` of the `storage`.
* `publicly` - (Required) The flag to indicate if the VM should be exposed on Internet (`true`) or only local (`false`).

### Network

#### VPC

This is the list of VPC defined in the service network. Each VPC has:

* `name` - (Required) The name of VPC, used in other OCL elements
* `cidr` - (Required) The VPN IP address range.

#### Subnet

This is the list of subnet defined in the service network. Each subnet has:

* `name` - (Required) The name of the subnet, used in other OCL elements.
* `vpc` - (Required) The `JsonPath` of the VPC where the subnet is attached.
* `cidr` - (Required) The subnet IP address range.

#### Security

This is the list of security (groups) defined in the service network. Each security has:

* `name` - (Required) The name of the security, used in other OCL elements.
* `rules` - (Required) The list of security rule.

##### SecurityRule <span id = "SecurityRule">

* `name` - (Required) The name of the security rule, define in `security` (groups).
* `priority` - (Required) The priority of the security rule. The lower the priority number, the higher the priority of the rule.
* `protocol` - (Required) Network protocol this rule applies to. Possible values include: `Tcp`,`Udp`,`Icmp`,`*`(which matches all).
* `cidr` - (Required) The IP address range this rule applies to. The `*` matches any IP.
* `direction` - (Required) The direction of the network traffic. Possible values include: `inbound`,`outbound`.
* `ports` - (Required) (Optional) Specifies the port value range, which supports single port (80), continuous port (1-30) and discontinuous port (22, 3389, 80) The valid port values is range form 1 to 65,535.
* `action` - (Required) Specifies whether network traffic is allowed or denied. Possible values include: `allow`,`deny`.

### Storage

### Administration Console

`console` element described the integration with CSP admin console, with:

* `backend` is the admin console backend API URL
* `properties` is key/value pairs used by OSC for the console integration

### Observability & Tracing

#### Logging

#### Tracing

#### Metrics (Gauge, ...)

### Identity Management

### Baseline

## Example

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
        "environment_vars": [
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
      },
      {
        "name": "centos-x64",
        "type": "t2.large",
        "filters": {
          "id": "ed2c9ea6-7134-44b9-bbfa-109e0753766e"
        }
      }
    ],
    "artifacts": [
      {
        "name": "kafka_image",
        "base": "$.image.base[0]",
        "provisioners": ["$.image.provisioners[0]"]
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
      },
      {
        "name": "my-another-vpc",
        "cidr": "172.32.0.0/16"
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
  "storage": [
    {
      "name": "my-storage",
      "type": "ssd",
      "size": "8GiB"
    }
  ],
  "console": {
    "backend": "https://...",
    "properties": {
      "one": "two"
    }
  }
}
```

## Terraform Provider

OCL is also available as Terraform Provider, supporting the same details as above.

```hcl
resource "osc_service" "myservice" {
  name      = "my-service"
  category  = "compute"
  namespace = "my-namespace"
  artifacts = ["mvn:groupId/artifactId/version", "https://host/path/to/artifact"]

  billing {
    model         = "flat"
    period        = "monthly"
    currency      = "euro"
    fixedPrice    = 20
    variablePrice = 10
    variableItem  = "instance"
  }

  compute {
    vm {
      name     = "my-vm"
      type     = "t2.large"
      platform = "linux-x64"
      vpc      = "my-vpc"
      subnet   = "my-subnet"
      security = "my-sg"
      storage  = "my-storage"
      publicly = true
    }
  }

  network {
    vpc {
      name   = "my-vpc"
      cidrs  = "172.31.0.0/16"
      routes = ""
      acl    = ""
    }

    subnet {
      name   = "my-subnet"
      vpc    = "my-vpc"
      table  = ""
      routes = ""
    }

    security {
      name     = "my-sg"
      inbound  = ["22->22", "443->443", "80->80"]
      outbound = []
    }
  }

  storage {
    name = "my-storage"
    type = "ssd"
    size = "8GiB"
  }
}
```
