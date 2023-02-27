from fastapi import FastAPI

from cortex import Cortex

app = FastAPI()


@app.post('/start')
def start(req: dict):
    payload = req['payload']
    client = Cortex.client(api_endpoint=req["apiEndpoint"], project=req["projectId"], token=req["token"])
    ttl = None
    description = "No description given"
    if "ttl" in payload:
        ttl = payload["ttl"]
    if "description" in payload:
        description = payload["description"]
    session = client.sessions.start_session(ttl, description, req["projectId"])
    return {'payload': {"session_id": session}}


@app.post('/get')
def get(req: dict):
    payload = req['payload']
    client = Cortex.client(api_endpoint=req["apiEndpoint"], project=req["projectId"], token=req["token"])

    session_id = None
    if "session_id" in payload:
        session_id = payload["session_id"]
    else:
        return {'payload': "session_id is required"}
    key = None
    if "key" in payload:
        key = payload["key"]
        if len(key) < 1:
            key = None
    session = client.sessions.get_session_data(session_id, key, req["projectId"])
    return {'payload': session}


@app.post('/put')
def put(req: dict):
    payload = req['payload']
    client = Cortex.client(api_endpoint=req["apiEndpoint"], project=req["projectId"], token=req["token"])

    session_id = None
    if "session_id" in payload:
        session_id = payload["session_id"]
    else:
        return {'payload': "session_id is required"}
    data = {}
    if "data" in payload:
        data = payload["data"]
    else:
        return {'payload': "data is required"}
    result = client.sessions.put_session_data(session_id, data, req["projectId"])
    return {"payload": result}


@app.post('/delete')
def delete(req: dict):
    payload = req['payload']
    client = Cortex.client(api_endpoint=req["apiEndpoint"], project=req["projectId"], token=req["token"])

    session_id = None
    if "session_id" in payload:
        session_id = payload["session_id"]
    else:
        return {'payload': "session_id is required"}
    result = client.sessions.delete_session(session_id, req["projectId"])
    return {"payload": result}
