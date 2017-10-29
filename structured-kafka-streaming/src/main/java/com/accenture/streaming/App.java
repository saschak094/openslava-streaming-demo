package com.accenture.streaming;

import static org.apache.spark.sql.functions.*;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.OutputMode;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.sql.types.StructType;

/**
 * Spark Structured Stream Processing with Apache Kafka <br>
 * Consumes JSON data from Kafka, maps the data into Java Objects and joins the
 * Kafka data with static data from a CSV file.
 *
 */
public class App {
	static final String KAFKA_SUBSCRIBE_TYPE = "subscribe";
	static final String SPARK_MASTER = "spark.master";
	static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";

	public static void main(String[] args) throws StreamingQueryException {

		// input parameters parsing
		if (args.length < 4) {
			System.err.println(
					"Required input params -> <bootstrap-server (ip:port)> <topics> <static_data_dir> <spark_master_address");
			System.exit(1);
		}

		// Kafka Broker IP with port
		String bootstrapServers = args[0];
		// Kafka Topic
		String topics = args[2];
		// Directory where the CSV file is stored
		String staticDataDir = args[3];
		// Spark Master address
		String masterAddress = args[4];

		// Getting the static CSV data from a directory
		SparkSession spark = SparkSession.builder().appName("OpenSlava 2017 Streaming Demo")
				.config(SPARK_MASTER, masterAddress).getOrCreate();

		// mute it down, Spark is superchatty on INFO
		spark.sparkContext().setLogLevel("WARN");

		// Define static schema (our CSV)
		StructType staticSchema = new StructType().add("uid", "string") // we'll match on this column
				.add("street", "string") // all other fields...
				.add("city", "string").add("zip", "string").add("state", "string").add("country", "string")
				.add("mobilenumber", "string");

		// now let's read the CSV and associate it with our staticSchema
		Dataset<Row> staticData = spark.read().format("csv") // reading a CSV
				.option("sep", ",") // separator is ","
				.schema(staticSchema) // apply schema
				.load(staticDataDir); // load all files from this dir

		// Definition of the Kafka Stream including the mapping of JSON into Java
		// Objects
		Dataset<UserActivity> kafkaEntries = spark.readStream() // read a stream
				.format("kafka") // from KAFKA
				.option("kafka.bootstrap.servers", bootstrapServers) // connection to servers
				.option("subscribe", topics).load() // subscribe & load
				.select(json_tuple(col("value").cast("string"), // explode value column as JSON
						"action", "uid", "username", "ts")) // JSON fields we extract
				.toDF("action", "uid", "username", "ts") // map columns to new names (same here in demo)
				.as(Encoders.bean(UserActivity.class)); // make a good old JavaBean out of it

		// Join kafkaEntries with the static data
		Dataset<Row> joinedData = kafkaEntries.join(staticData, "uid");

		// Write the real-time data from Kafka to the console
		// StreamingQuery query = kafkaEntries.writeStream()		// write a stream
		// 		.trigger(Trigger.ProcessingTime(2000))				// every two seconds
		// 		.format("console")														// to the console
		// 		.outputMode(OutputMode.Append())							// only write newly matched stuff
		// 		.start();

		// Start streaming to command line 		
		StreamingQuery query = joinedData.writeStream() // write a stream
				.trigger(Trigger.ProcessingTime(2000)) // every two seconds
				.format("console") // to the console
				.outputMode(OutputMode.Append()) // only write newly matched stuff
				.start();

		/*
		StreamingQuery query = joinedData
				.select(col("uid").as("key"), 								// uid is our key for Kafka (not ideal!)
						to_json(struct(col("action")							// build a struct (grouping) and convert to JSON
						, col("username"), col("ts")							// ...of our...
						, col("city"), col("state")))						// columns
						.as("value"))														// as value for Kafka
				.writeStream()																// write this key/value as a stream
				.trigger(Trigger.ProcessingTime(2000))				// every two seconds 
				.format("kafka")															// to Kafka :-)
				.option("kafka.bootstrap.servers", bootstrapServers)
				.option("topic", "openslava-output")
				.option("checkpointLocation", "checkpoint")  // metadata for checkpointing 
				.start();
		*/

		// block main thread until done.
		query.awaitTermination();
	}
}
