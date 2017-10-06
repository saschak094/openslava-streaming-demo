# OpenSlava material for "Data Streaming - No More Batches"

In this talk, we'll show how to do state of the art streaming in the Java / BigData ecosystem for operational systems so you never (realistic: rarely) have to do classic batches again and even those are solved more elegantly. This is the foundation for many asynchronous integration patterns in a microservice world where moving data becomes more important than ever. We'll show how data can be transformed with ease and how this all fits in a modern Java-centric developer lifecycle.

Specifically this means we have the following components:

- a small [generator](../generator/)
- a small "database" with fact-data we will join with. This is just a CSV :-)
- the actual Java 8 based (streaming job) [../structured-kafka-streaming] 