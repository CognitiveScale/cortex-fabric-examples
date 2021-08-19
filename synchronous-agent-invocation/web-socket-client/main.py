"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""
import os
import websocket
import time
import json
import urllib.parse
from threading import Thread
from cortex.utils import generate_token
from cortex.env import CortexEnv


def on_message(ws, message):
    """
    Callback object which is called when received data.
    on_message has 2 arguments.
    The 1st argument is this class object.
    The 2nd argument is utf-8 data received from the server.
    :param ws: WebSocketAPI
    :param message: Message String
    """
    msg = json.loads(message)
    if msg.get('eventType') == "agent.output":
        print("Agent Output: {}".format(msg["message"]))
    print(message)


def on_error(ws, error):
    """
    Callback object which is called when we get error.
    on_error has 2 arguments.
    The 1st argument is this class object.
    The 2nd argument is exception object.
    :param ws: WebSocketApp
    :param error: Exception Object
       """
    print("Error Connecting to Server: ", error)


def on_close(ws, *args):
    """
    Callback object which is called when connection is closed.
    on_close has 3 arguments.
    The 1st argument is this class object.
    The 2nd argument is close_status_code.
    The 3rd argument is close_msg.
    :param ws: WebSocketApp
    :param args: Args
    """
    print("### closed connection ###")


def on_open(ws, message):
    """
    Callback object which is called at opening websocket.
    on_open has one argument.
    The 1st argument is this class object.
    :param ws:
    :param message:
    """

    def run(*args):
        ws.send(json.dumps(message))
        time.sleep(20)
        ws.close()
        print("Thread terminating...")

    Thread(target=run).start()


# Get Environment details from local cli configuration
def get_env_details():
    """
    Environment Details from Local Cortex Configure
    :return: host, token, project
    """
    env = CortexEnv()
    token = generate_token(env.config)
    host = env.api_endpoint
    project = "sumanth-hello-world"
    return host, token, project


def read_config() -> dict:
    """
    Read Config as Dictionary
    :return: Config
    """
    return json.load(open(os.path.abspath(os.getcwd()) + '/../config.json', 'r'))


def invoke_agent(host, project, token, payload):
    """
    Invoke Agent using Web Sockets
    :param host: Cortex API Endpoint
    :param project: Project Name
    :param token: JWT Token
    :param payload: Input Payload
    """
    websocket.enableTrace(True)
    host = config.get("event_endpoint").format(host, project, config.get("agent"))
    headers = {'Authorization': 'Bearer {}'.format(token)}
    ws = websocket.WebSocketApp(url=host, header=headers, on_open=lambda *x: on_open(
        message={"action": "InvokeAgent", "payload": payload, "serviceName": config.get("service")}, *x),
                                on_message=on_message, on_error=on_error,
                                on_close=on_close)
    ws.run_forever()


def main(payload):
    global config
    print(f"Input Payload: {payload}")

    # read config
    config = read_config()

    # Get env details from cli configuration
    api_endpoint, token, project = get_env_details()

    # Extracting hostname form the api_endpoint as this is a web socket based invocation
    parsed_url = urllib.parse.urlsplit(api_endpoint)
    host = parsed_url.hostname

    # Invoke Agent
    invoke_agent(host, project, token, payload)


if __name__ == '__main__':
    payload = {"text": "Hello, World!"}
    main(payload)
