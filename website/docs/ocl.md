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

## Services Integration

### Billing

The `billing` component defines the integration into cloud provider billing system.

You can configure the business model associated to the service:

* `model` defines the business model (`flat`, `exponential`, ...)
* `period` defines the rental period (`daily`, `weekly`, `monthly`, `yearly`)
* `currency` defines the billing currency (`euro`, `usd`, ...)
* `fixedPrice` is the fixed price during the period (the price applied one shot whatever is the service use)
* `variablePrice` is the price depending of item volume
* `variableItem` is the item used to calculate the variable price on the period (for instance, the number of instances, the number of transactions, ...)

### Compute

#### VM

This is the list of VMs used by the service. Each VM has:

* `name` is the name of the VM
* `type` is the VM type, like `t2.large` for example
* `platform` is the VM image/platform, like `linux-x64` (to be generic)
* `vpc` is the name of the VPC defined in the `network` section
* `subnet` is the name of the Subnet defined in the `network` section
* `security` is the name of the security defined in the `network` section
* `storage` is the name of the storage defined in the `storage` section
* `publicly` is a flag to indicate if the VM should be exposed on Internet (`true`) or only local (`false`)

### Network

#### VPC

This is the list of VPC defined in the service network. Each VPC has:

* `name` is the name of VPC, used in other OCL elements
* `cidrs` is a the VPN IP address range
* `routes` is the routing policy for this VPC
* `acl` defines the Access Control List for this VPC

#### Subnet

This is the list of subnet defined in the service network. Each subnet has:

* `name` is the name of the subnet, used in other OCL elements
* `vpc` is the name of the VPC where the subnet is attached
* `table` is the subnet table
* `routes` is the routing policy for this subnet

#### Security

This is the list of security (groups) defined in the service network. Each security has:

* `name` is the name of the security, used in other OCL elements
* `inbound` is the port mapping/forwarding inbound (using format like `22->22`)
* `outbound` is the port mapping/forwarding outbound (using format like `22->22`)

### Storage

### Administration Console

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
  "billing": {
    "model": "flat",
    "period": "monthly",
    "currency": "euro",
    "fixedPrice": 20,
    "variablePrice": 10,
    "variableItem": "instance"
  },
  "compute": {
    "vm": [{
      "name": "my-vm",
      "type": "t2.large",
      "platform": "linux-x64",
      "vpc": "my-vpc",
      "subnet": "my-subnet",
      "security": "my-sg",
      "storage": "my-storage",
      "publicly": true
    }]
  },
  "network": {
    "vpc": [{
      "name": "my-vpc",
      "cidrs": "172.31.0.0/16",
      "routes": "",
      "acl": ""
    }],
    "subnet": [{
      "name": "my-subnet",
      "vpc": "my-vpc",
      "table": "",
      "routes": ""
    }],
    "security": [{
      "name": "my-sg",
      "inbound": [ "22->22", "443->443", "80->80" ],
      "outbound": []
    }]
  },
  "storage": [{
    "name": "my-storage",
    "type": "ssd",
    "size": "8GiB" 
  }]
}
```

## Terraform Provider

OCL is also available as Terraform Provider, supporting the same details as above.
