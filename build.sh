#!/bin/bash -ex
mvn clean install

#Start the kafka and zookeeper containers
sudo docker-compose up -d

#Create kafka topics
$1 --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic atm.account || true
$1 --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic atm.command || true
$1 --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic atm.account.result || true
$1 --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic atm.account.refused || true

#start kafka stream container
sudo docker build . -t kafka-stream
sudo docker run -d kafka-stream