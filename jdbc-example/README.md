### Cortex JDBC and CDATA type Connection example Cortex Skill

This example Skill is a template for JDBC and CDATA connection implemented as Cortex Daemon type Skill. 

#### Prerequisites:
* Java 8
* Make
* Docker daemon running  
* Cortex CLI configured to target Cortex DCI  
* Jar build of https://github.com/CognitiveScale/cortex-cdata-plugin in lib directory
* CDATA/JDBC driver jar uploaded in `shared` project of the DCI
* Cortex Connection created in the Cortex DCI (any project where we will deploy the Skill)
* **For CDATA Connections:**
    * OEM Key configured in `shared` project Secret (Secret name must be `cdata_oem_key`)
    * Plugin properties JSON prepared as per CDATA documentation and saved as Secret in the project

> This is based on https://sparkjava.com to serve HTTP server

> `ConnectionManager` is a utility class to manage Hikari connection pool and parse JDBC ResultSet

 ```
 Main class 
 * runs web server
 * parse Cortex request
 * uses `cortex-cdata-plugin` to initialize Cortex Connection
 * uses `ConnectionManager` to create connection
 * executes SQL query and parse ResultSet to Cortex Skill response
```
#### Steps to deploy this Skill

A Makefile is provided to do these steps. Set environment variables `DOCKER_PREGISTRY_URL` (like <docker-registry-url>/<namespace-org>) and `PROJECT_NAME` (Cortex Project Name) and use Makefile to deploy Skill.
`make all` will build and push Docker image, deploy Cortex Action and Skill, and then invoke Skill to test.

   The Skill is added to the Cortex Fabric catalog and is available for selection when building interventions or Agents

   Skills that are deployed may be invoked (run) either independently or within an agent.

For more details about how to build skills go to [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/development/define-skills)
