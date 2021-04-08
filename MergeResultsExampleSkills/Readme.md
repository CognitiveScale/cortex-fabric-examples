# Merge Results Skills Example
This example demonstrates how to use `Merge Results` accelerator skill to merge multiple skills results using `Merge Results` accelerator.

`Merge Results` is not part of this repo, find in `cortex-accelerator` repo and Deploy from there
https://github.com/CognitiveScale/cortex-accelerators

### Skills Involved
 - Hello Daemon
 - Session Skill A
 - Session Skill B
 - Writer Skill

### Cortex Components Involved
 - Skills
 - Sessions
 - Output Names 

### Deployment
Following are the deployment commands using Makefile

 - To deploy all run `make all ` <br>
 - To build `make build` <br>
 - To push docker images - `make push` <br>
 - To deploy actions & agents - `make deploy` <br>
    
    Note: Make sure to change docker registry details in `Makefile`
    
### Agent Depoyment
 - `make agents.save`