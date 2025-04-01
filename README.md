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
