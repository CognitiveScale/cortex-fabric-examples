"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""
import os
import sys
import json
import logging
import numpy as np
import pandas as pd
import boto3
import pymongo

# cortex
from cortex import Cortex


def load_model(client, experiment_name, run_id):
    """
    Load Model from Cortex Experiment

    :param client: Cortex Client
    :param experiment_name: Experiment Name
    :param run_id: Experiment Run ID
    :return: Model Object
    """
    logging.info("Loading Model from Experiment Run: {}".format(run_id))
    try:
        experiment = client.experiment(experiment_name)
        run = experiment.get_run(run_id)
        return run.get_artifact('model')
    except Exception as e:
        logging.error("Error: Failed to load model: {}".format(e))


def score_predictions(df, model):
    """
    Score Predictions using the downloaded model

    :param df: Input DataFrame
    :param model: Model Object
    :return: DataFrame with Predictions
    """
    try:
        # If the model artifact is of type `dict`
        if isinstance(model, dict):
            cat_cols = model["cat_columns"] if "cat_columns" in model else []
            num_cols = [x for x in df.columns if x not in cat_cols]
            # Transforming the input data-frame using encoder & normalizer from the experiment artifact
            if ("encoder" in model) or ("normalizer" in model):
                x_encoded = model["encoder"].transform(
                    df[cat_cols]).toarray() if "encoder" in model and cat_cols else []
                x_normalized = model["normalizer"].transform(df[num_cols]) if "normalizer" in model else df[
                    num_cols].values
                if np.any(x_encoded) and np.any(x_normalized):
                    x_transformed = np.concatenate((x_encoded, x_normalized), axis=1)
                else:
                    x_transformed = x_encoded if np.any(x_encoded) else x_normalized
            else:
                x_transformed = df.values
            df.loc[:, 'prediction'] = model["model"].predict(x_transformed)
        else:
            # If the model object is an instance of model itself
            df.loc[:, 'prediction'] = model.predict(df.values)
    except Exception as e:
        raise Exception("Error occurred while making predictions, Please check the model. Message: {}".format(e))
    return df


def init_s3_client(s3_key, s3_secret):
    """
    Initialize S3 Client

    :param s3_key: S3 Access Key
    :param s3_secret: S3 Secret Key
    :return: S3 Client Object
    """
    return boto3.client('s3', aws_access_key_id=s3_key, aws_secret_access_key=s3_secret)


def download_file(s3_client, path):
    """
    Download file from S3

    :param s3_client: S3 Client Object
    :param path: S3 Filepath from connection
    :return: Local downloaded filepath
    """
    logging.info("Downloading file from S3 bucket")
    s3_components = path.split('/')
    bucket = s3_components[2]
    file_name = ""
    if len(s3_components) > 1:
        file_name = '/'.join(s3_components[3:])
    out_path = s3_components[-1]
    s3_client.download_file(bucket, file_name, out_path)
    return out_path


def upload_file(s3_client, filepath, s3_output_path):
    logging.info("Uploading file from S3 bucket")
    s3_components = s3_output_path.split('/')
    bucket = s3_components[2]
    print("/".join(s3_components[3:]))
    s3_client.upload_file(filepath, bucket, "/".join(s3_components[3:]))


def make_batch_predictions(input_params):
    logging.info("Batch Prediction: Invoke Request:{}".format(input_params))
    conn_params = {}
    url = input_params["apiEndpoint"]
    token = input_params["token"]
    project = input_params["projectId"]
    outcome = input_params["properties"]["outcome"]
    batch_size = int(input_params["properties"]["batch-size"])

    try:
        # Initialize Cortex Client
        client = Cortex.client(api_endpoint=url, token=token, project=project)

        # Read cortex connection details
        connection = client.get_connection(input_params["properties"]["connection-name"])
        for p in connection['params']:
            conn_params.update({p['name']: p['value']})
        print(conn_params)
        logging.info("connection params", conn_params)

        # Load Model from the experiment run
        model = load_model(client, input_params["properties"]["experiment-name"], input_params["properties"]["run-id"])
        logging.info("Model Loaded!")

        if connection.get("connectionType") == "s3":
            s3_output_path = input_params["properties"]["output-path"]
            # Get S3 file path of the dataset
            uri = conn_params["uri"]
            s3_client = init_s3_client(conn_params.get('publicKey'), conn_params.get('secretKey'))
            local_path = download_file(s3_client, uri)
            output_path = 'temp.csv'
            for chunked_df in pd.read_csv(local_path, header=0, sep=",", chunksize=batch_size):
                if outcome in chunked_df.columns.tolist():
                    chunked_df = chunked_df.drop(outcome, axis=1)
                logging.info("Processing records of size: {}".format(chunked_df.shape[0]))

                # Score Predictions for a Batch
                predicted_df = score_predictions(chunked_df, model=model)
                if not os.path.isfile(output_path):
                    predicted_df.to_csv(output_path, index=False)
                predicted_df.to_csv(output_path, mode='a', header=False, index=False)

            # Uploading file to S3
            upload_file(s3_client, output_path, s3_output_path)

        elif connection.get("connectionType") == "mongo":
            output_collection = input_params["properties"]["output-collection"]
            mongo_uri = conn_params.get("uri")
            database = conn_params.get("database")
            collection = conn_params.get("collection")
            client = pymongo.MongoClient(mongo_uri)
            total_records = client[database][collection].count({})
            skip = 0
            while skip <= total_records:
                cursor = client[database][collection].find({}).limit(batch_size).skip(skip)
                # Expand the cursor and construct the DataFrame
                chunked_df = pd.DataFrame(list(cursor))
                if outcome in chunked_df.columns.tolist():
                    chunked_df = chunked_df.drop([outcome, "_id"], axis=1)
                logging.info("Processing records of size: {}".format(chunked_df.shape[0]))

                # Score Predictions for a Batch
                predicted_df = score_predictions(chunked_df, model=model)
                predicted_df.reset_index(inplace=True)
                data_dict = predicted_df.to_dict("records")
                # Insert collection
                client[database][output_collection].insert_many(data_dict)
                skip += batch_size
            client.close()
        logging.info("Prediction Job Completed!")
    except Exception as e:
        logging.error("Error while processing batch predictions. Message: ", e)


if __name__ == '__main__':
    params = sys.argv[1]
    params = json.loads(params)
    print(f'Received: {params}')
    make_batch_predictions(params)
