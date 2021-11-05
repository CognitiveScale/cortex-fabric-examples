"""
Copyright (c) 2020. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""

import sys
import json
import pickle
import numpy as np
from cortex import Cortex
from sklearn import datasets
from sklearn.ensemble import RandomForestClassifier

local_pickle_file = './model.pickle'


# Train and save model locally
def train_and_save_model():
    iris = datasets.load_iris()

    x = iris["data"][:, 3:]  # petal width
    y = (iris["target"] == 2).astype(np.int)

    clf = RandomForestClassifier(n_estimators=100, max_depth=2, random_state=0)
    clf.fit(x, y)

    # Save model
    pickle.dump(clf, open(local_pickle_file, "wb"))


# The starting point for the job
if __name__ == '__main__':
    # Get agent/skill activation request body
    request_body = json.loads(sys.argv[1])
    api_endpoint = request_body["apiEndpoint"]
    project = request_body["projectId"]
    token = request_body["token"]
    experiment_name = request_body["payload"]["experiment_name"]

    train_and_save_model()

    # Create Cortex client and create experiment
    client = Cortex.client(api_endpoint=api_endpoint, project=project, token=token)
    experiment = client.experiment(experiment_name)

    # Upload model to experiment run in Cortex
    model = open(local_pickle_file, "rb")
    run = experiment.start_run()
    run.log_artifact_stream("model", model)
    run.set_meta("algo", "RandomForestClassifier Model")

    print(f'Created experiment "{experiment_name}". Started Run {run.id}. Logged RandomForestClassifier model.')
