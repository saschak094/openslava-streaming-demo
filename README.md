

#  "Data Streaming - No More Batches"

This repository should serve as entrypoint into the world of streaming using [Apache Spark](https://github.com/apache/spark) as underlying foundation. 
It should demonstrate how to do state of the art streaming in the Java / BigData ecosystem for operational systems so you never (realistic: rarely) have to do classic batches again and even those are solved more elegantly. This is the foundation for many asynchronous integration patterns in a microservice world where moving data becomes more important than ever. We'll show how data can be transformed with ease and how this all fits in a modern Java-centric developer lifecycle.

The corresponding talk held on Openslava can be found on [youtube](https://www.youtube.com/watch?v=sKSHn00bl-o) 


## Getting Started

The repository consists of multiple components, showcasing how to join real-time data with static data using Apache Kafka and Apache Spark.

Specifically this means we have the following components:

- a small JavaScript [kafka-data-generator](./generator-js/)
- a small Python [kafka-data-generator](./generator-python/)
- a small "database" with fact-data we will join with. This is just a CSV :-)
- the actual Java 8 based streaming [job](./structured-kafka-streaming)

See the Run the applications section for notes on how to start up the applications.  

## Prerequisites
Make sure you have running instance/cluster of Apache Kafka. 

Install the Kafka module for python: 
``` (sudo) pip install kafka ```

## Run the applications


### kafka-data-generator 

Therefore we are using the generator written in python. 

Start the generator with: ```python generator-python/kafka_producer.py <broker_host> <topic_name>```

To verify that the generator is successfully producing message in Kafka, use the Kafka console tool:

```<kafka-path>/bin/kafka-console-consumer --bootstrap-server <broker_host> --topic <topic_name> --from-beginning```

The output should look like: 

```json
{"username": "DarthVader", "action": "login", "uid": "U001", "ts": "2017-10-29T11:00:52.748877"}
{"username": "C-3PO", "action": "login", "uid": "U002", "ts": "2017-10-29T11:00:53.414842"}
{"username": "Chewbacca", "action": "logout", "uid": "U003", "ts": "2017-10-29T11:00:53.916544"}
{"username": "HanSolo", "action": "contacted_support", "uid": "U004", "ts": "2017-10-29T11:00:54.420672"}
{"username": "LukeSkyWalker", "action": "contacted_support", "uid": "U005", "ts": "2017-10-29T11:00:54.922662"}
{"username": "ObiWan", "action": "creditcard_payment", "uid": "U006", "ts": "2017-10-29T11:00:55.424631"}
```
You can keep the generator running.

First step is done we succefully stored messages in Kafka, now we need to process them. 


### spark-streaming

For sake of demonstration we are joining real-time data with static data of a csv file. This file can easily be replaced by a database.

Navigate into the ```structured-kafka-streaming``` directory.


Before running the application make sure the right properties are set in the pom.xml:
```xml
<argument>kafka_broker:ip</argument>
<argument>subscribe</argument>
<argument>kafka_topic</argument>
<argument>csv_directory</argument>
<argument>spark_master</argument
```

To install the intitial dependencies and generate the output files use:
``` mvn install```

This is just necessary for the first start.

The application provides two different options of dealing with the data: 
* Print the output on the console
* Store the joined data back into a Kafka topic (You won't see any output on the console)

The options can be changed in the App.java file.

Run the application with the following command:
``` mvn exec:java```

If you choose to print out the joined data to the console, the output should look like: 

```bash
-------------------------------------------
+----+-----------------+---------+--------------------+--------------------+---------+-----+---------------+-------+--------------+
| uid|           action| username|                  ts|              street|     city|  zip|          state|country|  mobilenumber|
+----+-----------------+---------+--------------------+--------------------+---------+-----+---------------+-------+--------------+
|U008|contacted_support|     Yoda|2017-10-29T11:34:...|2552 Franklin Avenue|   Malibu|98704|     California|     US|   909-931-193|
|U009|contacted_support|SpiderMan|2017-10-29T11:34:...|    32 George Street|   Sydney| 4621|New South Wales|    AUS|(12) 4303 6821|
|U010|contacted_support|  IronMan|2017-10-29T11:34:...|      Marxergasse 12|   Vienna| 1030|         Vienna|    AUT|0676 123 54 93|
|U011|            login| DeadPool|2017-10-29T11:34:...|      Europa-Allee 6|Frankfurt|60327|         Hessen|     DE| 04894 762 119|
+----+-----------------+---------+--------------------+--------------------+---------+-----+---------------+-------+--------------+

-------------------------------------------
Batch: 2
-------------------------------------------
+----+------------------+----------+--------------------+-------------------+------------+-------+------------+-------+-------------+
| uid|            action|  username|                  ts|             street|        city|    zip|       state|country| mobilenumber|
+----+------------------+----------+--------------------+-------------------+------------+-------+------------+-------+-------------+
|U012|             login|      Thor|2017-10-29T11:34:...|   107 William Road|Johannesburg|   2192|Johannesburg|    ZAF|   2823 23412|
|U013|   checked_balance|      Hulk|2017-10-29T11:34:...|       Detroid Road|       Dubai|   2141|       Dubai|    UAE| 131231234213|
|U001|creditcard_payment|DarthVader|2017-10-29T11:34:...|2552 Lindale Avenue|     Alameda|  94501|  California|     US| 909-221-0684|
|U002| contacted_support|     C-3PO|2017-10-29T11:34:...|59 Hampton Court Rd|   SOUTHWOOD|BA6 5XG|           /|     UK|079 1104 5050|
+----+------------------+----------+--------------------+-------------------+------------+-------+------------+-------+-------------+

-------------------------------------------
Batch: 3
-------------------------------------------
+----+-----------------+-------------+--------------------+--------------------+---------------+-----+------------------+-------+--------------+
| uid|           action|     username|                  ts|              street|           city|  zip|             state|country|  mobilenumber|
+----+-----------------+-------------+--------------------+--------------------+---------------+-----+------------------+-------+--------------+
|U003|           logout|    Chewbacca|2017-10-29T11:34:...|   Kastanienallee 60|     Fresendelf|25876|Schleswig-Holstein|     DE|04884 98 80 56|
|U004|contacted_support|      HanSolo|2017-10-29T11:34:...|52 Glen William Road|         GUNUNA| 4871|        Queensland|    AUS|(07) 4038 5949|
|U005|            login|LukeSkyWalker|2017-10-29T11:34:...| Löwenzahnstrasse 60|UNTER-OBERNDORF| 3032|     Lower Austria|    AUT|0664 516 77 39|
|U006|            login|       ObiWan|2017-10-29T11:34:...|       Národná 671/1|         Žilina|01001|            Žilina|     SK| 041 562 21 96|
+----+-----------------+-------------+--------------------+--------------------+---------------+-----+------------------+-------+--------------+

-------------------------------------------
Batch: 4
-------------------------------------------
+----+-----------------+---------+--------------------+--------------------+--------+--------+---------------+-------+--------------+
| uid|           action| username|                  ts|              street|    city|     zip|          state|country|  mobilenumber|
+----+-----------------+---------+--------------------+--------------------+--------+--------+---------------+-------+--------------+
|U007|            login|     R2D2|2017-10-29T11:34:...|      58 Dunmow Road|GROMFORD|IP17 4HG|              /|     UK| 077 3021 9431|
|U008|  checked_balance|     Yoda|2017-10-29T11:34:...|2552 Franklin Avenue|  Malibu|   98704|     California|     US|   909-931-193|
|U009|           logout|SpiderMan|2017-10-29T11:34:...|    32 George Street|  Sydney|    4621|New South Wales|    AUS|(12) 4303 6821|
|U010|contacted_support|  IronMan|2017-10-29T11:34:...|      Marxergasse 12|  Vienna|    1030|         Vienna|    AUT|0676 123 54 93|
+----+-----------------+---------+--------------------+--------------------+--------+--------+---------------+-------+--------------+

-------------------------------------------
Batch: 5
-------------------------------------------
+----+-----------------+----------+--------------------+-------------------+------------+-----+------------+-------+-------------+
| uid|           action|  username|                  ts|             street|        city|  zip|       state|country| mobilenumber|
+----+-----------------+----------+--------------------+-------------------+------------+-----+------------+-------+-------------+
|U011|contacted_support|  DeadPool|2017-10-29T11:34:...|     Europa-Allee 6|   Frankfurt|60327|      Hessen|     DE|04894 762 119|
|U012|contacted_support|      Thor|2017-10-29T11:34:...|   107 William Road|Johannesburg| 2192|Johannesburg|    ZAF|   2823 23412|
|U013|contacted_support|      Hulk|2017-10-29T11:34:...|       Detroid Road|       Dubai| 2141|       Dubai|    UAE| 131231234213|
|U001|           logout|DarthVader|2017-10-29T11:34:...|2552 Lindale Avenue|     Alameda|94501|  California|     US| 909-221-0684|
+----+-----------------+----------+--------------------+-------------------+------------+-----+------------+-------+-------------+
```

Each table displays Kafka entries of the last two seconds joined with the static data. 
