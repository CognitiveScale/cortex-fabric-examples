# jdbc-connector-skill-template

Cortex Skill Template for Generic and CDATA JDBC connections

### Requirements:
- Java >= 8
- Gradle 3.4.x+

Uses hikari but doesn't parse the extra params CDATA has../CDATA specific plugin, require extra parsing for CData params.

### Contents:
* jdbc-plugin-cdata - generic/cdata JDBC plugin
* jdbc-service-harness - daemon code, load plugins, ...
* jdbc-service-lib - base library for creating plug-ins

### Installation Steps:

* First setup environment variables for docker registry and project like below:
```
export PROJECT_NAME=shared
export DOCKER_PREGISTRY_URL=private-registry.dci-dev.dev-eks.insights.ai
```

* Run make all command below to run all the installation steps after doing docker login.

```
cortex docker login
make all
```

### Building Step by Step:

* Gradle Build with tests
```
make gradle.build
```
* Gradle Build without tests
```
make gradle.build.skip-tests
```
* Build Docker Image:
```build docker
make push
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
      
  *  `managed_content_key`: Managed Content Key where the driver jar is uploaded. Project to Upload should be Shared for driver reusability.
       
  * `plugin_properties`: JSON String with Cdata Connection Params for Salesforce:
          
          "{\"User\":\"username@example.com\",\"Password\":\"password\",\"Security Token\":\"<TOKEN>\"}"    
  * `run_time_key`: CDATA JAR RTK String
          
* The Connection Metadata & Params for a cdata should be as below:

  * `managed_content_key`: Managed Content Key where the driver jar is uploaded. Project to Upload should be Shared for driver reusability.
    
  * `class_name`: Driver Class Name
  
  * `uri`: JDBC URI Ex: jdbc:mysql://sql6.freesqldatabase.com:3306/sql6434964 
    
  * `user`: Database User
    
  * `pass`: Database Password
  
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