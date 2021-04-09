from fastapi import FastAPI, HTTPException
import time, random
app = FastAPI()

@app.post('/invoke')
def run(request: dict):
    print("invoke request received {}".format(request))

    return {'payload': request}