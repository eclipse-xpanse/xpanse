---
sidebar_position: 8
---

# Plugin

This section described how to develop an orchestrator plugin (and associated storage).

## Orchestrator

An orchestrator plugin is basically a class that implements the `org.eclipse.osc.orchestrator.OrchestratorPlugin` interface.
It must also be annotated with `@Component` so that the class is scanned and loaded into the spring context.

You can create a basic plugin Maven `pom.xml` containing the OSC Orchestrator and OclLoader:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>my-group</groupId>
    <artifactId>my-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    
    <dependencies>
        <dependency>
            <groupId>org.eclipse.osc.modules</groupId>
            <artifactId>orchestrator</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.eclipse.osc.modules</groupId>
            <artifactId>ocl-loader</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>
    
</project>
```

Then, you can create your plugin class in the `src/main/java` folder:

```java
public class MyPlugin implements OrchestratorPlugin {
    
    @Override
    public void registerManagedService(Ocl ocl) {
        // load the OCL and register the corresponding managed service (creating all required resources)
    }

    @Override
    public void updateManagedService(String managedServiceName, Ocl ocl) {
        // update an existing service with a new OCL descriptor
    }

    @Override
    public void startManagedService(String managedServiceName) {
        // start a registered service
    }

    @Override
    public void stopManagedService(String managedServiceName) {
        // stop a registered service
    }

    @Override
    public void unregisterManagedService(String managedServiceName) {
        // unregister (remove and destroy service resources)
    }
    
    // optional method, only needed if you want to execute code when the plugin is loaded
    @Override
    public void onRegister(ServiceRegistry serviceRegistry) {
        // here you can interact with other services (via ServiceRegistry) in the OSC runtime
    }
}
```

## Storage

Most of the time, an orchestrator plugin needs a storage, especially to store the registered service.

This storage has to be persistent on the cloud infrastructure.

Creating an orchestrator storage is similar to the main orchestrator plugin, the only different is that you need to implement `org.eclipse.osc.orchestrator.OrchestratorStorage` interface instead of `OrchestratorPlugin`.
It must also be annotated with `@Component` so that the class is scanned and loaded into the spring context.

```java
public class MyStorage implements OrchestratorStorage {
    
    @Override
    public void store(String sid) {
        // store a service ID
    }

    @Overrde
    public void store(String sid, String pluginName, String key, String value) {
        // store a service ID with associated key/value pair
    }

    @Override
    public String getKey(String sid, String pluginName, String key) {
        // get the key corresponding to the service id
    }

    @Override
    public boolean exists(String sid) {
        // return true if the service ID is present in the store, false else
    }

    @Override
    public Set<String> services() {
        // return the list of services (ID) present in the store 
    }

    @Override
    public void remove(String sid) {
        // remove a service (ID) from the store
    }
}
```

## Adding in OSC runtime

To ensure the plugin and storage implementations are correctly scanned and loaded, both the classes must also be additionally annotated wit  `@Profile` and with value with the plugin name.

```@Profile(value = "${pluginName})```
