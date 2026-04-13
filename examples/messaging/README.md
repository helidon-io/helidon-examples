# Helidon Messaging Examples

## Prerequisites
* Docker
* Java 21+

### Test Kafka server
To make examples easily runnable, 
small, pocket size and pre-configured testing Kafka server Docker image is available. 

* To run it locally: `docker compose -f ./docker/kafka/docker-compose.yaml up -d`
  
* Send messages manually with: 
`docker exec -it kafka /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test-topic-1`
* Consume messages manually with: 
`docker exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test-topic-1`

* Stop kafka with `docker compose -f ./docker/kafka/docker-compose.yaml down`

### Test JMS server
* Start ActiveMQ server locally: 
```shell
docker run --name='activemq' --rm -p 61616:61616 -p 8161:8161 rmohr/activemq
```

### Test Oracle database
* Start ActiveMQ server locally: 
```shell
cd ./docker/oracle-aq
./buildAndRun.sh
```

## Helidon SE Reactive Messaging with Kafka Example
For demonstration of Helidon SE Messaging with Kafka connector, 
continue to [Kafka with WebSocket SE Example](kafka-websocket-se/README.md)

## Helidon MP Reactive Messaging with Kafka Example
For demonstration of Helidon MP Messaging with Kafka connector, 
continue to [Kafka with WebSocket MP Example](kafka-websocket-mp/README.md)

## Helidon MP Reactive Messaging with JMS Example
For demonstration of Helidon MP Messaging with JMS connector, 
continue to [JMS with WebSocket MP Example](jms-websocket-mp/README.md)

## Helidon MP Reactive Messaging with Oracle AQ Example
For demonstration of Helidon MP Messaging with Oracle Advance Queueing connector, 
continue to [Oracle AQ with WebSocket MP Example](oracle-aq-websocket-mp/README.md)


