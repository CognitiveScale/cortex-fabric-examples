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
See Fabric Documentation: [Configure CLI](https://cognitivescale.github.io/cortex-fabric/docs/getting-started/use-cli#configure-the-cli)


### 2. Deploy
Open the `deploy.sh` file and make sure that `DOCKER_REGISTRY` and `IMAGE_TAG` are set correctly.

Run the command to build the Docker containers and deploy the Action, Skill, and Agent:
```shell
./deploy.sh
```


### 3. Invoke the Skill
After deploying the Skill you can invoke the Skill using the Cortex CLI.

```shell
cortex skills invoke word-count-daemon input --params '{"payload":{"text":"This is a test"}}'
```

Response:
```json
{
  "success": true,
  "activationId": "a150571e-a484-4815-a3a5-6e60d788c0b4"
}
```

Query the activation to view the status, metadata, run time, and response of the Skill run.
```shell
cortex agents get-activation a150571e-a484-4815-a3a5-6e60d788c0b4
```

Response:
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


### 4. Invoke Agent via Fabric Console
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
cortex agents get-activation <requestId>
```


## Test the code locally
To avoid using up your private registry space, it is good practice testing your code before pushing.

Create Python virtual env.
```shell
python -m venv testvenv
source testvenv/bin/activate
pip install -r requirements.txt
```

Run the daemon.
```shell
uvicorn main:app --port 5000

INFO:     Started server process [57435]
INFO:     Waiting for application startup.
INFO:     Application startup complete.
INFO:     Uvicorn running on http://127.0.0.1:5000 (Press CTRL+C to quit)
```

Test daemon text echo endpoint.
```shell
curl -X 'POST' \
  'http://localhost:5000/invoke' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{"payload": "Hello! This is my test"}'
````

Response:
```json
{
  "message": "Hello received: 'This is a test', word count: 4"
}
```

You can also test your endpoints via fastapi docs. Visit `http://localhost:5000/docs` using your browser, click on the "Try it out" button, enter the required fields, and click "Execute"
