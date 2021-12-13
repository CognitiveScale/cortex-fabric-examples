from fastapi import FastAPI
import uvicorn
import logging
logger = logging.getLogger(__name__)
from main import main

app = FastAPI()

@app.post('/invoke')
def run(request: dict):
    logger.info(f"request params: {request}")
    res = main(request)
    logger.info(f"response: {res}")
    return {'payload': res}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=6000)