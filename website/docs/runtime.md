---
sidebar_position: 4
---

# Runtime

Open Services Cloud runtime is the running module. It packages and executes all together: OCL loader, orchestrator,
plugins, REST API, ...

## Build

You can easily build Xpanse yourself.

As requirement, you need:

* a Java Developer Kit (JDK) installed, version 17 or newer. You can use [openjdk](https://openjdk.org/)
  or [temurin](https://adoptium.net/)
* [Apache Maven 3.8.x or newer](https://maven.apache.org/)

You can clone the project locally on your machine with:

```shell
$ git clone https://github.com/huaweicloud/xpanse
$ cd xpanse
```

First, you can build the whole xpanse project, including all modules (orchestrator, OCL, runtime, plugins, etc), simply
with:

```shell
$ mvn clean install
```

### Run

By default, the application will not activate any plugins. They must be activated via spring profiles. Also ensure that
only one plugin is active at a time.

* for Huawei Cloud:

```shell
$ cd runtime/target
$ java -jar xpanse-runtime-1.0.0-SNAPSHOT.jar -Dspring.profiles.active=huaweicloud
```

* for Openstack:

```shell
$ cd runtime/target
$ java -jar xpanse-runtime-1.0.0-SNAPSHOT.jar -Dspring.profiles.active=openstack
```

By default, the runtime is built in "exploded mode". Additionally, you can also build a Docker image
adding `-Ddocker.skip=false` as build argument:

```shell
$ cd runtime
$ mvn clean install -Ddocker.skip=false
```

We can start xpanse runtime with a specific plugin by passing the plugin name in the profile name. For example to start
huaweicloud

```shell
$ docker run -e "SPRING_PROFILES_ACTIVE=huaweicloud" --name my-osc-runtime osc
```

```

You can see the log messages:

```shell
  ___                     ____                  _                  ____ _                 _
 / _ \ _ __   ___ _ __   / ___|  ___ _ ____   _(_) ___ ___  ___   / ___| | ___  _   _  __| |
| | | | '_ \ / _ \ '_ \  \___ \ / _ \ '__\ \ / / |/ __/ _ \/ __| | |   | |/ _ \| | | |/ _` |
| |_| | |_) |  __/ | | |  ___) |  __/ |   \ V /| | (_|  __/\__ \ | |___| | (_) | |_| | (_| |
 \___/| .__/ \___|_| |_| |____/ \___|_|    \_/ |_|\___\___||___/  \____|_|\___/ \__,_|\__,_|
      |_|

        Open Services Cloud 1.0.0-SNAPSHOT (2023)
11:14:08.843 [main] INFO  o.eclipse.osc.runtime.OscApplication - Starting OscApplication using Java 17.0.5 with PID 20664
11:14:08.849 [main] INFO  o.eclipse.osc.runtime.OscApplication - No active profile set, falling back to 1 default profile: "default"
11:14:10.600 [main] INFO  o.e.o.o.FileOrchestratorStorage - No other storage beans found. Using default file storage.
11:14:11.641 [main] WARN  o.e.o.o.OrchestratorService - No OSC plugins are available
11:14:11.644 [main] INFO  o.eclipse.osc.runtime.OscApplication - Started OscApplication in 3.252 seconds (process running for 3.818)

```

The Xpanse REST API is now available. You can check the status of the runtime by calling the health endpoint on the REST
API:

```shell
$ curl -XGET http://localhost:8080/xpanse/health
ready
```

### Docker

If you use `-Ddocker.skip=false` as option on the build command line, you have docker image ready for the runtime.

You can see the docker images created:

```shell
$ docker images|grep xpanse
xpanse                   1.0.0-SNAPSHOT   4b716096304b   15 seconds ago   293MB
xpanse                   latest           4b716096304b   15 seconds ago   293MB
```

You can run a docker container with:

```shell
$ docker run -d --name my-xpanse -p 8080:8080 xpanse
$ docker logs my-xpanse
...
   _  __   ____    ____ _   ____    _____  ___
  | |/_/  / __ \  / __ `/  / __ \  / ___/ / _ \
 _>  <   / /_/ / / /_/ /  / / / / (__  ) /  __/
/_/|_|  / .___/  \__,_/  /_/ /_/ /____/  \___/
       /_/

        xpanse 1.0.0-SNAPSHOT (2023)
13:44:19.633 [main] INFO  o.e.xpanse.runtime.XpanseApplication - Starting XpanseApplication using Java 17.0.5 with PID 7344
13:44:19.645 [main] INFO  o.e.xpanse.runtime.XpanseApplication - No active profile set, falling back to 1 default profile: "default"
13:44:22.211 [main] INFO  o.e.x.o.FileOrchestratorStorage - No other storage beans found. Using default file storage.
13:44:23.878 [main] WARN  o.e.x.o.OrchestratorService - No xpanse plugins loaded by the runtime.
13:44:23.886 [main] INFO  o.e.xpanse.runtime.XpanseApplication - Started XpanseApplication in 5.029 seconds (process running for 5.992)
```

The OSC REST API is now available. You can check the status of the runtime by calling the health endpoint on the REST
API:

```shell
$ curl -XGET http://localhost:8080/xpanse/health
ready
```

### Kubernetes

OSC provides all Kubernetes manifest files in the `runtime/src/main/kubernetes` folder, allowing you to deploy all
resources on your Kubernetes cluster.

As example, you can deploy on local `minikube` instance. First, start your `minikube` instance:

```shell
$ minikube start
$ eval $(minikube -p minikube docker-env)
```

First, you create the `osc` namespace in Kubernetes:

```shell
$ kubectl apply -f runtime/src/main/kubernetes/org.eclipse.osc.namespace.yaml
namespace/osc created
```

You can now create the OSC runtime deployment in Kubernetes:

```shell
$ kubectl apply -f runtime/src/main/kubernetes/org.eclipse.osc.deployment.k8s.yaml 
deployment.apps/osc created
```

You can check the status of the deployment:

```shell
$ kubectl get deployments -n xpanse
NAME   READY   UP-TO-DATE   AVAILABLE   AGE
xpanse    1/1     1            1           20s
```

and the associated pod running:

```shell
$ kubectl get pods -n xpanse
NAME                   READY   STATUS    RESTARTS   AGE
osc-8699fd7547-vj44p   1/1     Running   0          42s
```

```

We can now deploy the service exposing the port 8080 of the OSC runtime:

```shell
$ kubectl apply -f runtime/src/main/kubernetes/org.eclipse.osc.service.yaml 
service/xpanse created
```

and check that the port is currently bound on the NodePort:

```shell
$ kubectl get services -n xpanse
NAME   TYPE       CLUSTER-IP    EXTERNAL-IP   PORT(S)          AGE
osc    NodePort   10.97.81.86   <none>        8080:32228/TCP   13s
```

We can now access the health endpoint of the OSC REST API:

```shell
$ minikube service list -n xpanse
|-----------|------|-------------|---------------------------|
| NAMESPACE | NAME | TARGET PORT |            URL            |
|-----------|------|-------------|---------------------------|
| osc       | osc  |        8080 | http://192.168.49.2:32228 |
|-----------|------|-------------|---------------------------|
```

You can now point test the health endpoint:

```shell
$ curl -XGET http://192.168.49.2:32228/xpanse/health
ready
```

Xpanse is now ready on your Kubernetes cluster.