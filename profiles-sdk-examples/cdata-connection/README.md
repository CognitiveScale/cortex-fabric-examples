# Using a JDBC CData Connection

This example is a CLI application for reading data from a [JDBC CData Cortex Connection](https://cognitivescale.github.io/cortex-fabric/docs/reference-guides/connection-types#jdbc-cdata-connections)
and writing that data to a separate Connection. This builds off of the [Local Clients](../local-clients/README.md) example
for its initial setup but uses a separate set of connections defined in [cdata-connections.yml](../main-app/src/main/resources/spec/cdata-connections.yml).

Three example JDBC CData Connections are defined in the [Local Catalog](../local-clients/README.md#catalog):
- `cdata-csv` - Connection to a local `members_100_v14.csv` file that is used for the following examples.
- `cdata-s3` - CData Connection to Amazon S3. To use this example you will need update the `plugin_properties`
  and `query` parameters for your S3 data in  the [cdata-connections.yml](../main-app/src/main/resources/spec/cdata-connections.yml)
  file. (See [Notes on CData S3 Connection](#notes-on-jdbc-cdata-s3-connection))
- `cdata-bigquery` - CData Connection to Google BigQuery. To use this you must update the `plugin_properties`
  and `query` parameters to match your BigQuery data in the [cdata-connections.yml](../main-app/src/main/resources/spec/cdata-connections.yml)
  file. (See [Notes on CData BigQuery Connection](#notes-on-jdbc-cdata-bigquery-connection)).

(See [CData.java](./src/main/java/com/c12e/cortex/examples/cdata/CData.java) for the source code.)

## CData Connection Types
* The expected parameters in a CData Connection are `plugin_properties`, `classname`, and optionally `query`.
* The value of `plugin_properties` the field should be a reference to a Cortex Secret (JSON) with values specific to the
  CDATA Driver being used. Example properties could include `url`, `dbtable`, `query`, `username`, `password`, etc.
* The `classname` is the class path of to your JDBC Driver.
* The Profiles SDK expects the CDATA OEM Key and Product Checksum to be Secrets in the `"shared"` project, but Secrets and Project name are configurable.
* The Profiles SDK **does not** inject the driver into the classpath (like the cortex-cdata-plugin does).

## Prerequisites
* Get a CData OEM Key and CData Product Checksum and save these values for later use. If you do not have one, then check with your SRE team or Systems Administrator. Otherwise, try running this example as a Skill.
* Download the CData driver jar files from: http://cdatabuilds.s3.amazonaws.com/support/JDBC_JARS_21.0.8059.zip.
* Add the required driver jars and CData Spark SQL jar to [../main-app/src/main/resources/lib/](../main-app/src/main/resources/lib). These jars will be made available to the Spark driver and executors, as well as the current classpath for development.
  - Copy: `cdata.jdbc.csv.jar`, `cdata.jdbc.sparksql.jar`.
  - If using a BigQuery Connection copy: `cdata.jdbc.googlebigquery.jar`.
  - If using a S3 Connection copy: `cdata.jdbc.amazons3.jar`.
* (Optional) Update the fields within the `plugin_properties` parameters for the CData Connection you will be using.
   [JDBC CData Connections](https://cognitivescale.github.io/cortex-fabric/docs/reference-guides/connection-types#jdbc-cdata-connections) require a secure `plugin_properties` parameter that is a reference to a Cortex Secret.
   The corresponding Secret should be a JSON String that includes parameters specific to the CData JDBC Driver required by your Connection. These parameters will be passed to the CData Driver when reading/writing your data.

Example:

The `cdata-csv` Connection (show below) has a reference to the `csv-props` Secret, which is set in the local
Secret Client (See [CData.java](./src/main/java/com/c12e/cortex/examples/cdata/CData.java) for the implementation).
```yaml
---
apiVersion: cognitivescale.io/v1
kind: Connection
metadata:
name: "cdata-csv"
spec:
title: CDATA CSV
connectionType: jdbc_cdata
params:
- name: query
  value: select * from members_100_v14
- name: classname
  value: cdata.jdbc.csv.CSVDriver
- name: plugin_properties
  value: "#SECURE.csv-props"
```
The JSON value of the `csv-props` Secret (shown below) includes parameters expected by the JDBC CData Driver (`cdata.jdbc.csv.CSVDriver`) and would be encoded to the JSON String: `"{\"GenerateSchemaFiles\":\"OnStart\",\" URI\":\"file://./src/main/resources/data/members_100_v14.csv\",\"Location\":\"./build/tmp/work\"}"`
```json
{
  "GenerateSchemaFiles": "OnStart",
  "URI": "file://./src/main/resources/data/members_100_v14.csv",
  "Location": "./build/tmp/work"
}
```
You can encode a JSON File to a String with `jq` by running: `cat <path-to-file> | jq -c '. | tostring'`.

**NOTE**: The `cortex-cdata-plugin` cannot be used to create JDBC Connections with the Profiles SDK because Spark SQL
has specific requirements for [JDBC connections](https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html).

## Run Locally

This example allows you to use the Profiles SDK locally. It utilizes a local Secret Client for manging the Connection
Secrets. Database passwords, SSL certs, service account JSON file contents, and other variables. should be set in
the [CData.java](./src/main/java/com/c12e/cortex/examples/cdata/CData.java) file via environment variables.

To run this example locally with local Cortex clients (from the parent directory):
1. Build the application.
    ```
    make build
    ```
1. Set the CData OEM Key and Product Checksum.
    ```
    export CDATA_OEM_KEY=key
    export CDATA_PRODUCT_CHECKSUM=checksum
    ```
1. Run the application with Gradle.
    ```
    ./gradlew main-app:run --args="cdata --project local --input cdata-csv --output sink"
    ```

This will read the `cdata-csv` Connection and write it to the `sink` connection defined
in [cdata-connections.yml](../main-app/src/main/resources/spec/cdata-connections.yml). Both connections require:
- An `oem_key` to be set via `CDATA_OEM_KEY`.
- A product checksum to be set via `CDATA_PRODUCT_CHECKSUM`.

The sink file can be found at `./main-app/build/tmp/test-data/sink-ds` after running the command.

## Run Locally in a Docker Container With Spark-submit

To run this example in a Docker container with local Cortex clients (from the parent directory):
1. Build the application.
    ```
    make build
    ```
2. Create the Skill Docker image.
    ```
    make create-app-image
    ```
4. Export the Cortex token, CDATA OEM Key, and Product Checksum environment variables.
    ```
    export CORTEX_TOKEN=<token>
    export CDATA_OEM_KEY=<key>
    export CDATA_PRODUCT_CHECKSUM=<checksum>
    ```
5. Run the application with Docker.
    ```
    docker run -p 4040:4040 --entrypoint="python" \
        -e "CORTEX_TOKEN=${CORTEX_TOKEN}" \
        -e "CDATA_OEM_KEY=${CDATA_OEM_KEY}" \
        -e "CDATA_PRODUCT_CHECKSUM=${CDATA_PRODUCT_CHECKSUM}" \
        -v $(pwd)/cdata-connection/src/main/resources/conf:/app/conf \
        -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
        -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
        profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/spark-conf.json\"}}"
    ```

   NOTES:
    * Port 4040 is forwarded from the container to expose the Spark UI (for debugging).
    * The first volume mount is sharing the [spark-submit config file](./src/main/resources/conf/spark-conf.json).
    * The second volume mount shares the LocalCatalog contents and other local application resources.
    * The third volume mount is sharing output location of the sink connection.

  This will read the `cdata-csv` Connection and write it to the `sink` connection defined
  in [cdata-connections.yml](../main-app/src/main/resources/spec/cdata-connections.yml). The sink file can be found
  at `./main-app/build/tmp/test-data/sink-ds` after running the command.


The logs should be similar to:
```
['/opt/spark/bin/spark-submit', '--master', 'local[*]', '--class', 'com.c12e.cortex.examples.Application', '--conf', 'spark.app.name=CortexProfilesExamples', '--conf', 'spark.ui.enabled=true', '--conf', 'spark.ui.prometheus.enabled=true', '--conf', 'spark.sql.streaming.metricsEnabled=true', '--conf', 'spark.cortex.catalog.impl=com.c12e.cortex.phoenix.LocalCatalog', '--conf', 'spark.cortex.catalog.local.dir=src/main/resources/spec', '--conf', 'spark.cortex.client.secrets.impl=com.c12e.cortex.examples.cdata.CData$CDataSecretClient', '--conf', 'spark.cortex.storage.storageType=file', '--conf', 'spark.cortex.storage.file.baseDir=src/main/resources/data', '--conf', 'spark.kubernetes.driverEnv.CORTEX_TOKEN=eyJhbGciOiJFZERTQSIsImtpZCI6Im5YMHZfcjdiMGJKOC1UVW5Sc3U2cHB2OFVUX0szYVMzdE11d3JzVVp1aEEifQ.eyJzdWIiOiI5ZGMxMjY2Mi1jZDUxLTQ5NDYtYTdmYy0zMTJmZWNlNzg5NTEiLCJhdWQiOiJjb3J0ZXgiLCJpc3MiOiJjb2duaXRpdmVzY2FsZS5jb20iLCJpYXQiOjE2NTc1NTEwNjMsImV4cCI6MTY1ODE1NTg2M30.WS-xexxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxU4ZnCevMfMGR7mfxZq6ofAra_FM0SQOeqruyJuidmdB1Cg', '--conf', 'spark.cortex.phoenix.token=eyJhbGciOiJFZERTQSIsImtpZCI6Im5YMHZfcjdiMGJKOC1UVW5Sc3U2cHB2OFVUX0szYVMzdE11d3JzVVp1aEEifQ.eyJzdWIiOiI5ZGMxMjY2Mi1jZDUxLTQ5NDYtYTdmYy0zMTJmZWNlNzg5NTEiLCJhdWQiOiJjb3J0ZXgiLCJpc3MiOiJjb2duaXRpdmVzY2FsZS5jb20iLCJpYXQiOjE2NTc1NTEwNjMsImV4cCI6MTY1ODE1NTg2M30.WS-xeXI3cphyuJ7iqgbmCRqZwaiN6ERxl8PglEDFU4ZnCevMfMGR7mfxZq6ofAra_FM0SQOeqruyJxxxxxxxxx', 'local:///app/libs/app.jar', 'cdata', '-p', 'local', '-i', 'cdata-csv', '-o', 'sink']
21:05:06,331 |-INFO in ch.qos.logback.classic.LoggerContext[default] - Could NOT find resource [logback-test.xml]
21:05:06,333 |-INFO in ch.qos.logback.classic.LoggerContext[default] - Found resource [logback.xml] at [file:/opt/spark/conf/logback.xml]
21:05:06,334 |-WARN in ch.qos.logback.classic.LoggerContext[default] - Resource [logback.xml] occurs multiple times on the classpath.
21:05:06,334 |-WARN in ch.qos.logback.classic.LoggerContext[default] - Resource [logback.xml] occurs at [jar:file:/opt/spark/jars/main-app-1.0.0-SNAPSHOT.jar!/logback.xml]
21:05:06,334 |-WARN in ch.qos.logback.classic.LoggerContext[default] - Resource [logback.xml] occurs at [file:/opt/spark/conf/logback.xml]
21:05:06,334 |-WARN in ch.qos.logback.classic.LoggerContext[default] - Resource [logback.xml] occurs at [jar:file:/opt/spark/jars/profiles-sdk-6.3.0-M.2.jar!/logback.xml]
21:05:06,411 |-INFO in ch.qos.logback.classic.joran.action.ConfigurationAction - debug attribute not set
21:05:06,415 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - About to instantiate appender of type [ch.qos.logback.core.ConsoleAppender]
21:05:06,417 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - Naming appender as [ROOT]
21:05:06,423 |-INFO in ch.qos.logback.core.joran.action.NestedComplexPropertyIA - Assuming default type [ch.qos.logback.classic.encoder.PatternLayoutEncoder] for [encoder] property
21:05:06,477 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [org.apache.spark] to WARN
21:05:06,478 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [org.apache.spark.sql.execution.CacheManager] to ERROR
21:05:06,478 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [org.apache.spark.ui.SparkUI] to INFO
21:05:06,478 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [com.c12e.cortex.phoenix] to DEBUG
21:05:06,478 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [com.c12e.cortex.profiles] to DEBUG
21:05:06,478 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [com.c12e.cortex.examples] to DEBUG
21:05:06,478 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [org.apache.parquet] to WARN
21:05:06,478 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [org.apache.hadoop] to WARN
21:05:06,478 |-INFO in ch.qos.logback.classic.joran.action.RootLoggerAction - Setting level of ROOT logger to INFO
21:05:06,478 |-INFO in ch.qos.logback.core.joran.action.AppenderRefAction - Attaching appender named [ROOT] to Logger[ROOT]
21:05:06,478 |-INFO in ch.qos.logback.classic.joran.action.ConfigurationAction - End of configuration.
21:05:06,479 |-INFO in ch.qos.logback.classic.joran.JoranConfigurator@f0da945 - Registering current configuration as safe fallback point

WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by org.apache.spark.unsafe.Platform (file:/opt/spark/jars/spark-unsafe_2.12-3.2.1.jar) to constructor java.nio.DirectByteBuffer(long,int)
WARNING: Please consider reporting this to the maintainers of org.apache.spark.unsafe.Platform
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
Using default options: false
21:05:07.162 [main] WARN  o.a.hadoop.util.NativeCodeLoader - Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
21:05:07.976 [main] INFO  org.sparkproject.jetty.util.log - Logging initialized @2534ms to org.sparkproject.jetty.util.log.Slf4jLog
21:05:08.042 [main] INFO  org.sparkproject.jetty.server.Server - jetty-9.4.43.v20210629; built: 2021-06-30T11:07:22.254Z; git: 526006ecfa3af7f1a27ef3a288e2bef7ea9dd7e8; jvm 11.0.15+10
21:05:08.065 [main] INFO  org.sparkproject.jetty.server.Server - Started @2624ms
21:05:08.101 [main] INFO  o.s.jetty.server.AbstractConnector - Started ServerConnector@36cc9385{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
21:05:08.125 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@323f3c96{/jobs,null,AVAILABLE,@Spark}
21:05:08.128 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@6a472566{/jobs/json,null,AVAILABLE,@Spark}
21:05:08.129 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@5e65afb6{/jobs/job,null,AVAILABLE,@Spark}
21:05:08.133 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@6b2e46af{/jobs/job/json,null,AVAILABLE,@Spark}
21:05:08.134 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@2f37f1f9{/stages,null,AVAILABLE,@Spark}
21:05:08.135 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@2af69643{/stages/json,null,AVAILABLE,@Spark}
21:05:08.136 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@48528634{/stages/stage,null,AVAILABLE,@Spark}
21:05:08.138 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@5c77ba8f{/stages/stage/json,null,AVAILABLE,@Spark}
21:05:08.139 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@7a0ef219{/stages/pool,null,AVAILABLE,@Spark}
21:05:08.140 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@7add323c{/stages/pool/json,null,AVAILABLE,@Spark}
21:05:08.141 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@4760f169{/storage,null,AVAILABLE,@Spark}
21:05:08.141 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@35c12c7a{/storage/json,null,AVAILABLE,@Spark}
21:05:08.142 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@25290bca{/storage/rdd,null,AVAILABLE,@Spark}
21:05:08.143 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@4ac86d6a{/storage/rdd/json,null,AVAILABLE,@Spark}
21:05:08.144 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@508a65bf{/environment,null,AVAILABLE,@Spark}
21:05:08.163 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@1e58512c{/environment/json,null,AVAILABLE,@Spark}
21:05:08.164 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@22a736d7{/executors,null,AVAILABLE,@Spark}
21:05:08.165 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@7f353d99{/executors/json,null,AVAILABLE,@Spark}
21:05:08.167 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@5d43409a{/executors/threadDump,null,AVAILABLE,@Spark}
21:05:08.169 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@6ede46f6{/executors/threadDump/json,null,AVAILABLE,@Spark}
21:05:08.178 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@2127e66e{/static,null,AVAILABLE,@Spark}
21:05:08.179 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@7aac8884{/,null,AVAILABLE,@Spark}
21:05:08.180 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@5b852b49{/api,null,AVAILABLE,@Spark}
21:05:08.182 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@895416d{/metrics,null,AVAILABLE,@Spark}
21:05:08.183 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@6edcad64{/jobs/job/kill,null,AVAILABLE,@Spark}
21:05:08.184 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@3e33d73e{/stages/stage/kill,null,AVAILABLE,@Spark}
21:05:08.186 [main] INFO  org.apache.spark.ui.SparkUI - Bound SparkUI to 0.0.0.0, and started at http://41f1cd77c7c5:4040
21:05:09.007 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@3ff54f3d{/metrics/json,null,AVAILABLE,@Spark}
21:05:09.007 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@62a68bcb{/metrics/prometheus,null,AVAILABLE,@Spark}
21:05:09.444 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@28393e82{/SQL,null,AVAILABLE,@Spark}
21:05:09.445 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@7cf63b9a{/SQL/json,null,AVAILABLE,@Spark}
21:05:09.446 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@22a260ff{/SQL/execution,null,AVAILABLE,@Spark}
21:05:09.447 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@50b734c4{/SQL/execution/json,null,AVAILABLE,@Spark}
21:05:09.457 [main] INFO  o.s.j.server.handler.ContextHandler - Started o.s.j.s.ServletContextHandler@1c297897{/static/sql,null,AVAILABLE,@Spark}
21:05:17.503 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Writing to connection: 'file:///opt/spark/work-dir/build/tmp/test-data/sink-ds/'
21:05:20.515 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@36cc9385{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
21:05:20.518 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://41f1cd77c7c5:4040
Pod Name:
Container State:
Termination Reason:
Exit Code: 0
```


## Run as a Skill

These instructions will use the [JDBC CData S3 Connection](#notes-on-jdbc-cdata-s3-connection).

### Prerequisites
* Generate a `CORTEX_TOKEN`.
* Save a JSON Cortex Secret to use as the `plugin_properties` parameter for your Connection. It should include driver specific
  properties for authenticating to Amazon S3 and configuring your JDBC Connection. Refer to the [CData JDBC S3 Documentation](https://cdn.cdata.com/help/SXG/jdbc/).
* Ensure that the Cortex resources exist, specifically the Project, Connections, and any associated Secrets.
* Ensure the CData Driver jar file is included in the Skill Docker image. This should be handled automatically by including the jar in `../main-app/src/main/resources/lib/`.
* When deployed in a Skill, the Profiles SDK utilizes a CDATA OEM Key and Product Checksum saved in the Cortex Cluster.
* Update the [spark-conf.json](./src/main/resources/conf/spark-conf.json) file to:
    - Use the [Remote Catalog](../docs/catalog.md#remote-catalog) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the in-cluster GraphQL API endpoint (`"http://cortex-api.cortex.svc.cluster.local:8080"`) and removing the Local Catalog implementation (`spark.cortex.catalog.impl`).
    - Use the [remote storage client](../docs/backendstorage.md#remote-storage-client) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the GraphQL API endpoint, and remove the local storage client implementation (`spark.cortex.client.storage.impl`).
    - Remove the local Secret client implementation (`spark.cortex.client.secrets.impl`).
    - Update the `app_command` arguments to match your Cortex Project and Profile Schema (`--project`, `--input`, `--output`)

To run this example in Spark cluster mode against a Cortex Cluster with access to the Catalog and Secrets, you must
update the spark configuration file used by the main application (`main-app/src/main/resources/conf/spark-conf.json`) to
match configuration for this example.

Refer to the [instructions for running the Skill Template](../README.md#skill-template) in the top level README for
deploying and invoking the skill.

## Notes on JDBC CData S3 Connection

The CData S3 Connection defined in the local Catalog (shown below) requires authenticating to Amazon S3 along with
providing the CDATA keys. The Connection includes a reference to the `s3-props` Secret with properties specific to
the [CData JDBC S3 Documentation](https://cdn.cdata.com/help/SXG/jdbc/).
```yaml
---
apiVersion: cognitivescale.io/v1
kind: Connection
metadata:
  name: "cdata-s3"
spec:
  title: JDBC CDATA BigQuery
  connectionType: jdbc_cdata
  params:
    - name: plugin_properties
      value: "#SECURE.s3-props"
    - name: classname
      value: cdata.jdbc.amazons3.AmazonS3Driver
    - name: query
      value: "SELECT * FROM Objects WHERE Object = 'objectKey' AND Bucket = 'bucket'"
```

The `s3-props` Secret has a JSON value similar to the object shown below, but the AWS access and secret keys are
replaced with the value of the `S3_ACCESS_KEY ` and `S3_SECRET_KEY` environment variables.
```json
{
  "AWSAccessKey": "<S3_ACCESS_KEY env var>",
  "AWSSecretKey": "<S3_SECRET_KEY env var>"
}
```

You can encode a JSON File to a String with `jq` by running: `cat <path-to-file> | jq -c '. | tostring'`.

To run the CLI application locally in a container:
* Set the `S3_ACCESS_KEY` and `S3_SECRET_KEY` environment variables to authenticate against Amazon, for example:
  ```
  export S3_ACCESS_KEY=""
  export S3_SECRET_KEY=""
  ```
* Update the `query` parameter in the Connection for your data. You can optionally keep the same query and only update the
  name of your S3 `bucket` and `objectKey` to lookup metadata about the S3 Object.
* Update the `app_command` argument in the `spark-conf.json` file to reference the `cdata-s3` connection.
* (Optional) Update the Secret value used by the Connection's `plugin_properties` in `CData.java` if you are using an alternative authentication mechanism.
```
docker run -p 4040:4040 \
    -e CORTEX_TOKEN="${CORTEX_TOKEN}" \
    -e CDATA_OEM_KEY="${CDATA_OEM_KEY}" \
    -e CDATA_PRODUCT_CHECKSUM="${CDATA_PRODUCT_CHECKSUM}" \
    --entrypoint="python" \
    -v $(pwd)/main-app/src/main/resources/credentials/:/secure-storage/ \
    -v $(pwd)/cdata-connection/src/main/resources/conf:/app/conf \
    -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
    -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
    profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/spark-conf.json\"}}"
```

(See [Run locally in a Docker container](#run-locally-in-a-docker-container-with-spark-submit) for instructions to build
the application and create the Docker container).

## Notes on JDBC CData BigQuery Connection

The CData BigQuery Connection defined in the local Catalog (shown below) requires authenticating to Google Cloud Storage
along with providing the CDATA keys. The Connection includes a reference to the `bigquery-props` Secret with properties specific
to the [CDATA JDBC BigQuery Driver](https://cdn.cdata.com/help/DBG/jdbc/default.htm).
```yaml
---
apiVersion: cognitivescale.io/v1
kind: Connection
metadata:
  name: "cdata-bigquery"
spec:
  title: JDBC CDATA BigQuery
  connectionType: jdbc_cdata
  params:
    - name: plugin_properties
      value: "#SECURE.bigquery-props"
    - name: classname
      value: cdata.jdbc.googlebigquery.GoogleBigQueryDriver
    - name: query
      value: "SELECT * FROM `bigquery-public-data.covid19_weathersource_com.postal_code_day_forecast` LIMIT 10"
```

The `bigquery-props` Secret has a JSON value similar to the object shown below, but instead the `OAuthJWTCert` is
replaced with the value of the `BIGQUERY_CREDS_FILE` environment variable, and the `ProjectId` is replaced with the
value of the `BIGQUERY_PROJECT` environment variable.
```json
{
    "AuthScheme": "OAuthJWT",
    "InitiateOAuth": "GETANDREFRESH",
    "OAuthJWTCertType": "GOOGLEJSON",
    "DatasetId": "covid19_weathersource_co",
    "OAuthJWTCert": "<service-account-json-file-path>",
    "ProjectId": "<google-project>"
}
```

You can encode a JSON File to a String with `jq` by running: `cat <path-to-file> | jq -c '. | tostring'`.

<!-- TODO(LA): below google doc isn't public, we should link off to Google docs instead -->
To run the CLI application locally in a container:
* Get a GCP Service Account JSON file as described
  in https://docs.google.com/document/d/1T1u8RMZhDYMIXHk7v3lLF2rzag7xLTr5CLHC-49UiYU/edit#heading=h.756ioo8pxy08 and put
  it into `profiles-examples/main-app/src/main/resources/credentials/`.
* Set the `BIGQUERY_CREDS_FILE` environment variable to the path of the credentials in the container, for example:
  ```
  export BIGQUERY_CREDS_FILE=/opt/spark/work-dir/src/main/resources/credentials/gcs-service-account.json
  ```
* Update the `app_command` argument in the `spark-conf.json` file to reference the `cdata-bigquery` connection.
* Include an additional volume mount to share the Service Account credentials. The below command mounts the GCP Credentials
  file to `/secure-storage/` in the container.
* (Optional) Update the Secret value used by the Connection's `plugin_properties` in `CData.java` if you are using an alternative authentication mechanism.
```
docker run -p 4040:4040 \
    -e CORTEX_TOKEN="${CORTEX_TOKEN}" \
    -e CDATA_OEM_KEY="${CDATA_OEM_KEY}" \
    -e CDATA_PRODUCT_CHECKSUM="${CDATA_PRODUCT_CHECKSUM}" \
    -e BIGQUERY_CREDS_FILE="${BIGQUERY_CREDS_FILE}" \
    -e BIGQUERY_PROJECT="${BIGQUERY_PROJECT}" \
    --entrypoint="python" \
    -v $(pwd)/main-app/src/main/resources/credentials/:/secure-storage/ \
    -v $(pwd)/cdata-connection/src/main/resources/conf:/app/conf \
    -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
    -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
    profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/spark-conf.json\"}}"
```

(See [Run locally in a Docker container](#run-locally-in-a-docker-container-with-spark-submit) for instructions to build
the application and create the Docker container).

### Resources
* [Spark JDBC Data Sources](https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html)
* [CDATA JDBC CSV Driver Documentation](https://cdn.cdata.com/help/RVF/jdbc/default.htm)
* [CDATA JDBC BigQuery Documentation](https://cdn.cdata.com/help/DBG/jdbc/default.htm)
* [CDATA JDBC Postgres Documentation](https://cdn.cdata.com/help/FPG/jdbc/)
* [CData JDBC S3 Documentation](https://cdn.cdata.com/help/SXG/jdbc/)
