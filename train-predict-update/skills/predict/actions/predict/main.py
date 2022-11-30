"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Template/Example Code [License](https://github.com/CognitiveScale/cortex-code-templates/blob/main/LICENSE.md)
"""

import json
import os
import io
import sys
import json
import logging
import numpy as np
import pandas as pd
from datetime import datetime

# cortex
from cortex import Cortex
from cortex.content import ManagedContentClient
from cortex.experiment import Experiment, ExperimentClient


def download_model(client, projectId, exp_name, run_id=None):
    """
    Load Model from Cortex Experiment

    :param client: Cortex Client
    :param projectId: Cortex project
    :param experiment_name: Experiment Name
    :param run_id: Experiment Run ID
    :return: Model Object
    """
    
    experiment_client = ExperimentClient(client)
    try:
        result = experiment_client.get_experiment(exp_name, projectId)
        experiment = Experiment(result, projectId, experiment_client)
    except Exception as e:
        raise e
    if not run_id:
        exp_run = experiment.last_run()
    else:
        exp_run = experiment.get_run(run_id)

    try:
        model = exp_run.get_artifact('model')
        return model
    except Exception as e:
        logging.error("Model does not exist {:s}".format(str(e)), level=logging.FATAL)
        return {}


def score_predictions(df, model, primary_column):
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
            num_cols = [x for x in df.columns if (x not in cat_cols) and (x != primary_column)]
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


def make_predictions(data: pd.DataFrame, target_column: str, primary_column: str, model) -> pd.DataFrame:
    logging.info("Running Prediction: Invoke Request:")
    try:
        if target_column in data.columns.tolist():
            data = data.drop(target_column, axis=1)
        
        logging.info("Processing records of size: {}".format(data.shape[0]))

        # Score Predictions for a Batch
        predicted_df = score_predictions(data, model=model, primary_column=primary_column)
       
        logging.info("Prediction Job Completed!")
        return predicted_df
    except Exception as e:
        logging.error("Error while processing batch predictions. Message: ", e, level=logging.FATAL)
        return pd.DataFrame()

def get_data(managed_content: ManagedContentClient, key: str, project: str) -> pd.DataFrame:
    """
    Function to load parquet dataframes form Managed Content

    @param managed_content: ManagedContentClient instance
    @param key: Cortex Managed Content key
    @param project: Cortex projectId
    @return: pd.DataFramce
    """
    response = managed_content.download(key, retries=1, project=project)
    content = response.read()
    pq_file = io.BytesIO(content)
    data = pd.read_parquet(pq_file)
    return data

def run(params):
    """
    Function to uses the skill params to extract profiles and dump to Managed Content

    @param payload: dictionary containing
        @param profileScehma: this is a first param
        @param experimentName: Experiment Name
        @param targetColumn: The target column to train on
        @param runId(optional): To select a particular runId fromt he experiments
    @param token: Cortex token
    @param apiEndpoint: Cortex apiendpoint
    @param projectId: Cortex projectId
    @return: True/False
    """
    token = params["token"]
    apiendpoint = params["apiEndpoint"]
    project = params["projectId"]
    payload = params["payload"]
    profile_schema = payload["profileSchema"]
    experiment_name = payload["experimentName"]
    target_column = payload["targetColumn"]
    primary_column = "profileId"
    run_id = payload.get("runId", None)

    cortex_client = Cortex.client(
        api_endpoint=apiendpoint, verify_ssl_cert=True, token=token, project=project)
    managed_content = ManagedContentClient(cortex_client)
    data = get_data(managed_content, profile_schema + ".parquet", project)
    model = download_model(cortex_client, project, experiment_name, run_id)
    pred = make_predictions(data, target_column, primary_column, model)[[primary_column, "prediction"]]
    # the new data needs to be written to sub folder
    pred_file_location = profile_schema + "_predictions/pred-" + str(datetime.utcnow()) + ".parquet"
    bootstrap_file = profile_schema + "_predictions/pred.parquet"
    pred.to_parquet("temp.parquet")
    f_obj = open("temp.parquet", mode="rb")
    if not managed_content.exists(bootstrap_file, project):
        # a boorstrap file is needed for 
        managed_content.upload_streaming(key=bootstrap_file, project=project, stream=f_obj,
                                            content_type="application/octet-stream")
    else:
        managed_content.upload_streaming(key=pred_file_location, project=project, stream=f_obj,
                                            content_type="application/octet-stream")

    f_obj.close()
    return True

def extract_json_objects(text):
    """
    Find JSON objects in text, and yield the decoded JSON data

    Does not attempt to look for JSON arrays, text, or other JSON types outside
    of a parent JSON object.
    """
    MATCH = 'Received: {'
    match_index = text.find(MATCH)
    new_line = text.find('\n', match_index)
    print(text[match_index + len(MATCH):new_line])
    result = eval(text[match_index + len(MATCH)-1:new_line])
    return result

if __name__ == '__main__':
    import sys
    params = sys.argv[1]
    params = json.loads(params)
    # params = {
    #     "token": "eyJhbGciOiJFZERTQSIsImtpZCI6Im5WalJOdWhPQzc5ZFpPYVMwaGt4U09Bek14Zm1mTWl0SUpLY05fdWQwTGcifQ.eyJzdWIiOiIyNmU5NmU1OC1kYjhjLTQ5NWQtODI3OS1jMjQ1YzNlMjMzMGUiLCJhdWQiOiJjb3J0ZXgiLCJpc3MiOiJjb2duaXRpdmVzY2FsZS5jb20iLCJpYXQiOjE2Njk4MjMzNzgsImV4cCI6MTY2OTkwOTc3OH0.5kmaTBBGcn90-yKDilNhgKmMkrhw2fQT6kxVBY95UoFfgLNjYbK6w1nXXr68vIQ81HdKx-yR9-Vv_xzQJTucAQ",
    #     "apiEndpoint": "https://api.dci-dev.dev-eks.insights.ai",
    #     "projectId": "bptest",
    #     "payload": {
    #         "profileSchema": "german-credit-0c3d3",
    #         "experimentName": "german_credit",
    #         "targetColumn": "outcome"
    #     }
    # }
    print(f'Received: {params["payload"]}')
    # need to parse the payload to get the original json
    print(f'Parsed : --')
    print(extract_json_objects(params["payload"]))
    run(params)
