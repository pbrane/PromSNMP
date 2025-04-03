# PromSNMP [![promsnmp-build](https://github.com/pbrane/PromSNMP/actions/workflows/promsnmp-build.yaml/badge.svg)](https://github.com/pbrane/PromSNMP/actions/workflows/promsnmp-build.yaml)

![promSnmpCast](https://github.com/user-attachments/assets/13e0b6a7-6fe7-49f0-9e98-726e736e1370)

## 👩‍🏭 Build from source

Check out the source code with

```shell
git clone https://github.com/pbrane/PromSNMP.git
```

Compile and assemble the JAR file including the test suite

```shell
make
```

Build a Docker container image in your local registry

```shell
make oci
```

## 🕹️ Run the application

Start the application locally

```shell
java -jar target/promsnmp-*.jar
```

Start the application using Docker

```shell
docker run -it --init --rm -p "8082:8080/tcp" local/promsnmp:$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
```
## 🔌 API Endpoints

The application exposes the following endpoints:

| Endpoint                     | Method | Description                                                          |
|------------------------------|--------|----------------------------------------------------------------------|
| `/promSnmp/hello`            | GET    | Returns a simple "Hello World" response                              |
| `/promSnmp/sample`           | GET    | Returns sample Prometheus metrics from static file                   |
| `/promSnmp/router`           | GET    | Returns router metrics from static file                              |

## 🎢 Deployment playground

You can find in the development folder a stack with Prometheus and Grafana.

```shell
cd deployment
docker compose up -d
```
Endpoints:
* Grafana: http://localhost:3000, login admin, password admin
* Prometheus: http://localhost:9090
* PromSNMP: http://localhost:8080

## Create and publish a new release

To make a release the following steps are required:

1. Set the Maven project version without -SNAPSHOT
2. Make a version tag with git
3. Set a new SNAPSHOT version in the main branch
4. Publish a release 

To help you with these steps you can run a make goal `make release`.
It requires a version number you want to release.
As an example the current main branch has 0.0.2-SNAPSHOT and you want to release 0.0.2 you need to run

```shell
make release RELEASE_VERSION=0.0.2
```

The 0.0.2 version is set with the git tag v0.0.2.
It will automatically set the main branch to 0.0.3-SNAPSHOT for the next iteration.
All changes stay in your local repository.
When you want to publish the new released version you need to run

```shell
git push                # Push the main branch with the new -SNAPSHOT version
git push origin v0.0.2  # Push the release tag which triggers the build which publishes artifacts.
```
