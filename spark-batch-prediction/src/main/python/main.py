import sys
from cortex import Cortex
import json
import logging

from pyspark import SparkConf
from pyspark.sql import SparkSession
from pyspark.sql.functions import pandas_udf, udf
from pyspark.sql.types import DoubleType


def predict(*cols):
    prediction = broadcast_ml_model.value.predict([cols])
    return float(prediction)


def initialize_spark_session(conf):
    builder = SparkSession.builder.appName("spark-batch-predict")
    builder = builder.config(conf=conf) if conf else builder
    return builder.getOrCreate()


def load_experiment(client, experiment_name):
    experiment = client.experiment(experiment_name)
    run = experiment.last_run()
    return run.get_artifact('model')


def score_predictions(df, model, sc):
    global broadcast_ml_model
    # Broadcasting ML model across nodes for parallel prediction
    broadcast_ml_model = sc.broadcast(model)
    logging.info("Model Object:", broadcast_ml_model.value)

    # Scoring using the Model
    predict_udf = udf(predict, DoubleType())
    df = df.withColumn("score", predict_udf(*df.columns))
    return df


def make_batch_predictions(input_params):
    conn_params = {}
    url = input_params["apiEndpoint"]
    token = input_params["token"]
    project = input_params["projectId"]
    outcome = input_params["properties"]["outcome"]

    # Initialize Cortex Client
    client = Cortex.client(api_endpoint=url, token=token, project=project)

    # Read cortex connection details
    connection = client.get_connection(input_params["properties"]["connection_name"])
    for p in connection['params']:
        conn_params.update({p['name']: p['value']})
    logging.info("connection params", conn_params)

    # Load Experiment
    model = load_experiment(client, input_params["properties"]["experiment_name"])

    if connection.get("connectionType") == "s3":
        output_path = input_params["properties"]["output_path"]
        secret_key = input_params["properties"][conn_params["secretKey"].split("#SECURE.")[1]]
        conf = SparkConf().set("fs.s3a.access.key", conn_params.get('publicKey')) \
            .set("fs.s3a.secret.key", secret_key) \
            .set("fs.s3a.endpoint", conn_params.get("s3Endpoint")) \
            .set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")

        # Initialize spark session
        spark = initialize_spark_session(conf)
        sc = spark.sparkContext

        # Get S3 file path of the dataset
        file = conn_params["uri"]

        # Create spark data-frame for prediction
        df = spark.read.option("inferSchema", True).csv(file, header=True)
        df = df.drop(outcome)
        logging.info(df.printSchema())

        # Make predictions
        df = score_predictions(df, model, sc)

        # Writing to output
        df.write.csv(output_path, mode='append', header=True)
        spark.stop()

    if connection.get("connectionType") == "mongo":
        output_collection = input_params["properties"]["output_collection"]
        mongo_uri = input_params["properties"][conn_params["url"].split("#SECURE.")[1]]
        database = conn_params.get("database")
        collection = conn_params.get("collection")

        spark = initialize_spark_session(None)
        sc = spark.sparkContext
        df = spark.read().format("mongo").option("spark.mongodb.input.uri", mongo_uri) \
            .option("database", database) \
            .option("collection", collection).load()
        df = df.drop(outcome)
        logging.info(df.printSchema())

        # Make predictions
        df = score_predictions(df, model, sc)

        # Writing to output
        df.write.format("com.mongodb.spark.sql.DefaultSource") \
            .mode("append").option("spark.mongodb.input.uri", mongo_uri) \
            .option("database", database) \
            .option("collection", output_collection).save()
        spark.stop()


if __name__ == "__main__":
    if len(sys.argv) < 1:
        logging.error("Missing Arguments", file=sys.stderr)
        sys.exit(-1)
    params = json.loads(sys.argv[1])
    make_batch_predictions(params)
