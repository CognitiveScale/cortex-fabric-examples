"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""
import os
import sys
import json
import requests

from sensa.utils import generate_token
from sensa.env import SensaEnv


def read_config() -> dict:
    """
    Read Config as Dictionary
    :return: Config
    """
    return json.load(open(os.path.abspath(os.getcwd()) + '/../../config.json', 'r'))


# Get Environment details from local configuration
def get_env_details():
    """
    Environment Details from Local Cortex Configure
    :return: host, token, project
    """
    env = SensaEnv()
    token = generate_token(env.config)
    host = env.api_endpoint
    project = env.project
    return host, token, project


def invoke_agent(api_endpoint, project, token, params):
    """
    Invoke Agent Call
    :param api_endpoint: API Endpoint
    :param project: Project Name
    :param token: JWT
    :param params: Payload
    :return:
    """
    url = (config.get("invoke_endpoint")).format(api_endpoint, project, config.get("agent"), config.get("service"))
    headers = {
        'content-type': "application/json",
        'authorization': "Bearer {}".format(token)
    }
    response = requests.request("POST", url,
                                data=json.dumps({"payload": params["payload"], "properties": params["properties"]}),
                                headers=headers)
    if response.headers.get('Content-Type') == 'application/json':
        agent_res = json.loads(response.text)
        print(f"Agent Response: {agent_res}")
        activation_id = agent_res['activationId']
    else:
        raise ValueError(
            "Response from server is not json, this happens usually if token is expired,"
            " regenerate token and try again response = {}".format(response.text))
    return activation_id


def main(params):
    global config
    print(f"Input Payload: {params}")

    # Get env details from cli configuration
    api_endpoint, token, project = get_env_details()

    # read config
    config = read_config()

    # Invoke Agent
    activation_id = invoke_agent(api_endpoint, project, token, params)

    # Get Activation Results
    return {"payload": {"message": "Your request is being processed. You will get a callback once done",
                        "activation_id": activation_id}}


if __name__ == '__main__':
    params = {"payload": {"text": "Hello, World!"}, "properties": {"callbackUrl": "http://778621b0a55d.ngrok.io/invoke"}}
    if len(sys.argv) > 1:
        params = sys.argv[1]
    print(main(params))
