# Springboot Playground

Testing all things Springboot microservices. The system will be composed of multiple microservices connected using different technologies. Refer to [Services](#services) for a list of services and technologies used.

Uses [Jib](https://github.com/GoogleContainerTools/jib) and
[Skaffold](https://skaffold.dev/) to build and run the services in Docker locally. 
Uses [Testcontainers](https://www.testcontainers.org/modules/localstack/) for integration tests and 
[Localstack](https://github.com/localstack/localstack) for AWS resources. 
  
For a quick overview of Jib and Skaffold check out [JavaServiceInContainer](https://github.com/ZanderAdam/JavaServiceInContainer/blob/master/README.md) repo.
  
## Prereqs

Assumes that the Java SDK is already installed.  
Install [Maven](https://maven.apache.org/)  
Install [Docker for Desktop](https://www.docker.com/products/docker-desktop)  
Install [Skaffold](https://skaffold.dev/docs/install/)  
Install [Localstack AWS CLI](https://github.com/localstack/awscli-local)

Enable Kuberneties on Docker for Desktop by going to Preferences and checking "Enable Kubernetes"

![Docker Preferences](https://raw.githubusercontent.com/ZanderAdam/JavaServiceInContainer/master/docs/dockerPrefs.png)

## Services

### Kinesis using Localstack

Two simple microservices that are connected via Kinesis running on Localstack:

- **order-service** - Exposes a simple RestAPI that receives an order. Acts as a Kinesis producer and sends the order to a Kinesis "springbootplayground" stream. Also contains a Kinesis producer integration test using Testcontainers and Localstack.
- **order-fulfillment-service** - Kinesis consumer that receives the order and (currently) outputs to the console window.

#### Quick Start

1. In a terminal window cd into `SpringBootPlayground/order-service` and run:
 ```
skaffold dev --cache-artifacts=false
 ```
2. Once the service is up and running, in another terminal window create the Kinesis stream by running:
```
awslocal kinesis create-stream --shard-count 1 --stream-name springbootplayground
```
3. On the same window cd into `SpringBootPlayground/order-fulfillment-service` and run 
```
skaffold dev --cache-artifacts=false
 ```
4. The first time startup might take a while as the Kinsesis stream gets configured. Once you see a message similar to below, the service is fully loaded:
```
[order-fulfillment-service] 2020-12-28 23:15:05.973  INFO 1 --- [           main] s.a.k.coordinator.DiagnosticEventLogger  : Current thread pool executor state: ExecutorStateEvent(executorName=SchedulerThreadPoolExecutor, currentQueueSize=0, activeThreads=0, coreThreads=0, leasesOwned=1, largestPoolSize=2, maximumPoolSize=2147483647)
```
5. By default, the `order-service` runs on `localhost:8080`. Create a new order by creating a POST request (using postman, curl or a tool of your choice) to `localhost:8080/order` with following JSON body:
```
{
    "userId": 1,
    "orderedProductIds": [1, 2, 3]
}
```
6. The order-fulfillment-service will receive the message and output the order in terminal:
```
[order-fulfillment-service]             GOT DATA
[order-fulfillment-service] {"userId":1,"orderedProductIds":[1,2,3],"timeStamp":{"year":2020,"month":"DECEMBER","nano":481000000,"monthValue":12,"dayOfMonth":28,"hour":23,"minute":15,"second":24,"dayOfYear":363,"dayOfWeek":"MONDAY","chronology":{"id":"ISO","calendarType":"iso8601"}}}
```
