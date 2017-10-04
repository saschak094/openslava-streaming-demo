package com.accenture.streaming;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

/**
 * Spark Structured Stream Processing with Apache Kafka <br>
 * Consumes JSON data from Kafka, maps the data into Java Objects and joins the
 * Kafka data with static data from a CSV file.
 *
 */
public class App {
	private static final String KAFKA_SUBSCRIBE_TYPE = "subscribe";
	private static final String STRUCT_TYPE_INTEGER = "integer";
	private static final String STRUCT_TYPE_STRING = "string";
	private static final String FORMAT_CSV = "csv";
	private static final String FORMAT_KAFKA = "kafka";
	private static final String SPARK_MASTER = "spark.master";
	private static final String STRUCTURED_KAFKA_STREAMING = "StructuredKafkaStreaming";
	private static final String TS = "ts";
	private static final String UID = "uid";
	private static final String ACTION = "action";
	private static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";
	private static final String CONSOLE = "console";
	private static final String APPEND = "append";
	private static final String USERNAME = "username";

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
		SparkSession spark = SparkSession.builder().appName(STRUCTURED_KAFKA_STREAMING)
				.config(SPARK_MASTER, masterAddress).getOrCreate();
		StructType staticSchema = new StructType().add(UID, STRUCT_TYPE_STRING).add("street", STRUCT_TYPE_STRING)
				.add("city", STRUCT_TYPE_STRING).add("zip", STRUCT_TYPE_STRING).add("state", STRUCT_TYPE_STRING).add("country", STRUCT_TYPE_STRING).add("mobilenumber", STRUCT_TYPE_STRING);
		
		Dataset<Row> staticData = spark.read().option("sep", ",").schema(staticSchema).format(FORMAT_CSV)
				.load(staticDataDir);

		// Schema Mapping for the incoming Kafka values
		StructType activitySchema = DataTypes.createStructType(
				new StructField[] { DataTypes.createStructField(USERNAME, DataTypes.StringType, false),
						DataTypes.createStructField(ACTION, DataTypes.StringType, false),
						DataTypes.createStructField(UID, DataTypes.StringType, false),
						DataTypes.createStructField(TS, DataTypes.TimestampType, false) });

		// Definition of the Kafka Stream including the mapping of JSON into Java
		// Objects
		Dataset<UserActivity> kafkaEntries = spark.readStream().format(FORMAT_KAFKA)
				.option(KAFKA_BOOTSTRAP_SERVERS, bootstrapServers).option(KAFKA_SUBSCRIBE_TYPE, topics).load()
				.selectExpr("CAST(value AS STRING)")
				.select(functions.from_json(functions.col("value"), activitySchema).as("json")).select("json.*")
				.as(Encoders.bean(UserActivity.class));

		// Join kafkaEntries with the static data
		Dataset<Row> joinedData = kafkaEntries.join(staticData, UID);

		// Starting streaming
		StreamingQuery query = joinedData.writeStream().format(CONSOLE).outputMode(APPEND).start();
		query.awaitTermination();
	}
}
