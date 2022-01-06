### Send Virtualcare Notification


#### Description:
For the Profile IDs derived from the configured Cohort, member contact information is shared with virtual care nurses, who call members to provide information and schedule flu-shots.

#### Invoke Skill From Cli

```
cortex skills invoke hw/virtual-care  request  --params '{"payload": {"profiles": ["41993296", "46041761"]}}' --project ${PROJECT_NAME}
```

#### Action API: 
<Action API Placeholder>

#### Input:
List of Profile ids

```
{"profiles": ["41993296", "46041761"]}
```


#### Ouput:
Acknowledgement of skill status
```
{
  "success": true,
  "payload": {
    "message": "Successfully sent given profile details to virtual care nurse"
  },
  "activationId": "4f73df5a-27e7-4b57-aff1-4a5ec14228a1",
  "elapsedTime": 6
}
```

#### Steps to build and deploy

Set environment variables `DOCKER_PREGISTRY_URL` (like <docker-registry-url>/<namespace-org>) and `PROJECT_NAME` (Cortex Project Name), and use build scripts to build and deploy.

Configure Docker auth to the private registry:
1. For Cortex DCI with Docker registry installed use `cortex docker login`
2. For external Docker registries like Google Cloud's GCR, ECR, ACR etc use their respective CLI for Docker login

##### On *nix systems
A Makefile is provided to do these steps.
* `export DOCKER_PREGISTRY_URL=<docker-registry-url>/<namespace-org>`
* `export PROJECT_NAME=<cortex-project>`
* `make all` will build and push Docker image, deploy Cortex Action and Skill, and then invoke Skill to test.##### On *nix systems
A Makefile is provided to do these steps.
* `export DOCKER_PREGISTRY_URL=<docker-registry-url>/<namespace-org>`
* `export PROJECT_NAME=<cortex-project>`
* `make all` will build and push Docker image, deploy Cortex Action and Skill, and then invoke Skill to test.

 > To individually run through the steps we can:
1. `make build`
2. `make push`
33. `make deploy`

 > To check the status
* `make get`
 
 > To run tests
* `make tests`

##### On Windows systems
A `make.bat` batch file is provided to do these steps.
* `set DOCKER_PREGISTRY_URL=<docker-registry-url>/<namespace-org>`
* `set PROJECT_NAME=<cortex-project>`

  > Below commands will build and push Docker image, deploy Cortex Action and Skill, and then invoke Skill to test.
1. `make build`
2. `make push`
3. `make deploy`

 > To check the status
* `make get`

 > To run tests
* `make tests`