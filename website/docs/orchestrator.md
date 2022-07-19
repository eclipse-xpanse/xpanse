---
sidebar_position: 4
---

# Open Services Cloud orchestrator

Open Services Cloud orchestrator is the central server.

It acts as a gateway taking the service configuration language and dealing with the cloud services to actually deploy the service.

## Installation

The orchestrator is available as a docker image.

You can also create virtual machine and install the orchestrator on it. Then you can easily start the orchestrator with:

```bash
bin/osc start
```

## Configuration

`etc/orchestrator.config.yaml` is the main orchestrator configuration file. It's where you can specify the backend cloud provider.
