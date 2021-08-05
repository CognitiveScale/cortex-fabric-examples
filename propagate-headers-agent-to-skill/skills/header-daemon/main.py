from fastapi import FastAPI
from fastapi import Request

app = FastAPI()

@app.post('/invoke')
async def run(request: Request):
    requestBody = await request.json()
    requestHeaders = dict(request.headers)
    return {'payload': requestHeaders}