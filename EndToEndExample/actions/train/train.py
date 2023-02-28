"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.

"""

import time
import sys
import json
import random
import pickle
import warnings
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from cat_encoder import CatEncoder
from cortex import Cortex
from cortex.experiment import Experiment
from sklearn.tree import DecisionTreeClassifier
from sklearn.neural_network import MLPClassifier
from sklearn import svm
from sklearn.linear_model import LogisticRegression
import boto3

# supress all warnings
warnings.filterwarnings('ignore')


# download training data from s3 connection
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
    s3_components = path.split('/')
    bucket = s3_components[2]
    file_name = ""
    if len(s3_components) > 1:
        file_name = '/'.join(s3_components[3:])
    out_path = s3_components[-1]
    s3_client.download_file(bucket, file_name, out_path)
    return out_path


# function to pickle our models for later access
def pickle_model(model, encoder, model_name, test_accuracy, description, filename):
    model_obj = {'model': model, 'encoder': encoder, 'name': model_name,
                 'description': description, 'test_acc': test_accuracy,
                 'created': int(time.time())}
    with open(filename, 'wb') as file:
        pickle.dump(model_obj, file)


# save model metadata
def save_model(client, name, title, description, source, model_type, status, tags):
    # create model
    model_obj = {
        "name": name,
        "title": title,
        "description": description,
        "source": source,
        "type": model_type,
        "status": status,
        "tags": tags
    }
    result = client.models.save_model(model_obj)

    print(f'Model saved, name: {name}')


# save experiment using model
def save_experiment(client, experiment_name, filename, algo, model_id):
    # create experiment
    result = client.experiments.save_experiment(experiment_name, title=experiment_name, modelId=model_id)
    experiment = Experiment(client.experiments.get_experiment(experiment_name), client.experiments)
    run_id = None
    with open(filename, "rb") as model:
        with experiment.start_run() as run:
            run.log_artifact_stream("model", model)  # save model
            run.set_meta("algo", algo)  # save meta data
            run_id = run._id

    print(f'Experiment saved, name: {experiment_name} run_id: {run_id}')


# train model using the connection
def train(params):
    client = Cortex.from_message(params)

    payload = params['payload']
    # Read connection
    connection_name = payload['connection_name']
    print(f'Reading connection {connection_name}')
    connection = client.connections.get_connection(connection_name)
    conn_params = {}
    for p in connection['params']:
        conn_params.update({p['name']: p['value']})
    print({k:v for k,v in conn_params.items() if v not in ('secretKey', 'publicKey')})
    uri = conn_params["uri"]

    # Download training data using connection
    s3_client = init_s3_client(conn_params.get('publicKey'), conn_params.get('secretKey'))
    local_path = download_file(s3_client, uri)
    print(local_path)
    print(f'Downloaded training data for {connection_name}')

    random.seed(0)
    np.random.seed(0)

    # Load dataset
    data = pd.read_csv(local_path)

    # Separate outcome
    y = data['outcome']
    x = data.drop('outcome', axis=1)

    # Bring in test and training data
    x_train, x_test, y_train, y_test = train_test_split(x, y, random_state=0)

    # Create an encoder
    cat_columns = [
        'checkingstatus',
        'history',
        'purpose',
        'savings',
        'employ',
        'status',
        'others',
        'property',
        'age',
        'otherplans',
        'housing',
        'job',
        'telephone',
        'foreign'
    ]
    encoder = CatEncoder(cat_columns, x, normalize=True)
    encoded_x_train = encoder(x_train.values)
    encoded_x_test = encoder(x_test.values)

    # Train a decision tree model
    dtree = DecisionTreeClassifier(criterion='entropy', random_state=0)
    dtree.fit(encoded_x_train, y_train.values)
    dtree_acc = dtree.score(encoded_x_test, y_test.values)

    # Train a multi-layer perceptron model
    mlp = MLPClassifier(hidden_layer_sizes=(20, 20), max_iter=2000)
    mlp.fit(encoded_x_train, y_train.values)
    mlp_acc = mlp.score(encoded_x_test, y_test.values)

    # Train a support vector machine model
    SVM = svm.SVC(gamma='scale', probability=True)
    SVM.fit(encoded_x_train, y_train.values)
    svm_acc = SVM.score(encoded_x_test, y_test.values)

    # Train a logistic regression model
    logit = LogisticRegression(random_state=0, solver='lbfgs')
    logit.fit(encoded_x_train, y_train.values)
    logit_acc = logit.score(encoded_x_test, y_test.values)

    # Save model meta-data

    model_name = payload["model_name"]

    save_model(client, model_name, payload.get("model_title", ""), payload.get("model_description", ""),
               payload.get("model_source", ""), payload.get("model_type", ""), payload.get("model_status", ""), payload.get("model_tags", []))

    # Save models as pickle files and Save experiments
    pickle_model(dtree, encoder, 'Decision Tree', dtree_acc, 'Basic Decision Tree model', 'german_credit_dtree.pkl')
    pickle_model(logit, encoder, 'LOGIT', logit_acc, 'Basic LOGIT model', 'german_credit_logit.pkl')
    pickle_model(mlp, encoder, 'MLP', mlp_acc, 'Basic MLP model', 'german_credit_mlp.pkl')
    pickle_model(SVM, encoder, 'SVM', svm_acc, 'Basic SVM model', 'german_credit_svm.pkl')

    save_experiment(client, 'gc_dtree_exp', 'german_credit_dtree.pkl', 'DecisionTreeClassifier', model_name)
    save_experiment(client, 'gc_logit_exp', 'german_credit_logit.pkl', 'LogisticRegression', model_name)
    save_experiment(client, 'gc_mlp_exp', 'german_credit_mlp.pkl', 'MLPClassifier', model_name)
    save_experiment(client, 'gc_svm_exp', 'german_credit_svm.pkl', 'SVM', model_name)


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Message/payload commandline is required")
        exit(1)
    # The last argument in sys.argv is the payload from cortex
    train(json.loads(sys.argv[-1]))
