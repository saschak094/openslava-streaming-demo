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
 * Spark Structured Stream Processing
 * Joins static data with incoming values of Kafka 
 *
 */
// TODO: write some comments 
public class App {
	public static void main(String[] args) throws StreamingQueryException {
		
		// input parameters parsing -> subscribeType is "subscribe"
		if (args.length < 3) {
			System.err
					.println("Usage: JavaStructuredKafkaWordCount <bootstrap-servers> " + "<subscribe-type> <topics>");
			System.exit(1);
		}

		String bootstrapServers = args[0];
		String subscribeType = args[1];
		String topics = args[2];

		// Getting the static data from the static directory 
		// TODO: improve that
		SparkSession spark = SparkSession.builder().appName("JavaStructuredKafkaWordCount")
				.config("spark.master", "local").getOrCreate();
		StructType staticSchema = new StructType().add("username", "string").add("plz", "integer");
		Dataset<Row> csvDF = spark.read().option("sep", ",").schema(staticSchema).format("csv")
				.load("./static-data");
		
		
		// Schema mapped to the incoming values
		StructType activitySchema = DataTypes.createStructType(
				new StructField[] { DataTypes.createStructField("username", DataTypes.StringType, false),
						DataTypes.createStructField("action", DataTypes.StringType, false),
						DataTypes.createStructField("uid", DataTypes.StringType, false),
						DataTypes.createStructField("ts", DataTypes.TimestampType, false) });

		// Definition of the Kafka Stream including the mapping of the JSON into the Java Objects 
		Dataset<UserActivity> kafkaEntries = spark.readStream().format("kafka")
				.option("kafka.bootstrap.servers", bootstrapServers).option(subscribeType, topics).load()
				.selectExpr("CAST(value AS STRING)")
				.select(functions.from_json(functions.col("value"), activitySchema).as("json")).select("json.*")
				.as(Encoders.bean(UserActivity.class));
		Dataset<Row> joinedData = kafkaEntries.join(csvDF, "username");
		
		
		// Starting streaming
		StreamingQuery query = joinedData.writeStream().format("console").outputMode("append").start();
		query.awaitTermination();
	}
}
