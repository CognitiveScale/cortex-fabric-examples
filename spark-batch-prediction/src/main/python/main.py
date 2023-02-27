"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""
import sys
from cortex import Cortex
from cortex.utils import log_message, get_logger
from cortex.experiment import Experiment
import json
import numpy as np
import logging
import pandas as pd

from pyspark import SparkConf
from pyspark.sql import SparkSession, Row
from pyspark.sql.functions import col


def predict_partition(rows):
    """ Predictions by loading the partition into memory

    :param rows:  List[pyspark.sql.Row]
    :return: [pyspark.sql.Row]
    """
    # Load the input rows into a data frame.
    rows_df = pd.DataFrame.from_records(
        [row.asDict() for row in rows]
    )
    if rows_df.empty:
        return []
    model_artifact = broadcast_ml_model.value

    # If the model artifact is of type <dict>
    if isinstance(model_artifact, dict):
        cat_cols = model_artifact["cat_columns"] if "cat_columns" in model_artifact else []
        num_cols = [x for x in rows_df.columns if x not in cat_cols]
        # Transforming the input data-frame using encoder & normalizer from the experiment artifact
        x_encoded = model_artifact["encoder"].transform(rows_df[cat_cols]).toarray() if "encoder" in model_artifact and cat_cols else []
        x_normalized = model_artifact["normalizer"].transform(rows_df[num_cols]) if "normalizer" in model_artifact and num_cols else []
        if np.any(x_encoded) and np.any(x_normalized):
            x_transform = np.concatenate((x_encoded, x_normalized), axis=1)
        elif np.any(x_encoded):
            x_transform = np.concatenate((x_encoded, rows_df[num_cols]), axis=1)
        elif np.any(x_normalized):
            x_transform = x_normalized
        else:
            x_transform = rows_df
        df = pd.DataFrame(x_transform)
        rows_df.loc[:, 'prediction'] = broadcast_ml_model.value["model"].predict(df.values)
    else:
        # If the model object is an instance of model itself
        rows_df.loc[:, 'prediction'] = broadcast_ml_model.value.predict(rows_df.values)

    data_dict = rows_df.dtypes.to_dict()
    for key in data_dict.keys():
        if data_dict[key].name == "float64":
            rows_df[key] = rows_df[key].apply(lambda x: (x,))

    # Transforming predictions into Rows again
    make_row = lambda row: Row(**{col: row[1][col] for col in rows_df.columns})
    return map(make_row, rows_df.iterrows())


def initialize_spark_session(conf):
    builder = SparkSession.builder.appName("spark-batch-predict")
    builder = builder.config(conf=conf) if conf else builder
    return builder.getOrCreate()


def load_model(client, experiment_name, run_id):
    result = client.experiments.get_experiment(experiment_name)
    experiment = Experiment(result, client.experiments)
    run = experiment.get_run(run_id)
    return run.get_artifact('model')


def score_predictions(df, model, outcome, sc, skill_name):
    global broadcast_ml_model
    # Broadcasting ML model across nodes for parallel prediction
    broadcast_ml_model = sc.broadcast(model)
    log_message(msg=f"Model Object: {str(broadcast_ml_model.value)}", log=get_logger(skill_name), level=logging.INFO)
    # Scoring using the Model
    return df.rdd.mapPartitions(predict_partition).toDF().withColumnRenamed("prediction", outcome)


def make_batch_predictions(input_params):
    conn_params = {}
    url = input_params["apiEndpoint"]
    token = input_params["token"]
    project = input_params["projectId"]
    skill_name = input_params["skillName"]
    outcome = input_params["properties"]["outcome"]

    # Initialize Cortex Client
    client = Cortex.client(api_endpoint=url, token=token, project=project)

    # Read cortex connection details
    connection = client.connections.get_connection(input_params["properties"]["connection-name"])
    for p in connection['params']:
        conn_params.update({p['name']: p['value']})
    log_message(msg=f"Connection Params: {str(conn_params)}", log=get_logger(skill_name), level=logging.INFO)
    # Load Model from the experiment run
    model = load_model(client, input_params["properties"]["experiment-name"], input_params["properties"]["run-id"])

    if connection.get("connectionType") == "s3":
        output_path = input_params["properties"]["output-path"]
        secret_key = input_params["properties"]["aws-secret"]
        conf = SparkConf().set("fs.s3a.access.key", conn_params.get('publicKey')) \
            .set("fs.s3a.secret.key", secret_key) \
            .set("fs.s3a.endpoint", conn_params.get("s3Endpoint")) \
            .set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")

        # Initialize spark session
        spark = initialize_spark_session(conf=conf)
        sc = spark.sparkContext

        # Get S3 file path of the dataset
        file = conn_params["uri"]

        # Create spark data-frame for prediction
        df = spark.read.option("inferSchema", True).csv(file, header=True)
        df = df.drop(outcome)
        log_message(msg=f"DataFrame Schema: {str(df.printSchema())}", log=get_logger(skill_name), level=logging.INFO)

        # Make predictions
        df = score_predictions(df, model, outcome, sc, skill_name)
        # Converting struct to double
        for t in df.dtypes:
            if t[1] == "struct<_1:double>":
                df = df.withColumn(t[0], col(t[0]).getField("_1"))

        # Writing to output
        df.write.csv(output_path, mode='append', header=True)

    elif connection.get("connectionType") == "mongo":
        output_collection = input_params["properties"]["output-collection"]
        mongo_uri = input_params["properties"]["mongo-uri"]
        database = conn_params.get("database")
        collection = conn_params.get("collection")

        spark = initialize_spark_session(conf=None)
        sc = spark.sparkContext
        df = spark.read.format("com.mongodb.spark.sql.DefaultSource").option("uri", mongo_uri) \
            .option("database", database) \
            .option("collection", collection).load()
        df = df.drop(outcome, "_id")
        log_message(msg=f"DataFrame Schema after Prediction: {str(df.printSchema())}", log=get_logger(skill_name), level=logging.INFO)

        # Score predictions
        df = score_predictions(df, model, outcome, sc, skill_name)
        for t in df.dtypes:
            if t[1] == "struct<_1:double>":
                df = df.withColumn(t[0], col(t[0]).getField("_1"))

        # Writing to output
        df.write.format("com.mongodb.spark.sql.DefaultSource") \
            .mode("append").option("uri", mongo_uri) \
            .option("database", database) \
            .option("collection", output_collection).save()
    else:
        # Implement based on requirement
        spark = initialize_spark_session(conf=None)
    spark.stop()


if __name__ == "__main__":
    if len(sys.argv) < 1:
        logging.error("Missing Arguments", file=sys.stderr)
        sys.exit(-1)
    params = json.loads(sys.argv[1])
    make_batch_predictions(params)
