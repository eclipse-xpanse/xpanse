---
sidebar_position: 3
---

# Runtime

Open Services Cloud runtime is the running module. It packages and executes all together: OCL loader, orchestrator, plugins, REST API, ...

## Build

You can easily build OSC yourself.

As requirement, you need:
* a Java Developer Kit (JDK) installed, version 11 or newer. You can use [openjdk](https://openjdk.org/) or [temurin](https://adoptium.net/)
* [Apache Maven 3.8.x or newer](https://maven.apache.org/)

You can clone the project locally on your machine with:

```shell
$ git clone https://github.com/huaweicloud/osc
$ cd osc
```

You can build the whole project with:

```shell
$ mvn clean install
```

It builds the whole project, including the runtime (located in the `runtime/target/runtime` folder).
This runtime is the default one without any plugin.

We provide profiles dedicated to build prepackaged runtimes (including specific plugins):

* Huawei Cloud runtime:

```shell
$ mvn clean install -Phuaweicloud
```

You can build only the runtime (once you built completely the project):

```shell
$ mvn clean install -Phuaweicloud -pl runtime
```

* Openstack runtime

```shell
$ mvn clean install -Popenstack
```

You can build only the runtime (once you built completely the project):

```shell
$ mvn clean install -Popenstack -pl runtime
```

* Kubernetes runtime

```shell
$ mvn clean install -Pk8s
```

You can build only the runtime (once you built completely the project):

```shell
$ mvn clean install -Pk8s -pl runtime
```

***NOTE***: The `huaweicloud`, `openstack`, and `k8s` profiles build the docker images. You can skip docker images creation by adding `-Ddocker.skip=true` to the `mvn` command.

## Launch

### Exploded mode

The runtime is built and available in `runtime/target/runtime` folder by default, where you can directly launch the runtime.

To launch the runtime, go into the exploded folder and launch minho-boot jar:

```shell
$ cd runtime/target/runtime
$ java -jar minho-boot-1.0-SNAPSHOT.jar
```

You can see the log messages:

```shell
...
Jan 02, 2023 6:48:31 AM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFO: Adding minho-rest-service service (1000)
Jan 02, 2023 6:48:31 AM org.apache.karaf.minho.rest.jersey.JerseyRestService onRegister
INFO: Starting minho-rest-service
Jan 02, 2023 6:48:31 AM org.apache.karaf.minho.rest.jersey.JerseyRestService onRegister
INFO:   path: /osc/*
Jan 02, 2023 6:48:31 AM org.apache.karaf.minho.rest.jersey.JerseyRestService onRegister
INFO:   packages: org.eclipse.osc.services.api
Jan 02, 2023 6:48:31 AM org.apache.karaf.minho.web.jetty.JettyWebContainerService addServlet
INFO: Adding servlet java.lang.Class with context /osc/*
Jan 02, 2023 6:48:31 AM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFO: Adding minho-banner-service service (2147483647)
Jan 02, 2023 6:48:31 AM org.apache.karaf.minho.banner.WelcomeBannerService onRegister
INFO: 
  ___                     ____                  _                  ____ _                 _
 / _ \ _ __   ___ _ __   / ___|  ___ _ ____   _(_) ___ ___  ___   / ___| | ___  _   _  __| |
| | | | '_ \ / _ \ '_ \  \___ \ / _ \ '__\ \ / / |/ __/ _ \/ __| | |   | |/ _ \| | | |/ _` |
| |_| | |_) |  __/ | | |  ___) |  __/ |   \ V /| | (_|  __/\__ \ | |___| | (_) | |_| | (_| |
 \___/| .__/ \___|_| |_| |____/ \___|_|    \_/ |_|\___\___||___/  \____|_|\___/ \__,_|\__,_|
      |_|

        Open Services Cloud 1.0.0-SNAPSHOT (2022)

Jan 02, 2023 6:48:31 AM org.apache.karaf.minho.boot.service.ServiceRegistry lambda$start$2
INFO: Starting services
Jan 02, 2023 6:48:31 AM org.apache.karaf.minho.boot.service.LifeCycleService start
INFO: Starting lifecycle service
Jan 02, 2023 6:48:31 AM org.eclipse.jetty.server.Server doStart
INFO: jetty-11.0.12; built: 2022-09-14T02:38:00.723Z; git: d5b8c29485f5f56a14be5f20c2ccce81b93c5555; jvm 11.0.12+8-LTS-237
Jan 02, 2023 6:48:31 AM org.eclipse.jetty.server.session.DefaultSessionIdManager doStart
INFO: Session workerName=node0
Jan 02, 2023 6:48:31 AM org.glassfish.jersey.server.wadl.WadlFeature configure
WARNING: JAXBContext implementation could not be found. WADL feature is disabled.
Jan 02, 2023 6:48:31 AM org.eclipse.jetty.server.handler.ContextHandler doStart
INFO: Started o.e.j.s.ServletContextHandler@108531c2{/,null,AVAILABLE}
Jan 02, 2023 6:48:31 AM org.eclipse.jetty.server.AbstractConnector doStart
INFO: Started ServerConnector@9cb8225{HTTP/1.1, (http/1.1)}{0.0.0.0:8080}
Jan 02, 2023 6:48:31 AM org.eclipse.jetty.server.Server doStart
INFO: Started Server@68f4865{STARTING}[11.0.12,sto=0] @846ms
Jan 02, 2023 6:48:31 AM org.eclipse.osc.orchestrator.OrchestratorService lambda$onRegister$2
INFO: Loading OSC orchestrator plugins
```

The OSC REST API is now available. You can check the status of the runtime by calling the health endpoint on the REST API:

```shell
$ curl -XGET http://localhost:8080/osc/health
ready
```

### Docker

By default, the `huaweicloud`, `openstack`, and  `k8s` profiles build corresponding docker image.

***NOTE***: you can skip docker image creation by adding `-Ddocker.skip=true` argument to the `mvn` command.

You can see the docker images created:

```shell
$ docker images|grep osc
osc/osc-k8s                     1.0.0-SNAPSHOT   4b716096304b   15 seconds ago   293MB
osc/osc-k8s                     latest           4b716096304b   15 seconds ago   293MB
osc/osc-openstack               1.0.0-SNAPSHOT   ffc69547c43f   27 seconds ago   263MB
osc/osc-openstack               latest           ffc69547c43f   27 seconds ago   263MB
osc/osc-huaweicloud             1.0.0-SNAPSHOT   d1a6bb77226c   42 seconds ago   264MB
osc/osc-huaweicloud             latest           d1a6bb77226c   42 seconds ago   264MB
```

You can run a docker container with:

```shell
$ docker run -d --name my-osc -p 8080:8080 osc/osc-k8s
$ docker logs my-osc
...
Jan 02, 2023 5:54:38 AM org.apache.karaf.minho.boot.service.ServiceRegistry add
INFO: Adding minho-banner-service service (2147483647)
Jan 02, 2023 5:54:38 AM org.apache.karaf.minho.banner.WelcomeBannerService onRegister
INFO: 
  ___                     ____                  _                  ____ _                 _
 / _ \ _ __   ___ _ __   / ___|  ___ _ ____   _(_) ___ ___  ___   / ___| | ___  _   _  __| |
| | | | '_ \ / _ \ '_ \  \___ \ / _ \ '__\ \ / / |/ __/ _ \/ __| | |   | |/ _ \| | | |/ _` |
| |_| | |_) |  __/ | | |  ___) |  __/ |   \ V /| | (_|  __/\__ \ | |___| | (_) | |_| | (_| |
 \___/| .__/ \___|_| |_| |____/ \___|_|    \_/ |_|\___\___||___/  \____|_|\___/ \__,_|\__,_|
      |_|

        Open Services Cloud 1.0.0-SNAPSHOT (2022)

Jan 02, 2023 5:54:38 AM org.apache.karaf.minho.boot.service.ServiceRegistry lambda$start$2
INFO: Starting services
Jan 02, 2023 5:54:38 AM org.apache.karaf.minho.boot.service.LifeCycleService start
INFO: Starting lifecycle service
Jan 02, 2023 5:54:38 AM org.eclipse.jetty.server.Server doStart
INFO: jetty-11.0.12; built: 2022-09-14T02:38:00.723Z; git: d5b8c29485f5f56a14be5f20c2ccce81b93c5555; jvm 11.0.17+8
Jan 02, 2023 5:54:38 AM org.eclipse.jetty.server.session.DefaultSessionIdManager doStart
INFO: Session workerName=node0
Jan 02, 2023 5:54:38 AM org.glassfish.jersey.server.wadl.WadlFeature configure
WARNING: JAXBContext implementation could not be found. WADL feature is disabled.
Jan 02, 2023 5:54:38 AM org.eclipse.jetty.server.handler.ContextHandler doStart
INFO: Started o.e.j.s.ServletContextHandler@703feacd{/,null,AVAILABLE}
Jan 02, 2023 5:54:38 AM org.eclipse.jetty.server.AbstractConnector doStart
INFO: Started ServerConnector@16a0ee18{HTTP/1.1, (http/1.1)}{0.0.0.0:8080}
Jan 02, 2023 5:54:38 AM org.eclipse.jetty.server.Server doStart
INFO: Started Server@5032714f{STARTING}[11.0.12,sto=0] @1007ms
Jan 02, 2023 5:54:38 AM org.eclipse.osc.orchestrator.OrchestratorService lambda$onRegister$2
INFO: Loading OSC orchestrator plugins
```

The OSC REST API is now available. You can check the status of the runtime by calling the health endpoint on the REST API:

```shell
$ curl -XGET http://localhost:8080/osc/health
ready
```

### Kubernetes

OSC provides all Kubernetes manifest files in the `runtime/src/main/kubernetes` folder, allowing you to deploy all resources on your Kubernetes cluster.

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

Then, you create the `osc-config` configmap in Kubernetes:

```shell
$ kubectl apply -f runtime/src/main/kubernetes/org.eclipse.osc.configmap.yaml 
configmap/osc-config created
```

You can now create the OSC runtime deployment in Kubernetes:

```shell
$ kubectl apply -f runtime/src/main/kubernetes/org.eclipse.osc.deployment.k8s.yaml 
deployment.apps/osc created
```

You can check the status of the deployment:

```shell
$ kubectl get deployments -n osc
NAME   READY   UP-TO-DATE   AVAILABLE   AGE
osc    1/1     1            1           20s
```

and the associated pod running:

```shell
$ kubectl get pods -n osc
NAME                   READY   STATUS    RESTARTS   AGE
osc-8699fd7547-vj44p   1/1     Running   0          42s
```

We can see the logs of the running pod:

```shell
$ kubectl logs osc-8699fd7547-vj44p -n osc
...
Jan 02, 2023 6:26:37 AM org.apache.karaf.minho.banner.WelcomeBannerService onRegister
INFO: 
  ___                     ____                  _                  ____ _                 _
 / _ \ _ __   ___ _ __   / ___|  ___ _ ____   _(_) ___ ___  ___   / ___| | ___  _   _  __| |
| | | | '_ \ / _ \ '_ \  \___ \ / _ \ '__\ \ / / |/ __/ _ \/ __| | |   | |/ _ \| | | |/ _` |
| |_| | |_) |  __/ | | |  ___) |  __/ |   \ V /| | (_|  __/\__ \ | |___| | (_) | |_| | (_| |
 \___/| .__/ \___|_| |_| |____/ \___|_|    \_/ |_|\___\___||___/  \____|_|\___/ \__,_|\__,_|
      |_|

        Open Services Cloud 1.0.0-SNAPSHOT (2022)

Jan 02, 2023 6:26:37 AM org.apache.karaf.minho.boot.service.ServiceRegistry lambda$start$2
INFO: Starting services
Jan 02, 2023 6:26:37 AM org.apache.karaf.minho.boot.service.LifeCycleService start
INFO: Starting lifecycle service
Jan 02, 2023 6:26:37 AM org.eclipse.jetty.server.Server doStart
INFO: jetty-11.0.12; built: 2022-09-14T02:38:00.723Z; git: d5b8c29485f5f56a14be5f20c2ccce81b93c5555; jvm 11.0.17+8
Jan 02, 2023 6:26:37 AM org.eclipse.jetty.server.session.DefaultSessionIdManager doStart
INFO: Session workerName=node0
Jan 02, 2023 6:26:37 AM org.glassfish.jersey.server.wadl.WadlFeature configure
WARNING: JAXBContext implementation could not be found. WADL feature is disabled.
Jan 02, 2023 6:26:37 AM org.eclipse.jetty.server.handler.ContextHandler doStart
INFO: Started o.e.j.s.ServletContextHandler@703feacd{/,null,AVAILABLE}
Jan 02, 2023 6:26:37 AM org.eclipse.jetty.server.AbstractConnector doStart
INFO: Started ServerConnector@16a0ee18{HTTP/1.1, (http/1.1)}{0.0.0.0:8080}
Jan 02, 2023 6:26:37 AM org.eclipse.jetty.server.Server doStart
INFO: Started Server@5032714f{STARTING}[11.0.12,sto=0] @858ms
Jan 02, 2023 6:26:37 AM org.eclipse.osc.orchestrator.OrchestratorService lambda$onRegister$2
INFO: Loading OSC orchestrator plugins
```

We can now deploy the service exposing the port 8080 of the OSC runtime:

```shell
$ kubectl apply -f runtime/src/main/kubernetes/org.eclipse.osc.service.yaml 
service/osc created
```

and check that the port is currently bound on the NodePort:

```shell
$ kubectl get services -n osc
NAME   TYPE       CLUSTER-IP    EXTERNAL-IP   PORT(S)          AGE
osc    NodePort   10.97.81.86   <none>        8080:32228/TCP   13s
```

We can now access the health endpoint of the OSC REST API:

```shell
$ minikube service list -n osc
|-----------|------|-------------|---------------------------|
| NAMESPACE | NAME | TARGET PORT |            URL            |
|-----------|------|-------------|---------------------------|
| osc       | osc  |        8080 | http://192.168.49.2:32228 |
|-----------|------|-------------|---------------------------|
```

You can now point test the health endpoint:

```shell
$ curl -XGET http://192.168.49.2:32228/osc/health
ready
```

OSC is now ready on your Kubernetes cluster.