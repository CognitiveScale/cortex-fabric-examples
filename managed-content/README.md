# Managed Content Agent Example (m-content-agent)

`m-content-job` uploads and downloads from managed content. The `m-content-agent` Agent uses the `m-content-job` Skill.

## Purpose
In the Example an Action with the logic that is executed is created. It is encapsulated in a Skill that provides the internal routing. Finally, that Skill is added to an Agent with input and output services that direct the data flow through the Skill.


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
- `managed_content_notebook.ipynb` - jupyter notebook example for managed content


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
$ docker build -t ${DOCKER_REGISTRY}/m-content-job:${IMAGE_TAG} -f "${SCRIPT_DIR}"/skills/m-content-job/Dockerfile "${SCRIPT_DIR}"/skills/m-content-job
$ docker push ${DOCKER_REGISTRY}/m-content-job:${IMAGE_TAG}
```

Deploy job action `m-content-job`
```shell
$ cortex actions deploy --actionName 'm-content-job' --actionType 'job' --docker ${DOCKER_REGISTRY}/m-content-job:${IMAGE_TAG}
```

Save Skill `m-content-job`
```shell
$ cortex skills save "${SCRIPT_DIR}"/skills/m-content-job/skill.yaml
```

Save Agent `m-content-agent`
```shell
$ cortex agents save "${SCRIPT_DIR}"/agent.yaml
```


### 3. Invoking the Skill
After deploying Skill you can invoke the Skill using the Cortex CLI.
```shell
cortex skills invoke m-content-job params --params '{"payload":{"command":"upload", "key":"m-content-job-content", "content":"I am here"}}'
```

You'll get a response with an `activationId` like below:
```json
{
  "success": true,
  "activationId": "9c956d88-d0af-4803-8e12-dac56666806f"
}
```

Use the Cortex's CLI to query the Activation to view the status, metadata, run time, and response of the Skill run.
```shell
cortex agents get-activation 9c956d88-d0af-4803-8e12-dac56666806f
```
The activation response will include the response from the `m-content-job`.
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

Get a verbose activation response.
```shell
cortex agents get-activation 9c956d88-d0af-4803-8e12-dac56666806f --verbose
```
```json
{
  "success": true,
  "requestId": "9c956d88-d0af-4803-8e12-dac56666806f",
  "skillName": "m-content-job",
  "channelId": "output",
  "inputName": "params",
  "projectId": "cogu",
  "token": "eyJraWQiOiJfM1g1aWpvcGdTSm0tSmVmdWJQenh5RS1XWGw3UzJqSVZDLXRNWnNiRG9BIiwiYWxnIjoiRWREU0EifQ.eyJiZWFyZXIiOiJ1c2VyIiwiaWF0IjoxNjM1NDAwMTkzLCJleHAiOjE2MzU0ODY1OTMsInJvbGVzIjpbImNvcnRleC1hZG1pbnMiXSwic3ViIjoiamxhcmFAZXhhbXBsZS5jb20iLCJhdWQiOiJjb3J0ZXgiLCJpc3MiOiJjb2duaXRpdmVzY2FsZS5jb20ifQ.7gyXVoSWcesMP17n-DkTeq80ZEnAUXoZnh08TGLV5JNEkyChQ-4If5BPN8C4ZAACA0m03ZBngqrPd-r3YImnBQ",
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
  "response": "Managed content (m-content-job-content) uploaded: I am here\n",
  "transits": [
    {
      "from": "params",
      "to": "output",
      "start": "2021-10-28T08:46:01.573Z",
      "end": "2021-10-28T08:46:13.003Z",
      "name": "skill:m-content-job",
      "status": "COMPLETE",
      "_id": "617a63553e239a9cbbb37d1e"
    }
  ]
}
```

### 4. Invoking Agent via Fabric Console
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


## Testing the code locally
To avoid using up your private registry space, it is good practice to test your code before pushing.

Create python virtual env
```shell
python -m venv testvenv
source testvenv/bin/activate
pip install -r ./skills/m-content-job/requirements.txt
```

Testing the uplaod managed content code path of the job
```shell
python ./skills/m-content-job/main.py '{"apiEndpoint":"https://api.dci-dev.dev-eks.insights.ai", "projectId":"cogu", "token":"xxxx", "payload":{"command":"upload", "key":"m-content-job-content", "content":"I am here"}}'
````
Response:
```text
Managed content (m-content-job-content) uploaded: I am here
```

Testing the download managed content code path of the job
```shell
python ./skills/m-content-job/main.py '{"apiEndpoint":"https://api.dci-dev.dev-eks.insights.ai", "projectId":"cogu", "token":"xxxx", "payload":{"command":"download", "key":"m-content-job-content"}}'
````
Response:
```text
b'I am here'
```
