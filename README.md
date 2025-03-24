# PromSNMP [![promsnmp-build](https://github.com/pbrane/PromSNMP/actions/workflows/promsnmp-build.yaml/badge.svg)](https://github.com/pbrane/PromSNMP/actions/workflows/promsnmp-build.yaml)


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
docker run --rm -p "8082:8080/tcp" local/promsnmp:0.0.1-SNAPSHOT
```

## 🔌 API Endpoints

The application exposes the following endpoints:

| Endpoint                     | Method | Description                                       |
|------------------------------|--------|---------------------------------------------------|
| `/promSnmp/hello`            | GET    | Returns a simple "Hello World" response           |
| `/promSnmp/sample`           | GET    | Returns sample Prometheus metrics from static file |
| `/promSnmp/router`           | GET    | Returns router metrics from static file           |
| `/promSnmp/metrics/{type}`   | GET    | Returns filtered metrics by type (optional query param: `instance`) |
| `/promSnmp/direct`           | GET    | Returns dynamically generated Prometheus metrics for all routers |
| `/promSnmp/direct/{routerId}`| GET    | Returns metrics for a specific router (optional query param: `type`) |

## 📊 Dynamic Metrics

The application provides two types of metrics:

1. **Static Metrics** (`/promSnmp/sample` and `/promSnmp/router`):
   - Fixed values from static data files
   - Consistent between requests
   - Useful for testing and initial setup

2. **Dynamic Metrics** (`/promSnmp/direct` and `/promSnmp/direct/{routerId}`):
   - Generated on-the-fly with randomized values
   - Values change on each request, simulating real network devices
   - Includes randomized:
     - Traffic counters (varying between 100GB and 5TB)
     - Interface status (10% chance of interface being down)
     - Error rates (20% chance of having errors)
     - System metrics (CPU, memory, temperature) with realistic variations
   - Use these endpoints for testing dynamic dashboards and alerts

## https://start.spring.io 
<img width="1354" alt="Pasted Graphic" src="https://github.com/user-attachments/assets/6f16a6de-af22-493b-8b3f-813c681fe273" />

## Unzip Generated promsnmp.zip and open in IntelliJ and configure application.properties:
<img width="1600" alt="Pasted Graphic 1" src="https://github.com/user-attachments/assets/0ff56d36-886d-4533-b4e4-2c599f2665b1" />

## List the Actuator endpoints:
<img width="1564" alt="Pasted Graphic 2" src="https://github.com/user-attachments/assets/01bf3963-9a85-403e-b7b3-e46a88ee0d40" />

## The “health” actuator endpoint:
<img width="1564" alt="Pasted Graphic 3" src="https://github.com/user-attachments/assets/8bc62051-3c2f-4ab4-9f46-e3daef8298e6" />

## Export Actuator Data in Prometheus format:
<img width="1564" alt="Pasted Graphic 4" src="https://github.com/user-attachments/assets/42202b6e-8fa8-4c63-a24a-507635a255b2" />

## Run in Docker
<img width="1535" alt="image" src="https://github.com/user-attachments/assets/cd6b3060-8d2e-4a16-af25-7c004cb943d7" />
