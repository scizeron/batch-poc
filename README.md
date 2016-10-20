# batch-poc

## Purpose

Batch processing based on 3 main compoments :
* scheduler : based on Quartz
* processor : based on Spring-batch (with DB repository)
* api       : management, executions reports ...

Leverage docker swarm 1.12 +

* scheduler   : service (replicas=1), provide an API to schedule/update jobs
* the scheduler leverages the service vip, ensuring the processor load balancing  
* the api is also multi replicated

## Build

```sh
maven clean install
```

## Scheduler

### run locally

```sh
java -jar target/batch-scheduler.jar
```

## Processor

### run locally

```sh
java -jar target/batch-processor.jar 
```

## Admin

Based on spring-data-rest-webmvc in order to pusblish a read only API about spring-batch jobs executions

```sh
java -jar target/batch-admin.jar 
```

## Swarm Cluster

###  Docker Image Deployment

```sh
mvn clean package docker:build -DpushImage
```

### Assumptions

* A cluster swarm is up and running
* An existing overlay network "mynet" or other ...
* A PostgreSQL server instance

```sh
docker run --name some-postgres -e POSTGRES_PASSWORD=mysecretpassword -d -p 5432:5432 postgres
```

* A "batch" database with a specific user (batch/batch)

```sh
 docker run -it --rm --link some-postgres:postgres postgres psql -h postgres -U postgres
```

```sh
CREATE USER batch WITH PASSWORD batch;
CREATE DATABASE batch OWNER batch;
```

### Create services

Set the replicas equals to 1, in order to troubleshoot.

```sh
docker service create --name processor --replicas 1 \
--network my-network scizeron/batch-processor:0.0.1-SNAPSHOT --db.batch.host=<db_host>
```

By default, the db connection info are listed below. Use --var in the command line to override :
* db.batch.host=localhost
* db.batch.port=5432
* db.batch.username=batch
* db.batch.password=batch

```sh
docker service create --name scheduler --replicas 1 \
--network my-network --endpoint-mode vip \
--publish 80:7000 scizeron/batch-scheduler:0.0.1-SNAPSHOT \
--batch.processing.service=processor:7001 --db.batch.host=<db_host>
```

### Schedule jobs

### List the scheduled jobs

```sh
curl http://<swarm_node_host>/v1/jobs returns an empty array at the begining.
```

### Add a new job

```sh
curl -X POST -d '{"jobName":"test", "clientId": "clientId", "description": "desc", "cronExpression": "0 * 14 * * ?"}' http://<swarm_node_host>/v1/jobs
```

### Troubleshooting

* Find the node where task is running : docker service ps <service>
* Connect to the host, "docker ps" allows you to get the container id
* To display the application logs : docker logs -f <container_id>

### Scaling

```sh
docker service scale processor=2
```

### Jobs executions

```sh
docker service create --name admin --replicas 1 \
--network my-network --endpoint-mode vip \
--publish 82:7002 scizeron/batch-admin:0.0.1-SNAPSHOT \
--db.batch.host=<db_host>
```

```sh
curl http://<swarm_node_host>/api/batchJobExecutions
```
