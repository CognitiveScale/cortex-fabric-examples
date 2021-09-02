"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""
import logging
import traceback

import uvicorn

from fastapi import FastAPI, Response, HTTPException

from predict.request_models import InvokeRequest, InitializeRequest
from predict.model_flow import load_model, process

app = FastAPI()


@app.get('/health')
async def health():
    return Response(status_code=200)


@app.post('/init')
def init(request: InitializeRequest):
    logging.info("Online Prediction: Init Request:{}".format(request))

    try:
        load_model(request.api_endpoint, request.token, request.project_id, request.properties.experiment_name,
                   request.properties.run_id)
    except ValueError as e:
        logging.error(traceback.format_exc())
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logging.error(traceback.format_exc())
        raise HTTPException(status_code=412, detail=str(e))

    return {
        "payload": "model loaded successfully"
    }


@app.post('/invoke')
async def run(request: InvokeRequest):
    logging.info("Online Prediction: Invoke Request:{}".format(request))

    try:
        predictions = await process(request)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        logging.error(traceback.format_exc())
        raise HTTPException(status_code=412, detail=str(e))

    return {
        "payload": predictions.tolist()
    }


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=5000)
