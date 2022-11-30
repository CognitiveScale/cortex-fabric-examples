"""
Copyright (c) 2022. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Template/Example Code [License](https://github.com/CognitiveScale/cortex-code-templates/blob/main/LICENSE.md)
"""

import io
import json
import pickle
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeClassifier
from sklearn.preprocessing import StandardScaler, OneHotEncoder

from cortex import Cortex
from cortex.content import ManagedContentClient
from cortex.experiment import Experiment, ExperimentClient


def encode(cat_columns):
    encoder = OneHotEncoder()
    # Encode Training Data
    encoded_train_df = encoder.fit_transform(x_train[cat_columns]).toarray()
    # Encode Training Data
    encoded_test_df = encoder.fit_transform(x_test[cat_columns]).toarray()
    return encoded_train_df, encoded_test_df, encoder


def normalize(numeric_columns):
    normalizer = StandardScaler()
    # Normalize Training Data
    numeric_train_df = normalizer.fit_transform(x_train[numeric_columns])
    # Normalize Training Data
    numeric_test_df = normalizer.fit_transform(x_test[numeric_columns])
    return numeric_train_df, numeric_test_df, normalizer


# Training German Credit Model using Decision Tree Classifier
def train_with_encoder(X):
    categorical_columns = ['checkingstatus', 'history', 'purpose', 'savings', 'employ', 'status', 'others',
                           'property', 'age', 'otherplans', 'housing', 'job', 'telephone', 'foreign']
    numerical_columns = [idx for idx in X.columns if idx not in categorical_columns]
    # encode categorical features
    encoded_train_df, encoded_test_df, encoder = encode(categorical_columns)

    # Normalize numerical features
    normalized_train_df, normalized_test_df, normalizer = normalize(numerical_columns)

    # Merge after encoding & normalizing
    train_df = np.concatenate((encoded_train_df, normalized_train_df), axis=1)
    test_df = np.concatenate((encoded_test_df, normalized_test_df), axis=1)

    # Train a decision tree classifier model
    d_tree_model = DecisionTreeClassifier(criterion='entropy', random_state=0)

    # Fit Model
    d_tree_model.fit(train_df, y_train.values)
    print("Model Trained Successfully!")

    # Metrics
    accuracy = d_tree_model.score(test_df, y_test.values)
    print("Model Accuracy: ", accuracy)

    model_obj = {"model": d_tree_model, "cat_columns": categorical_columns, "encoder": encoder, "normalizer": normalizer}

    return model_obj, accuracy


# Training German Credit Model using Decision Tree Classifier without Encoder
def train_without_encoder():
    # Train a decision tree classifier model
    d_tree_model = DecisionTreeClassifier(criterion='entropy', random_state=0)

    # Fit Model
    d_tree_model.fit(x_train.values, y_train.values)
    print("Model Trained Successfully!")

    # Metrics
    accuracy = d_tree_model.score(x_test.values, y_test.values)
    print("Model Accuracy: ", accuracy)

    return d_tree_model, accuracy


def train(data: pd.DataFrame, target_column: str, primary_column: str, is_encoded: bool=True):
    global x_train, x_test, y_train, y_test
    # Separate Training & Label data
    y = data[target_column]
    X = data.drop([target_column, primary_column], axis=1)

    # Splitting test and training data
    x_train, x_test, y_train, y_test = train_test_split(X, y, random_state=0)

    if is_encoded:
        # Train with Encoder
        model_obj, accuracy = train_with_encoder(X)
    else:
        # Train without Encoder
        model_obj, accuracy = train_without_encoder()
    
    return model_obj, accuracy

# Upload script only supports pkl file
def upload_model(api_endpoint, token, projectId, modelId, exp_name, model_pkl_file, algo, metrics, exp_title, exp_description):
    client = Cortex.client(api_endpoint=api_endpoint, project=projectId, token=token)
    experiment_client = ExperimentClient(client)
    result = experiment_client.save_experiment(exp_name, projectId, model_id=modelId,
                                               title=exp_title, description=exp_description)
    experiment = Experiment(result, projectId, experiment_client)
    with open(model_pkl_file, "rb") as model:
        with experiment.start_run() as run:
            run.log_artifact_stream("model", model)  # save model
            run.set_meta("algo", algo)
            [run.log_metric(k, metrics[k]) for k in metrics.keys()] # save meta data

def save_model(model_obj, metrics, api_endpoint, token, projectId, modelId, exp_name, exp_title, exp_description):
    # save as a pkl locally
    with open("model.pkl", 'wb') as file:
        pickle.dump(model_obj, file, protocol=2)
    
    client = Cortex.client(api_endpoint=api_endpoint, project=projectId, token=token)
    experiment_client = ExperimentClient(client)
    result = experiment_client.save_experiment(exp_name, projectId, model_id=modelId,
                                               title=exp_title, description=exp_description)
    experiment = Experiment(result, projectId, experiment_client)
    with open("model.pkl", "rb") as model:
        with experiment.start_run() as run:
            run.log_artifact_stream("model", model)  # save model
            [run.log_metric(k, metrics[k]) for k in metrics.keys()] # save meta data
    return True

def get_data(managed_content: ManagedContentClient, key: str, project: str) -> pd.DataFrame:
    """
    Function to load parquet dataframes form Managed Content

    @param managed_content: ManagedContentClient instance
    @param key: Cortex Managed Content key
    @param project: Cortex projectId
    @return: pd.DataFramce
    """
    response = managed_content.download(key,retries=1, project=project)
    content = response.read()
    pq_file = io.BytesIO(content)
    data = pd.read_parquet(pq_file)
    return data

def run(params):
    """
    Function to uses the skill params to extract profiles and dump to Managed Content

    @param payload: dictionary containing
        @param profileScehma: this is a first param
        @param targetColumn: The target column to train on
        @param experimentName: Experiment Name to use to save the model
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

    cortex_client = Cortex.client(
        api_endpoint=apiendpoint, verify_ssl_cert=True, token=token, project=project)
    managed_content = ManagedContentClient(cortex_client)
    data = get_data(managed_content, profile_schema + ".parquet", project)
    model_obj, accuracy = train(data, target_column, primary_column)
    metrics = {"accuracy": accuracy, "is_encoded": True}
    save_model(model_obj, metrics, apiendpoint, token, project, experiment_name, experiment_name, experiment_name, "Model trained on : "+profile_schema)

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
    #         "profileSchema": "german-credit-9afca",
    #         "experimentName": "german_credit",
    #         "targetColumn": "outcome",
    #         "primaryColumn": "id"
    #     }
    # }
    print(f'Parsing Payload : --')
    params["payload"] = extract_json_objects(params["payload"])
    print(f'Received: {params["payload"]}')
    run(params)
