# Eclipse Open Services Cloud

Eclipse Open Services Cloud is an Open Source project allowing to easily implement native managed service on any cloud service provider.

Eclipse Open Services Cloud unleash your cloud services by removing vendor lock-in and lock out. It standardizes and exposes cloud service providers core services, meaning that your Open Services Cloud service is portable (multi-cloud) on any cloud topology and provider.
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

OCL is a yaml descriptor of a managed service, describing the expected final state of your service, interacting with the fundamental APIs:

```yaml
osc {
 version = ">= 0.0.1"
}
billing {
  model = "flat"
  period = "monthly"
  pricing = "10"
}
console {
  name "My Service"
  logo "http://..."
  frame "..".
}
network {
  vpc "myvpc" {
    name = "myvpc"
    cidr = "192.168.1.0/24"
  }
  subnet "mysubnet" {
    vpc = "myvpc"
    name = "mysubnet"
    cidr = "192.168.1.0/26"
    gateway = "192.168.1.1"
  }
  loadbalancer "mylb" {
    algorithm = "round-robin"
    port = 80
    protocol = "http"
    members = "mynode"
  }
}
computing {
  node "mynode" {
    image = "myimage"
    size = "t1.micro"
    network = "mysubnet"
  }
}
dns {
    entry "a" {
        name "a"
        domain = "my-domain"
        nodes = "mynode"
    }
}
storage {
   blob "myblobstorage" {
     
   }
}
container {
    cluster = "my-cluster"
    image = "docker-image"
    replica = 10
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
