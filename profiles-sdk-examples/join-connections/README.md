# Join Connections

This example is a CLI application for Joining two Cortex Connections and saving the resulting dataset to
another Connection. This builds off the [Local Clients](../local-clients/README.md) example for its setup 
(see [connections](../local-clients/README.md#connections)).

See [JoinConnections.java](./src/main/java/com/c12e/cortex/examples/joinconn/JoinConnections.java) for the full source.

## Running Locally

To run this example with locally with local Cortex clients:
```
# from the parent directory
$ make build
$ ./gradlew main-app:run --args="join-connections -p local -l member-base-file -r member-feedback-file -w member-joined-file -c member_id"
```

This will merge the following connections defined in the Local Catalog, and will populate `member-joined-file` connection:
- member-base-file
- member-feedback-file
- member-flu-risk-file

The joined connection file will be at: `main-app/build/tmp/test-data/joined_v14.csv`

## Running in a Docker container with spark-submit

To run this example with Spark Submit based docker container and local Cortex clients:
```
# from the parent directory
$ make clean build create-app-image

$ export CORTEX_TOKEN=....

$ docker run -p 4040:4040 --entrypoint="python" -e CORTEX_TOKEN="${CORTEX_TOKEN}" \
  -v $(pwd)/join-connections/src/main/resources/conf:/app/conf \
  -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
  -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
  profiles-example submit_job.py "{ \"payload\" : { \"config\" : \"/app/conf/spark-conf.json\" } }"

['/opt/spark/bin/spark-submit', '--master', 'local[*]', '--class', 'com.c12e.cortex.examples.Application', '--conf', 'spark.app.name=CortexProfilesExamples', '--conf', 'spark.ui.enabled=true', '--conf', 'spark.ui.prometheus.enabled=true', '--conf', 'spark.sql.streaming.metricsEnabled=true', '--conf', 'spark.cortex.catalog.impl=com.c12e.cortex.phoenix.LocalCatalog', '--conf', 'spark.cortex.catalog.local.dir=src/main/resources/spec', '--conf', 'spark.cortex.client.secrets.impl=com.c12e.cortex.examples.local.CustomSecretsClient', '--conf', 'spark.cortex.storage.storageType=file', '--conf', 'spark.cortex.storage.file.baseDir=src/main/resources/data', '--conf', 'spark.kubernetes.driverEnv.CORTEX_TOKEN=eyJhbGciOiJFZERTQSIsImtpZCI6Im5YMHZfcjdiMGJKOC1UVW5Sc3U2cHB2OFVUX0szYVMzdE11d3JzVVp1aEEifQ.eyJzdWIiOiI5ZGMxMjY2Mi1jZDUxLTQ5NDYtYTdmYy0zMTJmZWNlNzg5NTEiLCJhdWQiOiJjb3J0ZXgiLCJpc3MiOiJjb2duaXRpdmVzY2FsZS5jb20iLCJpYXQiOjE2NTcxMzkxMDUsImV4cCI6MTY1Nzc0MzkwNX0.XoOTCwmbe6Ja2HQKNug5YSxze8r9pCdFEYzFJULhkTPBb8OPe9CGNvuymPwgBWyooBSy9rMrOehhv9ay8LGbCw', '--conf', 'spark.cortex.phoenix.token=eyJhbGciOiJFZERTQSIsImtpZCI6Im5YMHZfcjdiMGJKOC1UVW5Sc3U2cHB2OFVUX0szYVMzdE11d3JzVVp1aEEifQ.eyJzdWIiOiI5ZGMxMjY2Mi1jZDUxLTQ5NDYtYTdmYy0zMTJmZWNlNzg5NTEiLCJhdWQiOiJjb3J0ZXgiLCJpc3MiOiJjb2duaXRpdmVzY2FsZS5jb20iLCJpYXQiOjE2NTcxMzkxMDUsImV4cCI6MTY1Nzc0MzkwNX0.XoOTCwmbe6Ja2HQKNug5YSxze8r9pCdFEYzFJULhkTPBb8OPe9CGNvuymPwgBWyooBSy9rMrOehhv9ay8LGbCw', 'local:///app/libs/app.jar', 'join-connections', '-p', 'local', '-l', 'member-base-file', '-r', 'member-flu-risk-file', '-w', 'member-joined-file', '-c', 'member_id']
  ...

21:29:16.717 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: csv, uri: ./src/main/resources/data/members_100_v14.csv, extra
21:29:20.747 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Inferred schema from sample of connection (CSV) - project: 'local', connectionName: 'member-base-file'
21:29:20.778 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Finished reading connection (CSV) - project: 'local', connectionName: 'member-base-file'
21:29:20.787 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: parquet, uri: ./src/main/resources/data/member_flu_risk_100_v14.parquet, extra
21:29:20.787 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Reading connection from file_path './src/main/resources/data/member_flu_risk_100_v14.parquet'
21:29:21.231 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Writing to connection: './build/tmp/test-data/joined_v14.csv'
21:29:22.610 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@534c6767{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
21:29:22.613 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://ac5171d970ce:4040
Pod Name:
Container State:
Termination Reason:
Exit Code: 0
```

Spark submit launches the task in the docker container, but local Cortex clients will be used.

Notes:
* The `$CORTEX_TOKEN` environment variable is required by the Spark Submit wrapper, and needs to be a valid JWT token. You can generate this via: `cortex configure token`
* Port 4040 is forwarded from the container to expose the Spark UI (for debugging)
* The 1st volume mount is sharing the [Spark submit config file](./src/main/resources/conf/spark-conf.json)
* The 2cd volume mount shares the LocalCatalog contents and other local application resources
* The 3rd volume mount is the output location of the joined connection

## Running locally against a Cortex Cluster

**Note**: Because this is running outside the Cortex Cluster you will need to know the value of any Secrets used by
the connections. For access to Cortex Secrets, see [Running as a Skill](#running-as-a-skill).

### Prerequisites

To run this example in Spark local mode against a Cortex Cluster with access to the Catalog and Secrets, you will need to:
* Know the [backend storage configuration](../docs/config.md#cortex-backend-storage) for the Cluster, which includes
  names of buckets and access keys for remote storage
* Know the values of Cortex secrets used by the Connection
* Generate a `CORTEX_TOKEN`
* Ensures the Cortex resources exist, specifically the Project and Joining Connections (Left, Right, and Result)
* Update the [spark-conf.json](./src/main/resources/conf/spark-conf.json) file to:
  - use the [Remote Catalog](../docs/catalog.md#remote-catalog) implementation by setting the Cortex URL (`spark.cortex.client.phoenix.url`) to the GraphQL API endpoint (e.g. `https://api.<domain>/fabric/v4/graphql`) and removing the Local Catalog implementation (`spark.cortex.catalog.impl`).
  - update [Local Secret Client](../local-clients/README.md#secrets) with any secrets required by your Connection(s). Ensure to update the project, Secret name, and secret value.
  - Update the `app_command` arguments to match your Cortex project and Connections (`-p`, `-l`, `-r`, `-o` `-c`)

### Example

**Note**: The cortex backend storage configuration could be set in the `spark-conf.json`, but environment variables are
used below to avoid hardcoding access keys in the source.

The below example command is assuming:
* the Cortex backend is using the `minio` instance packaged in  the [Cortex Charts](https://github.com/CognitiveScale/cortex-charts) with access keys `xxxxx`/`xxxxx`
* the [CustomSecretClient](../local-clients/README.md#secrets) provides the secret used by the Connections (loaded from `CONNECTION_SECRET_VALUE`)

```
# from the parent directory
$ make clean build create-app-image

$ docker run -p 4040:4040 --entrypoint="python" \
  -e CORTEX_TOKEN="${CORTEX_TOKEN}" \
  -e CONNECTION_SECRET_VALUE="${CONNECTION_SECRET_VALUE}"
  -e STORAGE_TYPE=s3 \
  -e AWS_ACCESS_KEY_ID=xxxxx \
  -e AWS_SECRET_KEY=xxxxx \
  -e S3_ENDPOINT=http://host.docker.internal:9000 \
  -v $(pwd)/join-connections/src/main/resources/conf:/app/conf \
  -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
  -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
profiles-example submit_job.py "{ \"payload\" : { \"config\" : \"/app/conf/spark-conf.json\" } }"

...
23:37:27.873 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: csv, uri: s3a://fabric-dev-laguirre/members/members_v14_100.csv, extra
23:37:28.110 [main] WARN  o.a.h.metrics2.impl.MetricsConfig - Cannot locate configuration: tried hadoop-metrics2-s3a-file-system.properties,hadoop-metrics2.properties
23:37:33.636 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Inferred schema from sample of connection (CSV) - project: 'laguirre-testi-69257', connectionName: 'member-base'
23:37:34.071 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Finished reading connection (CSV) - project: 'laguirre-testi-69257', connectionName: 'member-base'
23:37:34.163 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Removed hadoop filesystem - format_type: csv, uri: s3a://fabric-dev-laguirre/members/members_flu_risk_v14_100.csv, extra
23:37:34.302 [main] WARN  o.a.h.metrics2.impl.MetricsConfig - Cannot locate configuration: tried hadoop-metrics2-s3a-file-system.properties,hadoop-metrics2.properties
23:37:35.555 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Inferred schema from sample of connection (CSV) - project: 'laguirre-testi-69257', connectionName: 'members-flu-risk'
23:37:35.982 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionReader - Finished reading connection (CSV) - project: 'laguirre-testi-69257', connectionName: 'members-flu-risk'
23:37:36.201 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Writing to connection: 's3a://fabric-dev-laguirre/members/members_joined.csv'
23:37:36.541 [main] WARN  o.a.spark.sql.catalyst.util.package - Truncated the string representation of a plan since it was too large. This behavior can be adjusted by setting 'spark.sql.debug.maxToStringFields'.
23:37:37.641 [main] WARN  o.a.h.f.s.c.AbstractS3ACommitterFactory - Using standard FileOutputCommitter to commit work. This is slow and potentially unsafe.
23:37:39.262 [Executor task launch worker for task 0.0 in stage 5.0 (TID 13)] WARN  o.a.h.f.s.c.AbstractS3ACommitterFactory - Using standard FileOutputCommitter to commit work. This is slow and potentially unsafe.
23:37:43.945 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@c6c82aa{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
23:37:43.948 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://a0a8fdbb79c2:4040
Pod Name:
Container State:
Termination Reason:
Exit Code: 0
```

## Running as a Skill

Update the spark configuration file (e.g. `spark-conf.json`) used by the main application to match configuration for
this example and refer to the [instructions for running the Skill Template](../README.md#skill-template).


## Common Errors
* If you see a NullPointerException, ClassNotFoundException, or Guice DI related exception then double check that ALL configuration options are set correctly and refer to the configuration options
* GraphQL 302 HTTP response - double check API endpoint
* GraphQL general non-200 HTTP response related to connections -> Double check all Cortex resources exist and match the config options