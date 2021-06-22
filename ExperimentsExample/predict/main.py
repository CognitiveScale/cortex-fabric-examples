"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.
"""

from cortex import Cortex
from fastapi import FastAPI
from cat_encoder import CatEncoder

import numpy as np

# model context
model_ctx = {}

app = FastAPI()


# load latest model by experiment name
@app.post('/init')
def load(req: dict):
    params = req["payload"]["params"]
    client = Cortex.client(api_endpoint=req["apiEndpoint"], project=req["projectId"], token=req["token"])
    exp_name = params["exp_name"]
    run_id = params["run_id"]
    if not exp_name in model_ctx:
        model_ctx[exp_name] = init_model(exp_name, run_id, client)
    print("Loaded model : {}".format(exp_name))
    return {'payload': 'Loaded model'}


# predict
@app.post('/invoke')
def run(req: dict):
    payload = req["payload"]
    instances = payload["instances"]
    exp_name = payload["exp_name"]
    run_id = payload["run_id"]

    # if model is not loaded
    if exp_name not in model_ctx:
        client = Cortex.client(api_endpoint=req["apiEndpoint"], project=req["projectId"], token=req["token"])
        model_ctx[exp_name] = init_model(exp_name, run_id, client)

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
        "predictions": predictions.tolist(),
        "scores": scores.tolist(),
        "labels": labels.tolist()
    }


# initialize model using experiment name
def init_model(exp_name, run_id, client):
    experiment = client.experiment(exp_name)
    if not run_id:
        exp_run = experiment.last_run()
    else:
        exp_run = experiment.get_run(run_id)
    return exp_run.get_artifact('model')
