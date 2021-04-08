from fastapi import FastAPI, HTTPException
import requests, json
import time, random
from sessions import SessionsAPI
app = FastAPI()

@app.post('/invoke')
def run(request: dict):
    """
    invoke
    :param request:
    :return:
    """
    print("Properties: {}".format(request['properties']))
    ## get parrams
    payload, properties, apiEndpoint, token, projectId, sessionId = get_params(request)
    merge_property = properties.get("merge.property", None)
    merge_result_name = properties.get("merge.result_name", None)

    ## dummy responses
    listt = [{"id":"5001","type":"None"},{"id":"5002","type":"Glazed"},{"id":"5005","type":"Sugar"},{"id":"5007","type":"Powdered Sugar"},{"id":"5006","type":"Chocolate with Sprinkles"},{"id":"5003","type":"Chocolate"},{"id":"5004","type":"Maple"}]
    choice_res = random.choice(listt)

    ## make unique session key with merge property and result name
    session_key = merge_property + "."+merge_result_name
    sessions_api = SessionsAPI(apiEndpoint, projectId, token)

    s_res = sessions_api.post_by_key(session_key, choice_res, sessionId)
    print("Session Post Response",  s_res)

    return {'payload': choice_res}

def get_params(request):
    """
    get params
    :param request:
    :return:
    """
    payload = request['payload']
    properties = request['properties']
    apiEndpoint = request['apiEndpoint']
    token = request['token']
    projectId = request['projectId']
    sessionId = request['sessionId']
    return payload, properties, apiEndpoint, token, projectId, sessionId
