"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.
Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""
import uvicorn
from fastapi import FastAPI

app = FastAPI()


@app.post('/invoke')
def run(request: dict):
    print(request)
    return {"Response": request}


if __name__ == "__main__":
    # TODO: Update the host to your callback server IP
    uvicorn.run(app, host="0.0.0.0", port=8000)
