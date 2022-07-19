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

## Services Integration

### Billing

### Network

### Computing

## Example
