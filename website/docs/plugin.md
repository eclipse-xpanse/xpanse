---
sidebar_position: 8
---

# Plugin

This section described how to develop an orchestrator plugin (and associated storage).

## Orchestrator

An orchestrator plugin is basically a class that implements the `org.eclipse.osc.orchestrator.OrchestratorPlugin` interface.
It also implements `org.apache.karaf.minho.boot.spi.Service` interface, to be automatically loaded by the OSC runtime.

You can create a basic plugin Maven `pom.xml` containing the OSC Orchestrator and Apache Karaf Minho dependencies:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>my-group</groupId>
    <artifactId>my-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    
    <dependencies>
        <dependency>
            <groupId>org.eclipse.osc.services</groupId>
            <artifactId>orchestrator</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>org.apache.karaf.minho</groupId>
            <artifactId>minho-boot</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
    </dependencies>
    
</project>
```

Then, you can create your plugin class in the `src/main/java` folder:

```java
public class MyPlugin implements Service, OrchestratorPlugin {
    
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

In order to automatically load the plugin in the OSC runtime, we have to create a `src/main/resources/META-INF/services/org.apache.karaf.minho.boot.spi.Service` containing the plugin full class name to load:

```
MyPlugin
```

## Storage

Most of the time, an orchestrator plugin needs a storage, especially to store the registered service.

This storage has to be persistent on the cloud infrastructure.

Creating an orchestrator storage is similar to the main orchestrator plugin, the only different is that you need to implement `org.eclipse.osc.orchestrator.OrchestratorStorage` interface instead of `OrchestratorPlugin`.

```java
public class MyStorage implements Service, OrchestratorStorage {
    
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

As for an orchestrator plugin, in order to automatically load the storage plugin in the OSC runtime, you have to add the storage plugin full class name in the `META-INF/services/org.apache.karaf.minho.boot.spi.Service` file:

```
MyStorage
```

It means that you will probably have:

```
MyPlugin
MyStorage
```

in the `META-INF/services/org.apache.karaf.minho.boot.spi.Service` file.

## Adding in OSC runtime

Now, you can compile and package your orchestrator plugin and orchestrator storage plugin classes in a jar file.

With Apache Maven, you simply have to do:

```shell
$ mvn clean install
```

Then, you will have `my-plugin-1.0-SNAPSHOT.jar` file in the `target` folder.

To add your plugin to OSC runtime, you simply have to add your plugin jar file into the runtime classpath. For instance, in exploded mode, you simply add the jar in the same folder as the other runtime jars. It will be automatically loaded.