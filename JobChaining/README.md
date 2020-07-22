# Job Chaining example (cortex/Job_Chaining_Example)
The `cortex/Job_Chaining_Example` agent consists of two skills `cortex/datagenerator-skill` and `cortex/dataconsumer-skill`.
`cortex/datagenerator-skill` generates a data file of json records and writes the datafile to cortex's managed content with a unique filename.  
The filename uses the `activationId` which is passed to all skills/actions during an agent invoke.  
The `cortex/dataconsumer-skill` reads the file from managed content and computes a color count from the data file.

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
cortex agents invoke cortex/Job_Chaining_Example input
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
  "response": "\"Got payload: Got payload: {}\\nDONE GENERATING DATA\\nWrote datafile to managed content key: jobchain-data-d14bae04-2817-4f68-84a7-b71d56129d71\\n\\nFetching datafile from managed content: jobchain-data-d14bae04-2817-4f68-84a7-b71d56129d71\\n{\\\"green\\\":266,\\\"yellow\\\":265,\\\"blue\\\":242,\\\"red\\\":227}\\n\"",
  "type": "ServiceActivation"
}
```

## Developing python code
Create python virtual env
```
virtualenv venv
source ./venv/bin/activate
pip install -r skills/dataconsumer/requirements.txt
```

The scripts below uses `jq`,  this is an optional tool for parsing/scripting with JSON files.
You can manually copy/paste the apiEndpoint and token from your ~/.cortex/config file.

Running the data generator from the command-line
```
python ./skills/datagenerator/job.py "{\"apiEndpoint\":\"$(jq -r .profiles.default.url  ~/.cortex/config)\",\"token\":\"$(jq -r .profiles.default.token  ~/.cortex/config)\", \"payload\":{\"recordCount\": 10}}"    

```

Running the data consumer code from the command-line
```
python ./skills/dataconsumer/job.py "{\"apiEndpoint\":\"$(jq -r .profiles.default.url  ~/.cortex/config)\",\"token\":\"$(jq -r .profiles.default.token  ~/.cortex/config)\", \"payload\":{\"datafileKey\":\"<jobchain-data-key>\"}}"

```
