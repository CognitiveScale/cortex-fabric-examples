"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""
import logging
import uvicorn
import numpy as np
import pandas as pd

# cortex
from cortex import Cortex

# fastapi
from fastapi import FastAPI

app = FastAPI()


@app.post('/invoke')
async def run(request: dict):
    logging.info("Online Prediction: Invoke Request:{}".format(request))
    columns = request['payload']["columns"]
    instances = request['payload']["instances"]
    df = pd.DataFrame(columns=columns, data=instances)
    # Initialize Cortex Client
    client = Cortex.client(api_endpoint=request["apiEndpoint"], token=request["token"], project=request["projectId"])

    # Load Model from the experiment run
    logging.info("Loading model artifacts from experiment run...")
    model = await load_model(client, request["properties"]["experiment-name"], request["properties"]["run-id"])
    logging.info("Model Loaded!")
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
            predictions = model["model"].predict(x_transformed)
        else:
            # If the model object is an instance of model itself
            predictions = model.predict(instances)
    except Exception as e:
        raise Exception("Error occurred while making predictions, Please check the model. Message: {}".format(e))
    return {'payload': predictions.tolist()}


async def load_model(client, experiment_name, run_id):
    try:
        experiment = client.experiment(experiment_name)
        run = experiment.get_run(run_id)
        return run.get_artifact('model')
    except Exception as e:
        logging.error("Error: Failed to load model: {}".format(e))


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=5000)
