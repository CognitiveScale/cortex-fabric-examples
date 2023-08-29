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
from sensa import Sensa
from sensa.experiment import Experiment
from sklearn.tree import DecisionTreeClassifier
from sklearn.neural_network import MLPClassifier
from sklearn import svm
from sklearn.linear_model import LogisticRegression
import boto3

# supress all warnings
warnings.filterwarnings('ignore')


# download training data from s3 connection
def download_training_data(connection):
    if connection:
        uri = ''
        s3_key = ''
        s3_secret = ''
        for param in connection['params']:
            if param['name'] == 'uri':
                uri = param['value']
            elif param['name'] == 'publicKey':
                s3_key = param['value']
            elif param['name'] == 'secretKey':
                s3_secret = param['value']
        s3_components = uri[6:].split('/')
        bucket = s3_components[0]
        file_name = ""
        if len(s3_components) > 1:
            file_name = '/'.join(s3_components[1:])
        s3_conn = boto3.client('s3', aws_access_key_id=s3_key, aws_secret_access_key=s3_secret)
        s3_conn.download_file(bucket, file_name, file_name)


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
    client.models.save_model(model_obj)
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
    project = params['projectId']
    # create a Cortex client instance from the job's parameters
    # client = Sensa.client(api_endpoint=params['apiEndpoint'], project=project, token=params['token'])
    client = Cortex.from_message(params)

    payload = params['payload']
    # Read connection
    connection_name = payload['connection_name']
    print(f'Reading connection {connection_name}')
    connection = client.connections.get_connection(connection_name)

    # Download training data using connection
    download_training_data(connection)
    print(f'Downloaded training data for {connection_name}')

    random.seed(0)
    np.random.seed(0)

    # Load dataset
    data = pd.read_csv('german_credit_eval.csv')

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
    # The last argument in sys.argv is the payload from sensa
    train(json.loads(sys.argv[-1]))
