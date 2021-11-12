# Managed Content Agent Example (m-content-agent)

`m-content-job` uploads and downloads from Managed Content. The `m-content-agent` Agent uses the `m-content-job` Skill.


## Included Files to Review
- `skills/` - The directory that houses the Skills
    - `m-content-job/` - The contents of the m-content-job action
        - `Dockerfile` - Builds the Docker image the action
        - `main.py` - Code for Cortex job
        - `requirements.txt` - Dependencies and libraries
        - `skill.yaml` - Skill definition and action mapping
- `agent.yaml` - Agent definition and Skill mapping
- `deploy.sh` - Builds and pushes images, deploys actions, saves Skills and saves Agents.
- `README.md` - Provides the objectives, requirements, and instructions for generating and deploying the Skill.
- `managed_content_notebook.ipynb` - jupyter notebook example for Managed Content


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

#### Use Skill to `upload` Managed Content 
The following is an example command used to upload Managed Content.
```shell
cortex skills invoke m-content-job params --params '{"payload":{"command":"upload", "key":"m-content-job-content", "content":"I am here"}}'
```

Response:
```json
{
  "success": true,
  "activationId": "9c956d88-d0af-4803-8e12-dac56666806f"
}
```

Query the activation to view the status, metadata, run time, and response of the Skill run.
```shell
cortex agents get-activation 9c956d88-d0af-4803-8e12-dac56666806f
```

Response:
```json
{
  "success": true,
  "requestId": "9c956d88-d0af-4803-8e12-dac56666806f",
  "skillName": "m-content-job",
  "inputName": "params",
  "projectId": "cogu",
  "username": "jlara@example.com",
  "payload": {
    "command": "upload",
    "key": "m-content-job-content",
    "content": "I am here"
  },
  "sessionId": "9c956d88-d0af-4803-8e12-dac56666806f",
  "start": 1635410761558,
  "status": "COMPLETE",
  "end": 1635410773001,
  "response": "Managed content (m-content-job-content) uploaded: I am here\n"
}
```

#### Use Skill to `download` Managed Content
The following is an example command used to `download` Managed Content.
```shell
cortex skills invoke m-content-job params --params '{"payload":{"command":"download", "key":"m-content-job-content"}}'
```

Response:
```json
{
  "success": true,
  "activationId": "90c79afb-4ed8-42de-8ffc-bd2ec5f71e2c"
}
```

Query the activation to view the status, metadata, run time, and response of the Skill run.
```shell
cortex agents get-activation 90c79afb-4ed8-42de-8ffc-bd2ec5f71e2c
```

Response:
```json
{
  "success": true,
  "requestId": "90c79afb-4ed8-42de-8ffc-bd2ec5f71e2c",
  "skillName": "m-content-job",
  "inputName": "params",
  "projectId": "cogu",
  "username": "jlara@example.com",
  "payload": {
    "command": "download",
    "key": "m-content-job-content"
  },
  "sessionId": "90c79afb-4ed8-42de-8ffc-bd2ec5f71e2c",
  "start": 1635410850487,
  "status": "COMPLETE",
  "end": 1635410859928,
  "response": "b'I am here'\n"
}
```


### 4. Invoke Agent via Fabric Console
1. Log into the Console.
2. On the left menu, click Agents.
3. From the list of Agents, click on "m-content-agent".
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



## Test the code locally
To avoid using up your private registry space, it is good practice testing your code before pushing.

Create Python virtual env.
```shell
python -m venv testvenv
source testvenv/bin/activate
pip install -r ./skills/m-content-job/requirements.txt
```

Testing the `upload` Managed Content code path of the job.
```shell
python ./skills/m-content-job/main.py '{"apiEndpoint":"https://api.dci-dev.dev-eks.insights.ai", 
"projectId":"cogu", "token":"xxxx", 
"payload":{"command":"upload", "key":"m-content-job-content", "content":"I am here"}}'
````
Response:
```text
Managed content (m-content-job-content) uploaded: I am here
```

Testing the `download` Managed Content code path of the job.
```shell
python ./skills/m-content-job/main.py '{"apiEndpoint":"https://api.dci-dev.dev-eks.insights.ai", 
"projectId":"cogu", "token":"xxxx", 
"payload":{"command":"download", "key":"m-content-job-content"}}'
````
Response:
```text
b'I am here'
```
