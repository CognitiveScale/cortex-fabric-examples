"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.
"""

from cortex import Cortex
from cortex.experiment import Experiment, ExperimentClient
from fastapi import FastAPI

import numpy as np
from cat_encoder import CatEncoder

# model context
model_ctx = {}

app = FastAPI()

# predict
@app.post('/invoke')
def run(req: dict):
    payload = req["payload"]
    instances = payload["instances"]
    exp_name = payload["exp_name"]
    run_id = None
    if "run_id" in payload:
        run_id = payload["run_id"]

    # if model is not loaded
    client = Cortex.client(api_endpoint=req["apiEndpoint"], project=req["projectId"], token=req["token"])
    
    model_ctx[exp_name] = init_model(exp_name, run_id, client, req["projectId"])

    # retrieve model from the context
    model_obj = model_ctx[exp_name]

    # using encoder from model object
    encoder = model_obj["encoder"]

    instances = np.array(instances, dtype=object)
    instances = instances if instances.ndim == 2 else np.reshape(instances, (1, -1))

    instances = encoder(instances)

    # predict
    predictions = model_obj["model"].predict(instances)
    scores = model_obj["model"].predict_proba(instances)
    labels = model_obj["model"].classes_
    return {
        "payload": {
            "predictions": predictions.tolist(),
            "scores": scores.tolist(),
            "labels": labels.tolist()
        }
    }


# initialize model using experiment name
def init_model(exp_name, run_id, client, project):
    experiment = Experiment(client.experiments.get_experiment(exp_name), client.experiments)
    if not run_id:
        exp_run = experiment.last_run()
    else:
        exp_run = experiment.get_run(run_id)
    return exp_run.get_artifact('model')