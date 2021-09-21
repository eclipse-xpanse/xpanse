# Eclipse OpenServicesCloud

Eclipse OpenServicesCloud is framework providing a set of common API implemented by any OSC cloud provider.

The purpose of this framework is to provide a standardized and common access layer to the cloud services engines.

## Why?

As OSC is an open source effort and a common layer, there's no lock-in on the clud engines. It facilitates and standardizes access to the cloud engines, allowing users to create multi-cloud services, whatever the services are running on public, private, or hybrid cloud platforms.

On the other hand, there's no lock-out, meaning that any ecosystem can easily create managed cloud services without being coupled to a single cloud engine.

## APIs

Eclipse OSC provides the following APIs:

* identity to deal with cloud engine access, users, groups, roles management
* computing machine abstract the access to virtual machines
* ...

NB: OSC doesn't cover cloud engine specific services like storages or filesystems for instance.

## TCK

The framework provides a TCK to "verify" the OSC compliance.

## Simple Reference Implementation

The framework provides a very simple reference implementation, local runner, allowing you to develop and test your services on OCS.

This reference implementation is powered by Apache Karaf 5.0.0 as runtime and Apache jClouds as abstraction layer.

## OSC compliant cloud engines

Currently the known OSC compliant cloud engines are:

* Huawei Cloud
* ...
