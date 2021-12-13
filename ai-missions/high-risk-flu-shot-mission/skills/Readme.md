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

## Deployment steps

Set the evironment variable `PROJECT_NAME` using `export PROJECT_NAME=<project_name>` and `DOCKER_PRIV_REGISTRY_URL` using `export DOCKER_PRIV_REGISTRY_URL=<project_name>`, make sure to have access to the private registry being used, for cortex users it would be as simple as `cortex docker login`.

`make all` builds and deploys all the skills along with saving the actions, types and skills.

To individually run through the steps we can:
1. `make save-type`
2. `make build`
3. `make deploy`
4. `make save-skill`





