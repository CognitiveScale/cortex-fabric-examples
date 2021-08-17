from fastapi import FastAPI
from fastapi import Request

app = FastAPI()

@app.post('/invoke')
async def run(request: Request):
    request_body = await request.json()
    request_headers = dict(request.headers)
    return {'payload': request_headers}
