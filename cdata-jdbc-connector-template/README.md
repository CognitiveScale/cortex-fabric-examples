# jdbc-connector-skill-template

Cortex Skill Template for Generic and CDATA JDBC connections

### Requirements:
- cortex-cli 2.0.x
- Docker

### Developer Notes (Local environment setup for updating example):
- Java 1.8 (confirm with checking `javac -version`, as we need JDK for development)
- Gradle will be downloaded in not installed

Uses hikari but doesn't parse the extra params CDATA has../CDATA specific plugin, require extra parsing for CData params.

### Contents:
* jdbc-plugin-cdata - generic/cdata JDBC plugin
* jdbc-service-harness - daemon code, load plugins, ...
* jdbc-service-lib - base library for creating plug-ins

#### Steps to build and deploy

Set environment variables `DOCKER_PREGISTRY_URL` (like <docker-registry-url>/<namespace-org>) and `PROJECT_NAME` (Cortex Project Name), and use build scripts to build and deploy.
Configure Docker auth to the private registry:
  1. For Cortex DCI with Docker registry installed use `cortex docker login`
  2. For external Docker registries like Google Cloud's GCR etc use their respective CLI for Docker login

##### On *nix systems
A Makefile is provided to do these steps.
* `export DOCKER_PREGISTRY_URL=<docker-registry-url>/<namespace-org>`
* `export PROJECT_NAME=<cortex-project>`
* `make all` will build and push Docker image, deploy Cortex Action and Skill, and then invoke Skill to test.

##### On Windows systems
A `make.bat` batch file is provided to do these steps.
* `set DOCKER_PREGISTRY_URL=<docker-registry-url>/<namespace-org>`
* `set PROJECT_NAME=<cortex-project>`
  > Below commands will build and push Docker image, deploy Cortex Action and Skill, and then invoke Skill to test.
* `make build`
* `make push`
* `make deploy`
* `make tests`

### Building Step by Step:

* Build Docker Image:
```build docker
make build
```
* Push Docker Image:
```push docker
make push
```
* Deploy Cortex Action: Deploys Actions & Save Skill using skill.yaml
```build docker
make deploy
```

### Adding Skill to Agent.
Once the Skill is deployed, you can add it to any Agent in the current project using `Add Skill`.

### Skill Configuration:
Properties:
* `connectionName`: Cortex Connection Name
* `connectionType`: Jdbc daemon to use (ENUM with values jdbc_generic/jdbc_cdata)

### Invocation Skill:
* Prerequisites:
    * Create a Connection using `Cortex Console` with ConnectionType as `jdbc_generic` or `jdbc_cdata`. 
      
* The Connection Metadata & Params for a cdata should be as below:
      
  *  `managed_content_key`: Managed Content Key where the driver jar is uploaded. Project to Upload should be `shared` for driver re-usability.
       
  * `plugin_properties`: JSON String with Cdata Connection Params for Salesforce:
          
          "{\"User\":\"username@example.com\",\"Password\":\"password\",\"Security Token\":\"<TOKEN>\"}"    
  * `run_time_key`: CDATA JAR RTK String
          
* The Connection Metadata & Params for a jdbc should be as below:

  * `managed_content_key`: Managed Content Key where the driver jar is uploaded. Project to Upload should be Shared for driver reusability.
    
  * `classname`: Driver Class Name
  
  * `uri`: JDBC URI Ex: jdbc:mysql://sql6.freesqldatabase.com:3306/sql6434964 
    
  * `username`: Database User
    
  * `password`: Database Password
  
* Select the Skill Properties `ConnectionType` Dropdown to respective Connection Type to use based on connection created.
* Update connectionName in Skill Properties
* Invoke the Skill with below sample Payload:
```skill payload
select * from Account limit 2;
```

* References:
The Skill is added to the Cortex Fabric catalog and is available for selection when building interventions or Agents.

Skills that are deployed may be invoked (run) either independently or within an agent.

For more details about how to build skills go to Cortex Fabric Documentation - Development - Develop Skills

Salesforce Standard Objects:
https://developer.salesforce.com/docs/atlas.en-us.api.meta/api/sforce_api_objects_list.htm
