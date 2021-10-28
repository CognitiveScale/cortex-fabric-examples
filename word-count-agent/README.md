# Word Count Agent Example (word-count-agent)

`word-count-daemon` reads `text` from the payload and responds with a word count.
The `word-count-agent` Agent uses the `word-count-daemon` Skill.

## Purpose
In the Example an Action with the logic that is executed is created. It is encapsulated in a Skill that provides the internal routing. Finally, that Skill is added to an Agent with input and output services that direct the data flow through the Skill.


## Included Files to Review
- `skills/` - The directory that houses the Skills
    - `word-count-daemon/` - The contents of the word-count-daemon action
        - `Dockerfile` - Builds the Docker image the action
        - `main.py` - Code for Cortex daemon
        - `requirements.txt` - Dependencies and libraries
        - `skill.yaml` - Skill definition and action mapping
- `agent.yaml` - Agent definition and Skill mapping
- `deploy.sh` - Builds and pushes images, deploys actions, saves Skills and saves Agents.
- `README.md` - Provides the objectives, requirements, and instructions for generating and deploying the Skill.


## Requirements
- Python 3.6+
- Docker Desktop 3.6.0 (client)
- Bash shell or PowerShell
- URL/Credentials for a Cortex v6 Domain



## Steps


### 1. Login to the CLI
Refer to the Training Module "Introduction to CLI" for steps on how to log in to the CLI.


### 2. Deploy
Then build the Docker containers and deploy the Agent, Skills, and actions to Cortex. Run the command below:
```shell
$ ./deploy.sh
```

Set the current script directory
```shell
$ SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
```

Set the Docker private-registry URL. Ex: `private-registry.trainingv6.cognitiveu.insights.ai`
```shell
$ DOCKER_REGISTRY=private-registry.<YOUR_DOMAIN>.insights.ai
```

Set the Docker image tag
```shell
$ IMAGE_TAG=test1
```

Log in to Docker
```shell
$ DEBUG=* cortex docker login
```

Build and push Docker image to private-registry
```shell
$ docker build -t ${DOCKER_REGISTRY}/word-count-daemon:${IMAGE_TAG} -f "${SCRIPT_DIR}"/skills/word-count-daemon/Dockerfile "${SCRIPT_DIR}"/skills/word-count-daemon
$ docker push ${DOCKER_REGISTRY}/word-count-daemon:${IMAGE_TAG}
```

Deploy daemon action `word-count-daemon`
```shell
$ cortex actions deploy --actionName 'word-count-daemon' --actionType 'daemon' --port 5000 --docker ${DOCKER_REGISTRY}/word-count-daemon:${IMAGE_TAG}
```

Save Skill `word-count-daemon`
```shell
$ cortex skills save "${SCRIPT_DIR}"/skills/word-count-daemon/skill.yaml
```

Save Agent `word-count-agent`
```shell
$ cortex agents save "${SCRIPT_DIR}"/agent.yaml
```


### 3. Invoking the Agent via CLI
After deploying the Agent and Skills you can invoke the Agent using the Cortex CLI.
```shell
$ cortex agents invoke word-count-agent input --params '{"payload":{"text":"This is a test"}}'
```

You'll get a response with an `activationId` like below:
```json
{
  "success": true,
  "activationId": "8bd0746a-2ab3-46f5-b058-ef604695a681"
}
```

Use the Cortex CLI to query the Activation to view the status, metadata, run time, and response of the Agent run.
```shell
$ cortex agents get-activation 8bd0746a-2ab3-46f5-b058-ef604695a681
```
The activation response will include the response from the `word-count-daemon`.
```json
{
  "success": true,
  "requestId": "8bd0746a-2ab3-46f5-b058-ef604695a681",
  "agentName": "word-count-agent",
  "serviceName": "input",
  "sessionId": "8bd0746a-2ab3-46f5-b058-ef604695a681",
  "projectId": "cognitive-u",
  "username": "jlara@example.com",
  "payload": {
    "text": "This is a test"
  },
  "start": 1631855778557,
  "status": "COMPLETE",
  "end": 1631855778592,
  "response": {
    "message": "Hello received: 'This is a test', word count: 4"
  }
}
```

Get a verbose activation response
```shell
$ cortex agents get-activation 8bd0746a-2ab3-46f5-b058-ef604695a681 --verbose
```
```json
{
  "success": true,
  "requestId": "8bd0746a-2ab3-46f5-b058-ef604695a681",
  "agentName": "word-count-agent",
  "serviceName": "input",
  "channelId": "80a1fc2b-2077-49fe-8fee-5bb5e2cb53f3",
  "sessionId": "8bd0746a-2ab3-46f5-b058-ef604695a681",
  "projectId": "cognitive-u",
  "token": "xxxx",
  "username": "jlara@example.com",
  "payload": {
    "text": "This is a test"
  },
  "start": 1631855778557,
  "status": "COMPLETE",
  "end": 1631855778592,
  "response": {
    "message": "Hello received: 'This is a test', word count: 4"
  },
  "transits": [
    {
      "_id": "614424a2c2029f1008dcf72a",
      "from": "deef58fc-b698-4166-a0a9-41bc6d29537b",
      "to": "a469818f-f7d0-41bf-8577-bde05136d6b8",
      "start": "2021-09-17T05:16:18.587Z",
      "status": "COMPLETE",
      "name": "output:output:output",
      "end": "2021-09-17T05:16:18.589Z"
    },
    {
      "_id": "614424a2c2029f27c6dcf72b",
      "from": "80a1fc2b-2077-49fe-8fee-5bb5e2cb53f3",
      "to": "deef58fc-b698-4166-a0a9-41bc6d29537b",
      "start": "2021-09-17T05:16:18.563Z",
      "status": "COMPLETE",
      "name": "skill:word-count-daemon:word-count-daemon",
      "end": "2021-09-17T05:16:18.586Z"
    }
  ]
}
```


### 4. Invoking the Skill
After deploying Skill you can invoke the Skill using the Cortex CLI.
```shell
$ cortex skills invoke word-count-daemon input --params '{"payload":{"text":"This is a test"}}'
```

You'll get a response with an `activationId` like below:
```json
{
  "success": true,
  "activationId": "a150571e-a484-4815-a3a5-6e60d788c0b4"
}
```

Use the Cortex's CLI to query the Activation to view the status, metadata, run time, and response of the Skill run.
```shell
$ cortex agents get-activation a150571e-a484-4815-a3a5-6e60d788c0b4
```
The activation response will include the response from the `word-count-daemon`.
```json
{
  "success": true,
  "requestId": "a150571e-a484-4815-a3a5-6e60d788c0b4",
  "skillName": "word-count-daemon",
  "inputName": "input",
  "projectId": "cognitive-u",
  "username": "jlara@example.com",
  "payload": {
    "text": "This is a test"
  },
  "sessionId": "a150571e-a484-4815-a3a5-6e60d788c0b4",
  "start": 1631853675410,
  "status": "COMPLETE",
  "end": 1631853675516,
  "response": {
    "message": "Hello received: 'This is a test', word count: 4"
  }
}
```

Get a verbose activation response.
```shell
$ cortex agents get-activation a150571e-a484-4815-a3a5-6e60d788c0b4 --verbose
```
```json
{
  "success": true,
  "requestId": "a150571e-a484-4815-a3a5-6e60d788c0b4",
  "skillName": "word-count-daemon",
  "channelId": "output",
  "inputName": "input",
  "projectId": "cognitive-u",
  "token": "eyJraWQiOiJfM1g1aWpvcGdTSm0tSmVmdWJQenh5RS1XWGw3UzJqSVZDLXRNWnNiRG9BIiwiYWxnIjoiRWREU0EifQ.eyJiZWFyZXIiOiJ1c2VyIiwiaWF0IjoxNjMxODUxOTIyLCJleHAiOjE2MzE5MzgzMjIsInJvbGVzIjpbImNvcnRleC1hZG1pbnMiXSwic3ViIjoiamxhcmFAZXhhbXBsZS5jb20iLCJhdWQiOiJjb3J0ZXgiLCJpc3MiOiJjb2duaXRpdmVzY2FsZS5jb20ifQ.yxhJzq0obZ-tMsiDQMPJQLpknFMmas_N3xqtkk1dzjKl4XXYMHsdv8PDbRgePNlaN12dVW8yZ4vxBdgp2dzcAA",
  "username": "jlara@example.com",
  "payload": {
    "text": "This is a test"
  },
  "sessionId": "a150571e-a484-4815-a3a5-6e60d788c0b4",
  "start": 1631853675410,
  "status": "COMPLETE",
  "end": 1631853675516,
  "response": {
    "message": "Hello received: 'This is a test', word count: 4"
  },
  "transits": [
    {
      "_id": "61441c6bc2029fd620dcf724",
      "from": "input",
      "to": "output",
      "start": "2021-09-17T04:41:15.429Z",
      "status": "COMPLETE",
      "name": "skill:word-count-daemon",
      "end": "2021-09-17T04:41:15.516Z"
    }
  ]
}
```

### 5. Invoking Agent via Fabric Console
1. Log into the Console.
2. On the left menu, click Agents.
3. From the list of Agents, click on "word-count-agent".
4. In the Agent Composer's Debug Pane (bottom), click the play button.
5. In the Run Test modal, select the `input` option.
6. Fill in the `text` property in the Message Body Text Area.
7. Click Run and watch it GO!
8. Watch for a green result in the Replay's Trace Output.
9. Click on Debug and scroll to the bottom for the result.

The `requestId` can be used like an `activationId` in the command `cortex agents get-activation` for example:

```shell
$ cortex agents get-activation <requestId>
```


## Testing the code locally
To avoid using up your private registry space, it is good practice to test your code before pushing.

Create python virtual env
```shell
$ python -m venv testvenv
$ source testvenv/bin/activate
$ pip install -r requirements.txt
```

Running the daemon.
```shell
$ uvicorn main:app --port 5000

INFO:     Started server process [57435]
INFO:     Waiting for application startup.
INFO:     Application startup complete.
INFO:     Uvicorn running on http://127.0.0.1:5000 (Press CTRL+C to quit)
```

Testing daemon text echo endpoint
```shell
$ curl -X 'POST' \
  'http://localhost:5000/invoke' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{"payload": "Hello! This is my test"}'
````

```json
{
  "message": "Hello received: 'This is a test', word count: 4"
}
```

You can also test your endpoints via fastapi docs. Visit `http://localhost:5000/docs` using your browser, click on the "Try it out" button, enter the required fields, and click "Execute"
