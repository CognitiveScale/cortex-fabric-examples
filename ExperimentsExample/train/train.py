"""
Copyright (c) 2020. Cognitive Scale Inc. All rights reserved.

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
from sklearn.tree import DecisionTreeClassifier
from sklearn.neural_network import MLPClassifier
from sklearn import svm
from sklearn.linear_model import LogisticRegression
import boto3

# supress all warnings
warnings.filterwarnings('ignore')

def download_training_data(connection, client):
    if connection:
        for param in connection['params']:
            if param['name'] == 'uri':
                uri = param['value']
            elif param['name'] == 'publicKey':
                s3_key = param['value']
            elif param['name'] == 'secretKey':
                s3_secret = client.get_secret(param['value'].split('.')[1])
        s3_components = uri[5:].split('/')
        bucket = s3_components[0]
        file_name = ""
        if len(s3_components) > 1:
            file_name = '/'.join(s3_components[1:])
        s3_conn = boto3.client(
            's3',
            aws_access_key_id=s3_key,
            aws_secret_access_key=s3_secret
            )
        s3_conn.download_file(bucket, file_name, file_name)
    
# function to pickle our models for later access
def pickle_model(model, encoder, model_name, test_accuracy, description, filename):
    model_obj = {'model': model, 'encoder': encoder, 'name': model_name,
                 'description': description, 'test_acc': test_accuracy,
                 'created': int(time.time())}
    with open(filename, 'wb') as file:
        pickle.dump(model_obj, file)
    print(f"Saved: {model_name}")

def save_experiment(client, experiment_name, filename, algo):
    #create experiment
    experiment = client.experiment(experiment_name)
    with open(filename, "rb") as model:
        with experiment.start_run() as run:
            run.log_artifact_stream("model", model)  # save model
            run.set_meta("algo", algo)  # save meta data
    print(f'Experiment saved {experiment_name}')
        
def train(params):
    
    # create a Cortex client instance from the job's parameters
    client = Cortex.client(api_endpoint=params['apiEndpoint'], project=params['projectId'], token=params['token'])
    
    # Read connection
    connection_name = params['payload']['connection_name']
    connection = client.get_connection(connection_name)
    
    # Download training data using connection
    download_training_data(connection, client)
    
    random.seed(0)
    np.random.seed(0)

    # Load dataset
    data = pd.read_csv('german_credit_eval.csv')

    # Separate outcome
    y = data['outcome']
    x = data.drop('outcome',axis=1)

    # Bring in test and training data
    x_train, x_test, y_train, y_test = train_test_split(x, y, random_state = 0)

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
    dtree = DecisionTreeClassifier(criterion = 'entropy', random_state = 0)
    dtree.fit(encoded_x_train, y_train.values)
    dtree_acc = dtree.score(encoded_x_test,y_test.values)

    # Train a multi-layer perceptron model
    mlp = MLPClassifier(hidden_layer_sizes=(20,20),max_iter=2000)
    mlp.fit(encoded_x_train, y_train.values)
    mlp_acc = mlp.score(encoded_x_test,y_test.values)

    # Train a support vector machine model
    SVM = svm.SVC(gamma='scale')
    SVM.fit(encoded_x_train, y_train.values)
    svm_acc = SVM.score(encoded_x_test,y_test.values)

    # Train a logistic regression model
    logit = LogisticRegression(random_state=0, solver='lbfgs')
    logit.fit(encoded_x_train, y_train.values)
    logit_acc = logit.score(encoded_x_test,y_test.values)

    # Save models as pickle files and Save experiments
    pickle_model(dtree, encoder, 'Decision Tree', dtree_acc, 'Basic Decision Tree model', 'german_credit_dtree.pkl')
    save_experiment(client, 'gc_dtree_exp', 'german_credit_dtree.pkl', 'DecisionTreeClassifier')
    pickle_model(logit, encoder, 'LOGIT', logit_acc, 'Basic LOGIT model', 'german_credit_logit.pkl')
    save_experiment(client, 'gc_logit_exp', 'german_credit_logit.pkl', 'LogisticRegression')
    pickle_model(mlp, encoder, 'MLP', mlp_acc, 'Basic MLP model', 'german_credit_mlp.pkl')
    save_experiment(client, 'gc_mlp_exp', 'german_credit_mlp.pkl', 'MLPClassifier')
    pickle_model(SVM, encoder, 'SVM', svm_acc, 'Basic SVM model', 'german_credit_svm.pkl')
    save_experiment(client, 'gc_svm_exp', 'german_credit_svm.pkl', 'SVM')


if __name__ == "__main__":
    if len(sys.argv)<2:
        print("Message/payload commandline is required")
        exit(1)
    # The last argument in sys.argv is the payload from cortex
    train(json.loads(sys.argv[-1]))
