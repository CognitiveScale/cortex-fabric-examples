import datetime
from fastapi import FastAPI

app = FastAPI()

model = None

@app.post('/invoke')
def run(params: dict):

    try:
        payload = params["payload"]
        profile = payload["profiles"]
        intervention = payload["intervention_id"]
        message = ""
        try:
            message = "Successfully invoked action for given profile ID "+str(profile)+"for intervention "+str(intervention)
        except Exception as e:
            print("Error in invoking action")

        print(datetime.datetime.now())
        return {"response": {'message': message}}
    except Exception as e:
        print(e)
        raise




