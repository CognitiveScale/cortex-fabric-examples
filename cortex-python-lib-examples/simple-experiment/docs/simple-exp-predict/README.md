# simple-exp-predict Daemon Skill

A system generated Skill based on the custom daemon Skill template. This daemon Skill echos the `message` provided in the payload.


## Files Generated
- `docs/` - The directory that houses the Skills' READMEs
    - `simple-exp-predict/`: The directory that houses the simple-exp-predict Skill's README
        - `README.md`: Provides the objectives, requirements, and instructions for generating and deploying the Skill.
- `skills/`: The directory that houses the Skills
    - `simple-exp-predict/`: The directory that houses the simple-exp-predict Skill's assets
        - `actions/`: The directory that houses the simple-exp-predict Skill's Actions
            - `simple-exp-predict/`: The contents of the simple-exp-predict action
                - `Dockerfile`: Builds the Docker image the action
                - `main.py`: Code for Cortex daemon
                - `requirements.txt`: Dependencies and libraries
        - `invoke/`: Contains the payloads, organized by Skill input name, used to invoke the simple-exp-predict Skill
            - `request/`: Contains payload files used to invoke the Skill
                - `message.json`: JSON payload used to invoke the Skill
            - `skill.yaml`: simple-exp-predict Skill definition and Action mapping


## Generate the Skill.

You've already done this via:
- [VS Code Extension](https://cognitivescale.github.io/cortex-code/)
- [Skill Builder in the Fabric Console](https://cognitivescale.github.io/cortex-fabric/docs/build-skills/skill-builder-ui)

Please use the above links for more information on how to continue building, pushing, deploying, developing, and invoking your Skill.


## Test the code locally

To avoid filling your private registry, testing your code prior to deployment is recommended.

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

Test daemon endpoint.
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

You can also test your endpoints via fastapi docs. Visit `http://localhost:5000/docs` using your browser, click on the "Try it out" button, enter the required fields, and click "Execute"


#### Documentation
- [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/development/define-skills)
- [Skill Elements](https://cognitivescale.github.io/cortex-fabric/docs/build-skills/define-skills#skill-elements)
- 
