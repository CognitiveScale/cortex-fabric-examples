# Skill
 
A Skill is composed of two core elements:

Metadata that describes the Skill. Skill metadata is defined with CAMEL. At a minimum, it must include the Skill's name, title, CAMEL version, and at least one input that defines how to route input messages that the Skill receives when it is invoked. It also supports other optional fields, like description, outputs, and properties.

The runtime to execute when a Skill is invoked. Input messages sent to a Skill are processed based on the runtime type: cortex/jobs, cortex/daemons, and cortex/external-api. The runtime type determines what should happen when the Skill is invoked. Jobs and daemons are routed to an action, while an External API runtime takes the input and routes it to an API endpoint.

# Action

At the core of every Skill is the action it takes to process data when it is invoked. Cortex supports two types of actions:

### Jobs: 
Used to process high volumes of data that would normally consume long-term memory if run in the foreground, as well as for running programs that require little to no user interaction and are required to run on a regular basis.

### Daemons: 
Web servers typically used for ML predictions and serving inquiries. Once started, a daemon runs indefinitely.

# Intervetion skills generic functionality

These skills will send flu-shot reminder notifications along with provider info to members in preferred communication channel(eg.. Sydneycare App, Message, Email, Call.. )

## Skills List

1. Send Notification Via Sydneycare 
2. Engage Members to Sydneycare Chatbot
3. Send Schedule Appointment Notification
4. Send Nurseline Notification
5. Send Virtual Care Notification
6. Update Member Phone Number

#### Steps to build and deploy

Set environment variables `DOCKER_PREGISTRY_URL` (like <docker-registry-url>/<namespace-org>) and `PROJECT_NAME` (Cortex Project Name), and use build scripts to build and deploy.

Configure Docker auth to the private registry:
1. For Cortex DCI with Docker registry installed use `cortex docker login`
2. For external Docker registries like Google Cloud's GCR etc use their respective CLI for Docker login

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
