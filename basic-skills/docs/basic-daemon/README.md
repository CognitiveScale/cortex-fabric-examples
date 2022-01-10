# basic-daemon Daemon Skill (UPDATE THIS FOR YOUR SKILL)

A system generated Skill based on the custom daemon Skill template. 

> Modify these sections for your specific use case.

## Files Generated
- `docs/` - The directory that houses the Skills' READMEs
  - `basic-daemon/`: The directory that houses the basic-daemon Skill's README
    - `README.md`: Provide the objectives, requirements, and instructions for generating and deploying the Skill here.
- `skills/`: The directory that houses the Skills
  - `basic-daemon/`: The directory that houses the basic-daemon Skill's assets
    - `actions/`: The directory that houses the basic-daemon Skill's Actions
      - `basic-daemon/`: The contents of the basic-daemon action
        - `Dockerfile`: Modify this to build the Docker image the action
        - `main.py`: Modify the code in this file for your Cortex daemon
        - `requirements.txt`: List all the dependencies and libraries needed for this Skill
    - `invoke/`: Contains the payloads, organized by Skill input name, used to invoke the basic-daemon Skill
      - `request/`: Contains payload files used to invoke the Skill
        - `message.json`: Write a test JSON payload to invoke the Skill
      - `skill.yaml`: Define basic-daemon Skill definition and map Actions here


## Generate the Skill

The Skill should have been generated previously. If not, please use the following links for more information on how to continue building, pushing, deploying, developing, and invoking your Skill.
- [VS Code Extension](https://cognitivescale.github.io/cortex-code/)
- [Skill Builder in the Fabric Console](https://cognitivescale.github.io/cortex-fabric/docs/build-skills/skill-builder-ui)


> NOTE: Modify the following files for your specific use case:
> - `docs/basic-daemon/README.md`
> - `skills/basic-daemon/actions/basic-daemon/Dockerfile`
> - `skills/basic-daemon/actions/basic-daemon/main.py`
> - `skills/basic-daemon/actions/basic-daemon/requirements.txt`
> - `skills/basic-daemon/actions/skill.yaml`
> - `invoke/request/message.json`


## Test the code locally

To avoid filling your private registry, testing your code prior to deployment is recommended. Here's a way to do that.

### Create Python virtual env
```shell
python -m venv testvenv
source testvenv/bin/activate
pip install -r requirements.txt
```

### Run the daemon
```shell
uvicorn main:app --port 5000

INFO:     Started server process [57435]
INFO:     Waiting for application startup.
INFO:     Application startup complete.
INFO:     Uvicorn running on http://127.0.0.1:5000 (Press CTRL+C to quit)
```

### Test daemon endpoint

This can be done several ways. Use your preferred method to test the endpoint locally.

#### a. Using curl
```shell
curl -X 'POST' \
  'http://localhost:5000/invoke' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{"payload": {"message":  "This is a test payload message"}}'
````

Response:
```json
{
  "message":  "This is a test payload message"
}
```
#### b. Using fastdocs

Visit `http://localhost:5000/docs` using your browser, click on the "Try it out" button, enter the required fields, and click "Execute".


#### c. Using a web service testing application 

Examples: Postman, SoapUI, etc


## Documentation
- [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/development/define-skills)
- [Skill Elements](https://cognitivescale.github.io/cortex-fabric/docs/build-skills/define-skills#skill-elements)
- 
