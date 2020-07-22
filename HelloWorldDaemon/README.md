# Hello Daemon example (cortex/Hello_daemon_example)
The `cortex/Hello_daemon_example` agent consists of a single skill `cortex/hello-daemon-skill`.
- `cortex/hello-daemon-skill` reads `text` from the payload and responds with a message.  

## Requirements
- Python 3.x
- Docker client
- Bash shell ( Power shell TBA )
- Cortex client
- URL/Credentials for a cortex instance

## Deploying
First sign into cortex
```
cortex configure
```

Then build the docker containers and deploy the agent, skills, and actions to cortex.
```
./deploy.sh
```

## Invoking the agent
After deploying the agent and skills you can invoke the agent using the cortex cli.
```
cortex agents invoke cortex/Hello_daemon_example input --params '{"payload":{"text":"This is a test"}}'
```

You'll get a response with an `activationId` like below:
```
{
  "success": true,
  "activationId": "d14bae04-2817-4f68-84a7-b71d56129d71",
  "instanceId": "5f0e1d5980757c8592b7ce37",
  "sessionId": "292f1b61-cd61-486c-916c-572de7e7ba04",
  "message": "Service activated"
}
```

Use cortex's cli to get the activation
```
cortex agents get-activation d14bae04-2817-4f68-84a7-b71d56129d71
```
The activation response will include the stdout from the dataconsumer job.
```
{
  "activationId": "d14bae04-2817-4f68-84a7-b71d56129d71",
  "instanceId": "5f0e1d5980757c8592b7ce37",
  "channelId": "d31b8e6c-e192-40c5-b158-6aa1f68cc178",
  "sessionId": "4990891e-662b-4564-9003-3498ec0af7ba",
  "tenantId": "jgtenant",
  "username": "admin",
  "start": 1594839578722,
  "inputServiceName": "input",
  "outputServiceName": "output",
  "status": "COMPLETE",
  "end": 1594839690094,
  "response": {"message": "Hello received: 'This is a test', word count: 4"},
  "type": "ServiceActivation"
}
```

## Developing python code
Create python virtual env
```
virtualenv venv
source ./venv/bin/activate
pip install -r skills/hellodaemon/requirements.txt
```

The scripts below uses `jq`,  this is an optional tool for parsing/scripting with JSON files.
You can manually copy/paste the apiEndpoint and token from your ~/.cortex/config file.

Running the daemon from the command-line
```
python ./skills/hellodaemon/daemon.py"    
```

Invoking the daemon
```
curl -X POST http://localhost:5000/v1/hello -d '{"payload":{"text":"Calling from command-line."}}'
```
