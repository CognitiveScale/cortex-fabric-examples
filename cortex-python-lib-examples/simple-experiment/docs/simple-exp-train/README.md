# simple-exp-train Job Skill

Cortex Skill that runs a background job.


## Files Generated
- `docs/` - The directory that houses the Skills' READMEs
    - `simple-exp-train/`: The directory that houses the simple-exp-train Skill's README
        - `README.md`: Provides the objectives, requirements, and instructions for generating and deploying the Skill.
- `skills/`: The directory that houses the Skills
    - `simple-exp-train/`: The directory that houses the simple-exp-train Skill's assets
        - `actions/`: The directory that houses the simple-exp-train Skill's Actions
            - `simple-exp-train/`: The contents of the simple-exp-train action
                - `Dockerfile`: Builds the Docker image the action
                - `main.py`: Code for Cortex job
                - `requirements.txt`: Dependencies and libraries
        - `invoke/`: Contains the payloads, organized by Skill input name, used to invoke the simple-exp-train Skill
            - `request/`: Contains payload files used to invoke the Skill
                - `message.json`: JSON payload used to invoke the Skill
            - `skill.yaml`: simple-exp-train Skill definition and Action mapping


## Generate the Skill.

You've already done this via:
- [VS Code Extension](https://cognitivescale.github.io/cortex-code/)
- [Skill Builder in the Fabric Console](https://cognitivescale.github.io/cortex-fabric/docs/build-skills/skill-builder-ui)

Please use the above links for more information on how to continue building, pushing, deploying, developing, and invoking your Skill.


#### Test the code locally
To avoid using up your private registry space, it is good practice testing your code before pushing.

Create Python virtual env.
```shell
python -m venv testvenv
source testvenv/bin/activate
pip install -r requirements.txt
```

Testing the job.
```shell
python ./main.py '{"payload":{"message":  "This is a test payload message"}}'
````
Response:
```text
{"message":  "This is a test payload message"}
```
