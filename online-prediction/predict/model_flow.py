"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""
import asyncio
import logging

import numpy as np
import pandas as pd
from cortex import Cortex

from predict.request_models import InvokeRequest

model = None


def load_model(api_endpoint: str, token: str, project_id: str, experiment_name: str, run_id: str):
    global model

    if not experiment_name:
        raise ValueError("experiment-name is required if a model is not initialized")

    # Initialize Cortex Client
    client = Cortex.client(api_endpoint=api_endpoint, token=token, project=project_id)

    # Load Model from the experiment run
    logging.info("Loading model artifacts from experiment run...")
    try:
        experiment = client.experiment(experiment_name)
        run = experiment.get_run(run_id) if run_id else experiment.last_run()
        model = run.get_artifact('model')
    except Exception as e:
        logging.error("Error: Failed to load model: {}".format(e))
        raise

    logging.info("Model Loaded!")


async def process(request: InvokeRequest):
    global model
    if not model:
        load_model(request.api_endpoint, request.token, request.project_id, request.properties.experiment_name,
                   request.properties.run_id)

    columns = request.payload.columns
    instances = request.payload.instances
    df = pd.DataFrame(columns=columns, data=instances)

    try:
        # If the model artifact is of type `dict`
        if isinstance(model, dict):
            categorical_cols = model["cat_columns"] if "cat_columns" in model else []
            numerical_cols = [x for x in df.columns if x not in categorical_cols]
            # Transforming the input data-frame using encoder & normalizer from the experiment artifact
            if ("encoder" in model) or ("normalizer" in model):
                x_encoded = model["encoder"].transform(
                    df[categorical_cols]).toarray() if "encoder" in model and categorical_cols else []
                x_normalized = model["normalizer"].transform(df[numerical_cols]) if "normalizer" in model else df[
                    numerical_cols].values
                if np.any(x_encoded) and np.any(x_normalized):
                    x_transformed = np.concatenate((x_encoded, x_normalized), axis=1)
                else:
                    x_transformed = x_encoded if np.any(x_encoded) else x_normalized
            else:
                x_transformed = df.values

            predictions = await model_predict(model["model"], x_transformed)
        else:
            # If the model object is an instance of model itself
            predictions = await model_predict(model, instances)
    except Exception as e:
        raise Exception("Error occurred while making predictions, Please check the model. Message: {}".format(e))

    return predictions


def execute_async(f):
    async def wraps(*args, **kwargs):
        return await asyncio.get_event_loop().run_in_executor(None, f, *args, *kwargs)
    return wraps


@execute_async
def model_predict(my_model, params):
    return my_model.predict(params)
