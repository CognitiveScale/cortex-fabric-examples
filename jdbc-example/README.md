### Cortex JDBC and CDATA type Connection example Cortex Skill

This example Skill is a template for JDBC and CDATA connection implemented as Cortex Daemon type Skill. 

#### Prerequisites:
* Docker daemon running  
* Cortex CLI configured to target Cortex DCI  
* Jar build of https://github.com/CognitiveScale/cortex-cdata-plugin in lib directory (Already included in this example `lib` directory)
* Cortex 6.3 onwards, CDATA driver jars are already preloaded in DCI and available in `shared` project's Cortex Managed Content under reserved key prefix `.cortex/cdata/`
* Prior to Cortex 6.3, user need to add CData driver jars, same as other JDBC driver jars
  > Add to build.gradle
  
  > Copy to project's lib directory
  
  > Upload to Cortex managed content in `shared` project
  > (discouraged as this required instrumentation to add jar to classpath at runtime)
  > To use this run application with javaagent

* **For CDATA Connections:**
    * CDATA license keys are already installed in Cortex DCI. Check OEM Key and product checksum configured in `shared` project Secret (`cdata-oem-key` and `cdata-prdct-checksum`Secrets)
    * Get CDATA driver class name:
      * Get it from CDATA documentation. Example for BigQuery https://cdn.cdata.com/help/DBG/jdbc/pg_connectionj.htm. Or,
      * For Cortex 6.3 onwards, driver name can be listed through Cortex CLI. Example for BigQuery `cortex content list --project shared --profile cvsgke --query "[?contains(Key,'**bigquery**')].Key"` and copy value excluding key prefix `.cortex/cdata/`, like `cdata.jdbc.googlebigquery.GoogleBigQueryDriver`
    * Plugin properties JSON prepared as per CDATA documentation and saved as Secret in the project. This will be used while creating connection later.
      > BigQuery plugin properties JSON example (on Workload Identity enabled k8s cluster with permissions to execute BigQuery queries): `{"AuthScheme": "GCPInstanceAccount", "ProjectId": "fabric-qa", "DatasetId": "covid19_weathersource_co"}` Update `ProjectId` and `DatasetId`
      > Build connection properties from CDATA docs for respective database https://cdn.cdata.com/help/DBG/jdbc/pg_connectionj.htm
      > List of supported databases https://cdn.cdata.com/help/

* Create Cortex Connection of type `JDBC CDATA` or `JDBC Generic` in the Cortex DCI (in the project where we will deploy the Skill)

> This Cortex Skill is based on https://sparkjava.com to serve HTTP server. `Main.Java` sets up the route and runs the server.

> `Example` is the interface to plug different JDBC based frameworks. For example Hikari connection pool (HikariExample) and Hibernate (HibernateExample)
> Default is HikariExample, edit Main.java to use HibernateExample. HibernateExample comes with example entity class `Employee` mapped, update this class for desired entities and Hibernate configuration. 
 ```
 Main class 
 * runs web server
 * parse Cortex request
 * uses `cortex-cdata-plugin` to parse Cortex Connection
 * `Example#setup` initializes the connection 
 * `Example#execute` executes SQL query and parse result to Cortex Skill response
```
#### Steps to build and deploy

Set environment variables `DOCKER_PREGISTRY_URL` (like `<docker-registry-url>/<namespace-org>`) and `PROJECT_NAME` (Cortex Project Name), and use build scripts to build and deploy.

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
   Skills that are deployed may be invoked (run) either independently or within an agent. Update `test/payload.json` with `query` as per database and `connection_name`, and run `make tests` to invoke the deployed Skill.
    
    Example payload for BigQuery connection in Skill composed in an Agent 
    ```
  {
      "query": "SELECT * FROM `bigquery-public-data.covid19_weathersource_com.postal_code_day_forecast` WHERE forecast_date = '2022-06-21' LIMIT 10"
  }
  ```

For more details about how to build skills go to [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/development/define-skills)
