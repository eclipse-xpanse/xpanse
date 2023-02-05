# Open Services Cloud Demo

The goal of Open Services Cloud demo is to illustrate Open Services Cloud values, and how it can look like from users
standpoint.

The demo shows the benefits of Open Services Cloud regarding the different cloud personas:

* the end user, who can use the same services on different cloud providers (multi-cloud), with seamless integration with
  cloud providers service (monitoring, billing, ...)
* the software vendor, who can describe his software with OCL (Open Services Cloud Configuration Language), and deploy
  his software as native managed service on any cloud provider
* the cloud provider, who can extend his service catalog by easily integrating OCL described services

## Build & Run

The demo uses [reactjs](https://reactjs.org/) and [ant design](https://ant.design/).

You basically have two ways to build and run the demo:

* using [nodejs](https://nodejs.org/en/)
* using [docker](https://www.docker.com/)

### Nodejs

To build and run locally on your machine, you have to install [nodejs](https://nodejs.org/en/) on your machine,
with `npm` or `yarn` command in your `PATH`.

First, you have to download and install the dependencies:

```shell
$ npm install
```

_or_

```shell
$ yarn install
```

You are now ready to run the demo directly on your machine:

```shell
$ npm start
```

_or_

```shell
$ yarn start
```

You can now access the demo with your browser on `http://localhost:30000`.

Optionally, you can generate a static website for the demo with:

```shell
$ npm build
```

_or_

```shell
$ yarn build
```

The demo static content is located in the `build` or `target/classes/META-INF/resources` (depending of your environment)
folder. You can copy this folder in any web server (Apache httpd, nginx, ...).

### Docker

The demo also includes a `Dockerfile` to both build and package the demo as a Docker image. With this approach you don't
need [nodejs](https://nodejs.org) installed on your machine (neither `npm` or `yarn` commands), you just need a Docker
daemon running locally.

The Docker build fetch a nodejs Docker image to build the demo (static) and package a Docker image running the demo
with `nginx`.

To build `osc-demo` Docker image on your machine, you just have to do:

```shell
$ docker build -t osc-demo .
```

You now have a `osc-demo` Docker image in your local Docker registry. You can run a Docker container with:

```shell
$ docker run --name osc-demo -p 80:80 osc-demo
```

You can access the demo on `http://localhost:80`.

#### Kubernetes

It's also possible to deploy the demo on a Kubernetes cluster. The demo provides the K8S manifest in the `kubernetes`
folder.

Once you built the demo Docker image, you can tag and push the image on a registry where your Kubernetes cluster can
retrieve images from.

For instance, you can push on minikube registry with tag (assuming minikube is running on `192.168.49.2`):

```shell
$ docker tag osc-demo 192.168.49.2:5000/osc/osc-demo:0.1
$ docker push 192.168.49.2:5000/osc/osc-demo:0.1
```

You can verify the content of `kubernetes/osc-demo.yml` to see if the container image match your Docker registry.

Then, you can deploy with:

```shell
$ kubectl apply -f kubernetes/osc-demo.yml
```

You can check if the demo pods are running:

```shell
$ kubectl get pods
NAME                        READY   STATUS    RESTARTS   AGE
osc-demo-6bfd4ff768-m242x   1/1     Running   0          8s
osc-demo-6bfd4ff768-tdphp   1/1     Running   0          9s
osc-demo-6bfd4ff768-txddd   1/1     Running   0          6s
```

and the demo service:

```shell
$ kubectl get service
NAME         TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE
osc-demo     NodePort    10.103.125.208   <none>        80:32016/TCP   6d5h
```

You can now access the demo via the K8S service. For instance, with minikube, you can do:

```shell
$ minikube service osc-demo
|-----------|----------|-------------|---------------------------|
| NAMESPACE |   NAME   | TARGET PORT |            URL            |
|-----------|----------|-------------|---------------------------|
| default   | osc-demo |          80 | http://192.168.49.2:32016 |
|-----------|----------|-------------|---------------------------|
🎉  Opening service default/osc-demo in default browser...
```

## Use & demo

The demo uses different users to illustrate different perspectives depending of the persona.

You can see the demo on [youtube](https://youtu.be/P5C4z12YUGI).