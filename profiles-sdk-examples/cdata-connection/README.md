# CData Connection

This example is a CLI application for reading data from a CData Cortex Connection and writing that data to a separate
Connection. This builds off the [Local Clients](../local-clients/README.md) example for its initial setup, but uses a
separate set of connections defined in [cdata-connections.yml](../main-app/src/main/resources/spec/cdata-connections.yml).

Two source Connections are defined in the [Local Catalog](../local-clients/README.md#catalog):
- `cdata-csv` - Connection to a local `members_100_v14.csv` file that is used for the following examples.
- `cdata-bigquery` - CData Connection to Google BigQuery. To use this you must update the `url` and `query` parameters
  to match your BigQuery data in the [cdata-connections.yml](../main-app/src/main/resources/spec/cdata-connections.yml)
  file. (See [Notes on CData BigQuery Connection](#notes-on-cdata-bigquery-connection)).

See [CData.java](./src/main/java/com/c12e/cortex/examples/cdata/CData.java) for the full source.

## Prerequisites
* Get a CData OEM Key and CData Product Checksum and save these values for later use. If you do not have one then check with your SRE team or Systems Administrator.
* Download the CData driver jar files from: http://cdatabuilds.s3.amazonaws.com/support/JDBC_JARS_21.0.8059.zip
* Add required driver jars and CData Spark SQL jar to [../main-app/src/main/resources/lib/](../main-app/src/main/resources/lib). These jars will be made available to the Spark driver and executors, as well as the current classpath for development.
  - Copy: `cdata.jdbc.csv.jar`, `cdata.jdbc.sparksql.jar`. If using a BigQuery Connection copy: `cdata.jdbc.googlebigquery.jar`
* (Optional) Update `query`, `url`, `driver` in the CData/JDBC connection definitions [cdata-connections.yml](../main-app/src/main/resources/spec/cdata-connections.yml) to control which subset of the datasets will be used. (Refer to [CData documentation](https://cdn.cdata.com/help/RVF/jdbc/pg_JDBCconnectcode.htm) for syntax details.)

**NOTE**: `cortex-cdata-plugin` cannot be used to create JDBC connections, nor can it parse Cortex Connections because
Spark SQL has specific requirement for JDBC (see https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html).

<!--
**NOTE**: The above requires the additional jar files (from [Prerequisites](#prerequisites)) be listed
as [dependencies in the main application](../main-app/build.gradle.kts). This is handled automatically by including the
jar files in the [../main-app/src/main/resources/lib/](../main-app/src/main/resources/lib) folder:
```kotlin
// include extra jars (for CData/BigQuery examples)
runtimeOnly(fileTree("src/main/resources/lib"){ include("*.jar") })
testRuntimeOnly(fileTree("src/main/resources/lib"){ include("*.jar") })
```
-->

## Run Locally

This example allows you to use the Profiles SDK locally. It utilizes a local Secret Client for manging the connection
secrets. Database passwords, SSL certs, service account JSON file contents, and other variables. should be set in
the [CData.java](./src/main/java/com/c12e/cortex/examples/cdata/CData.java) file via environment variables.

To run this example locally with local Cortex clients (from the parent directory):
```
$ make build

$ export CDATA_OEM_KEY=...

$ export CDATA_PRODUCT_CHECKSUM=...

$ ./gradlew main-app:run --args="cdata -p local -i cdata-csv -o sink"
```

**NOTE**: This example is currently not working when running locally, because the Spark executors require a
scratch-directory (`/opt-spark/work-dir`) which does not exist (setting ). This should be sett-able
via `spark.local.dir`/`SPARK_LOCAL_DIRS`, but this configuration has not been overrideable. If you see an error similar
to the following, then instead try
[running this example in a docker container](#running-in-a-docker-container-with-spark-submit):

```
16:29:55.486 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Writing to connection: 'file:///opt/spark/work-dir/build/tmp/test-data/sink-ds/'
16:29:56.005 [main] ERROR o.a.h.m.l.output.FileOutputCommitter - Mkdirs failed to create file:/opt/spark/work-dir/build/tmp/test-data/sink-ds/_temporary/0
16:29:57.191 [Executor task launch worker for task 0.0 in stage 0.0 (TID 0)] ERROR org.apache.spark.executor.Executor - Exception in task 0.0 in stage 0.0 (TID 0)
java.io.IOException: Mkdirs failed to create file:/opt/spark/work-dir/build/tmp/test-data/sink-ds/_temporary/0/_temporary/attempt_202207111629564235807494301289100_0000_m_000000_0 (exists=false, cwd=file:/Users/laguirre/cortex/cortex-fabric-examples/profiles-sdk-examples/main-app)
        at org.apache.hadoop.fs.ChecksumFileSystem.create(ChecksumFileSystem.java:515)
        at org.apache.hadoop.fs.ChecksumFileSystem.create(ChecksumFileSystem.java:500)
        at org.apache.hadoop.fs.FileSystem.create(FileSystem.java:1195)
        at org.apache.hadoop.fs.FileSystem.create(FileSystem.java:1175)
        at org.apache.parquet.hadoop.util.HadoopOutputFile.create(HadoopOutputFile.java:74)
        at org.apache.parquet.hadoop.ParquetFileWriter.<init>(ParquetFileWriter.java:329)
        at org.apache.parquet.hadoop.ParquetOutputFormat.getRecordWriter(ParquetOutputFormat.java:482)
        at org.apache.parquet.hadoop.ParquetOutputFormat.getRecordWriter(ParquetOutputFormat.java:420)
        at org.apache.parquet.hadoop.ParquetOutputFormat.getRecordWriter(ParquetOutputFormat.java:409)
        at org.apache.spark.sql.execution.datasources.parquet.ParquetOutputWriter.<init>(ParquetOutputWriter.scala:36)
        at org.apache.spark.sql.execution.datasources.parquet.ParquetFileFormat$$anon$1.newInstance(ParquetFileFormat.scala:150)
        at org.apache.spark.sql.execution.datasources.SingleDirectoryDataWriter.newOutputWriter(FileFormatDataWriter.scala:161)
        at org.apache.spark.sql.execution.datasources.SingleDirectoryDataWriter.<init>(FileFormatDataWriter.scala:146)
        at org.apache.spark.sql.execution.datasources.FileFormatWriter$.executeTask(FileFormatWriter.scala:290)
        at org.apache.spark.sql.execution.datasources.FileFormatWriter$.$anonfun$write$16(FileFormatWriter.scala:229)
        at org.apache.spark.scheduler.ResultTask.runTask(ResultTask.scala:90)
        at org.apache.spark.scheduler.Task.run(Task.scala:131)
        at org.apache.spark.executor.Executor$TaskRunner.$anonfun$run$3(Executor.scala:506)
        at org.apache.spark.util.Utils$.tryWithSafeFinally(Utils.scala:1462)
        at org.apache.spark.executor.Executor$TaskRunner.run(Executor.scala:509)
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
        at java.base/java.lang.Thread.run(Thread.java:829)
16:29:57.216 [task-result-getter-0] WARN  o.a.spark.scheduler.TaskSetManager - Lost task 0.0 in stage 0.0 (TID 0) (c02wq091htdf.attlocal.net executor driver): java.io.IOException: Mkdirs failed to create file:/opt/spark/work-dir/build/tmp/test-data/sink-ds/_temporary/0/_temporary/attempt_202207111629564235807494301289100_0000_m_000000_0 (exists=false, cwd=file:/Users/laguirre/cortex/cortex-fabric-examples/profiles-sdk-examples/main-app)
        at org.apache.hadoop.fs.ChecksumFileSystem.create(ChecksumFileSystem.java:515)
        at org.apache.hadoop.fs.ChecksumFileSystem.create(ChecksumFileSystem.java:500)
        at org.apache.hadoop.fs.FileSystem.create(FileSystem.java:1195)
        at org.apache.hadoop.fs.FileSystem.create(FileSystem.java:1175)
        at org.apache.parquet.hadoop.util.HadoopOutputFile.create(HadoopOutputFile.java:74)
        at org.apache.parquet.hadoop.ParquetFileWriter.<init>(ParquetFileWriter.java:329)
        at org.apache.parquet.hadoop.ParquetOutputFormat.getRecordWriter(ParquetOutputFormat.java:482)
        at org.apache.parquet.hadoop.ParquetOutputFormat.getRecordWriter(ParquetOutputFormat.java:420)
        at org.apache.parquet.hadoop.ParquetOutputFormat.getRecordWriter(ParquetOutputFormat.java:409)
        at org.apache.spark.sql.execution.datasources.parquet.ParquetOutputWriter.<init>(ParquetOutputWriter.scala:36)
        at org.apache.spark.sql.execution.datasources.parquet.ParquetFileFormat$$anon$1.newInstance(ParquetFileFormat.scala:150)
        at org.apache.spark.sql.execution.datasources.SingleDirectoryDataWriter.newOutputWriter(FileFormatDataWriter.scala:161)
        at org.apache.spark.sql.execution.datasources.SingleDirectoryDataWriter.<init>(FileFormatDataWriter.scala:146)
        at org.apache.spark.sql.execution.datasources.FileFormatWriter$.executeTask(FileFormatWriter.scala:290)
        at org.apache.spark.sql.execution.datasources.FileFormatWriter$.$anonfun$write$16(FileFormatWriter.scala:229)
        at org.apache.spark.scheduler.ResultTask.runTask(ResultTask.scala:90)
        at org.apache.spark.scheduler.Task.run(Task.scala:131)
        at org.apache.spark.executor.Executor$TaskRunner.$anonfun$run$3(Executor.scala:506)
        at org.apache.spark.util.Utils$.tryWithSafeFinally(Utils.scala:1462)
        at org.apache.spark.executor.Executor$TaskRunner.run(Executor.scala:509)
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
        at java.base/java.lang.Thread.run(Thread.java:829)

16:29:57.217 [task-result-getter-0] ERROR o.a.spark.scheduler.TaskSetManager - Task 0 in stage 0.0 failed 1 times; aborting job
16:29:57.228 [main] ERROR o.a.s.s.e.d.FileFormatWriter - Aborting job 34fffb76-0ae6-420e-9fa8-035623f9b018.
org.apache.spark.SparkException: Job aborted due to stage failure: Task 0 in stage 0.0 failed 1 times, most recent failure: Lost task 0.0 in stage 0.0 (TID 0) (c02wq091htdf.attlocal.net executor driver): java.io.IOException: Mkdirs failed to create file:/opt/spark/work-dir/build/tmp/test-data/sink-ds/_temporary/0/_temporary/attempt_202207111629564235807494301289100_0000_m_000000_0 (exists=false, cwd=file:/Users/laguirre/cortex/cortex-fabric-examples/profiles-sdk-examples/main-app)
```

This will read the `cdata-csv` Connection and write it to the `sink` connection defined
in [cdata-connections.yml](../main-app/src/main/resources/spec/cdata-connections.yml). Both connections require:
- An `oem_key` to be set via `CDATA_OEM_KEY`.
- A product checksum to be set via `CDATA_PRODUCT_CHECKSUM`.

The sink file can be found at `./main-app/build/tmp/test-data/sink-ds` after running the command.

## Run in a Docker container with Spark-Submit

To run this example in a docker container with local Cortex clients (from the parent directory):
```
$ make clean build create-app-image

$ export CORTEX_TOKEN=...

$ export CDATA_OEM_KEY=...

$ export CDATA_PRODUCT_CHECKSUM=...

$ docker run -p 4040:4040 --entrypoint="python" \
    -e "CORTEX_TOKEN=${CORTEX_TOKEN}" \
    -e "CDATA_OEM_KEY=${CDATA_OEM_KEY}" \
    -e "CDATA_PRODUCT_CHECKSUM=${CDATA_PRODUCT_CHECKSUM}" \
    -v $(pwd)/cdata-connection/src/main/resources/conf:/app/conf \
    -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
    -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
    profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/spark-conf.json\"}}"
    
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

This will read the `cdata-csv` Connection and write it to the `sink` connection defined
in [cdata-connections.yml](../main-app/src/main/resources/spec/cdata-connections.yml). The sink file can be found
at `./main-app/build/tmp/test-data/sink-ds` after running the command.

Notes:
* Port 4040 is forwarded from the container to expose the Spark UI (for debugging).
* The first volume mount is sharing the [Spark submit config file](./src/main/resources/conf/spark-conf.json).
* The second volume mount shares the LocalCatalog contents and other local application resources.
* The third volume mount is the output location of the joined connection.

## Notes on CData BigQuery connection

The CData BigQuery Connection (shown below) requires authenticating to Google Cloud Storage along with providing the
CDATA keys. This example is configured to use Google Service Account JSON credentials that is specified
at `/secure-storage/gcs-service-account.json` (see the `OAuthJWTCert` in the `url` parameter).

```yaml
---
apiVersion: cognitivescale.io/v1
kind: Connection
metadata:
  name: "cdata-bigquery"
spec:
  title: CDATA BigQuery
  connectionType: jdbc
  params:
    - name: query
      value: SELECT * FROM `bigquery-public-data.covid19_weathersource_com.postal_code_day_forecast` WHERE forecast_date = '2022-06-03' LIMIT 10
    - name: url
      value: "jdbc:cdata:googlebigquery:AuthScheme:OAuthJWT;InitiateOAuth=GETANDREFRESH;OAuthJWTCertType:GOOGLEJSON;OAuthJWTCert:/secure-storage/gcp-service-account.json;ProjectId=fabric-qa;DatasetId=covid19_weathersource_co;"
    - name: driver
      value: cdata.jdbc.googlebigquery.GoogleBigQueryDriver
    - name: oem_key
      value: "#SECURE.oem_key"
```


<!-- TODO(LA): below google doc isn't public, we should link off to Google docs instead -->
To run this example locally in a container:
* Get a GCP Service Account JSON as described.
  in https://docs.google.com/document/d/1T1u8RMZhDYMIXHk7v3lLF2rzag7xLTr5CLHC-49UiYU/edit#heading=h.756ioo8pxy08 and put
  it into `profiles-examples/main-app/src/main/resources/credentials/`.
* Update the `url` and `query` parameter (including the `ProjectId`) to match your data.
* Include an additional volume mount to share the Service Account credentials.
```
$ docker run -p 4040:4040 \
    -e CORTEX_TOKEN="${CORTEX_TOKEN}" \
    -e CDATA_OEM_KEY="${CDATA_OEM_KEY}" \
    -e CDATA_PRODUCT_CHECKSUM="{CDATA_PRODUCT_CHECKSUM}" \
    --entrypoint="python" \
    -v $(pwd)/main-app/src/main/resources/credentials/:/secure-storage/
    -v $(pwd)/cdata-connection/src/main/resources/conf:/app/conf
    -v $(pwd)/main-app/src:/opt/spark/work-dir/src
    -v $(pwd)/main-app/build:/opt/spark/work-dir/build
    profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/spark-conf.json\"}}"
```


Currently, CData BigQuery JDBC connection is failing with:
```
 java.sql.SQLException: 'port' is not a valid connection property.
        at XcoreXgooglebigqueryX210X8059.qrc.a(Unknown Source)
        at XcoreXgooglebigqueryX210X8059.qrc.b(Unknown Source)
        at cdata.jdbc.googlebigquery.GoogleBigQueryDriver.connect(Unknown Source)
        at org.apache.spark.sql.execution.datasources.jdbc.connection.BasicConnectionProvider.getConnection(BasicConnectionProvider.scala:49)
        at org.apache.spark.sql.execution.datasources.jdbc.connection.ConnectionProvider$.create(ConnectionProvider.scala:77)
```
Looks like Spark is passing `port` implicitly.
