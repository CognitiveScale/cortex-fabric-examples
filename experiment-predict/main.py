from cortex import Cortex
from fastapi import FastAPI

app = FastAPI()

model = None


@app.post('/invoke')
def run(req: dict):
    payload = req['payload']
    instance = payload["instance"]
    # Load saved model if not loaded already
    global model
    if not model:
        client = Cortex.client(api_endpoint=req["apiEndpoint"], project=req["projectId"], token=req["token"])
        experiment = client.experiment('sample_experiment')

        exp_run = experiment.last_run()
        model = exp_run.get_artifact('model')

    # invoke model
    return {'payload': model.predict(instance).tolist()}
