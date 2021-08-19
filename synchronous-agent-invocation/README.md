### Synchronous Agent Invocation Example

Examples to demonstrate the synchronous agent invoke capabilities

##### Prerequisites:
* Python 3.7
* Cortex Python SDK v6
* Cortex CLI configured with env details

##### File Structure:

* `sync-true-example/src/main.py` Python code that invokes the Agent using Sync=True
* `client-callback-example/src/main.py` Python code that invokes the Agent using callbacks
* `web-socket-client/main.py` Python code that invokes the Agent using web sockets.
* `config.json` Configuration file
* `requirements.txt` Python libraries

#### Examples:
#####1. Using `sync=true` flag while invoking the Agent service endpoint:

Adding `sync=true` as a query param to the POST request like below
    
Invoke Endpoint: https://api.cortex.insights.ai/fabric/v4/projects/project-name/agentinvoke/agent-name/services/service-name
    
            headers = {
                "content-type": "application/json",
                "authorization": "Bearer {}".format(token)
            }
            requests.request("POST", url, data=json.dumps({"payload": payload}), headers=headers,
                                        params={"sync": True})
    
  > Sample Payload:
        
            {"text": "Hello, World!"}
     
* Note: This may not work always as it depends on the type of Skill used downstream (like Merge)

#####2. Using `Callback` functionality of processor gateway to invoke agent:

Adding `callbackUrl` to the properties and passing them in request body along with payload enables us to get the results of agent invocation directly without polling the get activation function. Once the request is processed, the `callbackUrl` endpoint will get a callback from processor gateway with results. 

            headers = {
                "content-type": "application/json",
                "authorization": "Bearer {}".format(token)
            }
            requests.request("POST", url, data=json.dumps({"payload": params["payload"],
                "properties": params["properties"]}), headers=headers)
   
   > Sample Payload:
        
            {
                "payload": {
                    "text": "Hello, World!"
                 },
                "properties": {
                # TODO: Update the host to your callback server IP
                    "callbackUrl": "http://callback.host.ip:8000/invoke"
                 }
             }
Note: The server used for getting callback should be accessible to cortex.

#####3. Using `WebSocket Client` to invoke agent:

Connecting to Websocket Server of Cortex using web-socket python client to make agent invoke requests using input payload and getting responses from the server as callbacks.

Update `config.json` with `Agent` & `Service` details before running this example.
       
1. Initializing Web Socket Client App:
            
                ws = websocket.WebSocketApp(url=host, header=headers, on_open=on_open,
                                on_message=on_message, on_error=on_error,
                                on_close=on_close)
2. Headers & Sample Input Payload:

            headers = {
                "authorization": "Bearer {}".format(token)
            }
            
            payload = {"text": "Hello, World!"}
            
Run `main.py` from `web-socket-client/main.py` and you should see events from websocket server & client.

Sample Event:
        
        {"eventType":"agent.input","requestId":"6ada0b84-c6fd-4fdf-a1af-680ad747ccdf","agentName":"test-agent-4f2ac",
        "serviceName":"input","channelId":"4dda2512-55d8-4ebd-aea5-d419c7de6cdc","sessionId":"6ada0b84-c6fd-4fdf-a1af-680ad747ccdf",
        "projectId":"sumanth-hello-world","token":"UOjtT1U2fV72rPl94C80B8CXhDA",
        "username":"cortex@example.com","payload":{"params":"Text"},"timestamp":1629368107550}


For more information on Agents, please refer to cortex fabric docs: https://cognitivescale.github.io/cortex-fabric/docs/build-agents/agents