# Data Streaming with Spark and Java 

The purpose of this application is to stream data from Kafka, combine the realtime data with static data from a CSV and either print it on the console or send it to Kafka.

## Run the application 

### Validate and change the runtime arguments in the pom.xml
```xml
<argument>kafka_broker:ip</argument>
<argument>subscribe</argument>
<argument>kafka_topic</argument>
<argument>csv_directory</argument>
<argument>spark_master</argument
```

### To run the application on the command line use the following command: 
``` mvn exec:java```