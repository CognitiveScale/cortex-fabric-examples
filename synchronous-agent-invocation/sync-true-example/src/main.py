"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""
import sys
import json
import requests
import os.path

from cortex.utils import generate_token
from cortex.env import CortexEnv


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
    env = CortexEnv()
    token = generate_token(env.config)
    host = env.api_endpoint
    project = env.project
    return host, token, project


def invoke_agent(api_endpoint, project, token, payload):
    """
    Invoke Agent Call
    :param api_endpoint: API Endpoint
    :param project: Project Name
    :param token: JWT
    :param payload: Payload
    :return:
    """
    url = (config.get("invoke_endpoint")).format(api_endpoint, project, config.get("agent"), config.get("service"))
    headers = {
        'content-type': "application/json",
        'authorization': "Bearer {}".format(token)
    }
    response = requests.request("POST", url, data=json.dumps({"payload": payload}), headers=headers,
                                params={"sync": True})
    if 'application/json' in response.headers.get('Content-Type'):
        agent_res = json.loads(response.text)
        activation_id = agent_res['activationId']
    else:
        raise ValueError(
            "Response from server is not json, this happens usually if token is expired,"
            " regenerate token and try again response = {}".format(response.text))
    return activation_id


def get_activation(host, project, token, activation_id):
    """
    Get Activation
    :param host: ApiEndpoint
    :param project: Project Name
    :param token: JWT
    :param activation_id: ActivationID or requestID from Invoke
    :return:
    """
    url = config.get("activation_endpoint").format(host, project, activation_id)
    headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer {}'.format(token)
    }
    response = requests.request("GET", url, headers=headers)
    return json.loads(response.text)


def main(payload):
    global config
    print(f"Input Payload: {payload}")

    # Get env details from cli configuration
    api_endpoint, token, project = get_env_details()

    # read config
    config = read_config()

    # Invoke Agent
    activation_id = invoke_agent(api_endpoint, project, token, payload)

    # Get Activation Results
    return get_activation(api_endpoint, project, token, activation_id)


if __name__ == '__main__':
    payload = {"text": "Hello, World!"}
    if len(sys.argv) > 1:
        payload = sys.argv[1]
    response = main(payload)
    print({"Agent Invoke response": response})
