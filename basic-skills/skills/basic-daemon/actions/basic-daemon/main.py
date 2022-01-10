"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Template/Example Code [License](https://github.com/CognitiveScale/cortex-code-templates/blob/main/LICENSE.md)
"""

from fastapi import FastAPI

app = FastAPI()


@app.post('/invoke')
def run(request_body: dict):
    payload = request_body['payload']
    return {'payload': payload}
