# Job Webhook example (cortex/Job_Webhook_Example)
The `cortex/Job_Webhook_Example` agent consists of two skills `cortex/simplejob-skill` and `cortex/webhook-skill`.
- `cortex/simplejob-skill` sleeps for 20 seconds and returns some print statements in the payload.
- `cortex/webhook-skill` uses the `webhook-url` property in its skill definition to post the payload from the previous job to a web hook.

This example uses an external site  https://webhook.site for testing web hooks.  
You can navigate to this site to see the results of the web hook.

## Requirements
- Python 3.x
- Docker client
- Bash shell ( Power shell TBA )
- Cortex client
- URL/Credentials for a cortex instance

## Deploying
First, navigate to https://webhook.site and obtain the url generated for your webhook.

Update `skills/webhook/skill.yaml` with the new URL.

Then sign into cortex
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
cortex agents invoke cortex/Web_Hook_Example input
```

You'll get a response with an `activationId` like below:
```
{
  "success": true,
  "activationId": "22c8ca6f-580a-443d-917a-0ec8045f8c48",
  "instanceId": "5f18774d97d563363b0da749",
  "sessionId": "ecd0c456-a5c3-4cb1-a12c-88aaa51eae78",
  "message": "Service activated"
}
```

## Developing python code
Create python virtual env
```
virtualenv venv
source ./venv/bin/activate
pip install -r skills/webhook/requirements.txt
```

The scripts below uses `jq`,  this is an optional tool for parsing/scripting with JSON files.
You can manually copy/paste the apiEndpoint and token from your ~/.cortex/config file.

Running the web hook job from the command-line
```
python ./skills/webhook/job.py "{\"apiEndpoint\":\"$(jq -r .profiles.default.url  ~/.cortex/config)\",\"token\":\"$(jq -r .profiles.default.token  ~/.cortex/config)\", \"payload\":{\"message\": \"Test message\"}, \"properties\":{\"webhook-URL\":\"https://webhook.site/71db092b-11c5-4f2a-954e-f63d1b97faae\"}}"    

```
