"""
Copyright (c) 2020. Cognitive Scale Inc. All rights reserved.
"""


from cortex import Cortex
from fastapi import FastAPI

import numpy as np

model_ctx = {}

app = FastAPI()

@app.post('/init')
def load(req: dict):
    params = req["payload"]["params"]
    client = Cortex.client(api_endpoint=req["apiEndpoint"], project=req["projectId"], token=req["token"])
    exp_name = params["exp_name"]
    if not exp_name in model_ctx:
        model_ctx[exp_name] = init_model(exp_name, client)
    print("Loaded model : {}".format(exp_name))
    return {'payload': 'Loaded model'}
    
@app.post('/invoke')
def run(req: dict):
    params = req["payload"]["params"]
    instances = params["instances"]
    exp_name = params["exp_name"]

    # if model is not loaded
    if exp_name not in model_ctx:
        client = Cortex.client(api_endpoint=req["apiEndpoint"], project=req["projectId"], token=req["token"])
        model_ctx[exp_name] = init_model(exp_name, client)

    model_obj = model_ctx[exp_name]

    encoder = model_obj["encoder"]

    instances = np.array(instances, dtype=object)
    instances = instances if instances.ndim == 2 else np.reshape(instances, (1, -1))

    instances = encoder(instances)

    predictions = model_obj["model"].predict(instances)
    scores = model_obj["model"].predict_proba(instances)
    labels = model_obj["model"].classes_
    return {
        "predictions": predictions.tolist(),
        "scores": scores.tolist(),
        "labels": labels.tolist()
    }

def init_model(exp_name, client):
    experiment = client.experiment(exp_name)
    exp_run = experiment.last_run()
    return exp_run.get_artifact('model')