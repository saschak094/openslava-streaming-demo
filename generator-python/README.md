
# OpenSlava test data generator

## Kafka Prep

Create a topic 'openslava' into which testdata will be generated:

```shell
kafka-topics --zookeeper localhost:2181 --create --replication-factor 1 --partitions 4 --topic openslava
```

Common helpers:

```shell
# list all topics:
kafka-topics --zookeeper localhost:2181 --list
# delete a topic:
kafka-topics --zookeeper localhost:2181 --delete --topic openslava
# consume a topic from the beginning and print it out:
kafka-console-consumer --bootstrap-server localhost:9092 --topic openslava --from-beginning

# purge a topic. This first step will reduce retention to a second:
kafka-configs --zookeeper localhost:2181 --entity-type topics --alter --add-config retention.ms=1000 --entity-name openslava
# give it a minute or so, then remove config
kafka-configs --zookeeper localhost:2181 --entity-type topics --alter --delete-config retention.ms --entity-name openslava

# Configurations set via this method can be displayed with the command
kafka-configs --zookeeper localhost:2181 --entity-type topics --describe --entity-name openslava
```

For local Kafka operation you'll also want to add to your `server.properties` (on homebrew this is in `/usr/local/etc/kafka`) to get rid off replication factor warnings on the `offsets` topic that keeps track of consumers position. Additionally allow _real_ topic deletion & speed up (only on dev) the retention checks to each 10sek.

```
offsets.topic.replication.factor=1
delete.topic.enable=true
log.retention.check.interval.ms=10000
```

## Start producing messages

#### Prerequisites 
Before running the producer it's necessary to install the python kafka module
``` (sudo) pip install kafka ```

#### Run the producer 
```python kafka_producer.py <broker_host> <topic_name>```

```python kafka_producer.py -h```  describes the parameters

