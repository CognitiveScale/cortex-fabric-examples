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

To run locally against a Cortex cluster, you will need to:
* Use the [Remote Catalog](../docs/catalog.md#remote-catalog) implementation instead of the Local Catalog. To do this update the `spark-conf.json` file by removing local catalog implementation and local catalog directory. See [config.md](../docs/config.md#configuration-options).
* Set the configuration properties for the [Remote Catalog](../docs/config.md#using-a-remote-catalog)
   - Set the Cortex URL
   - Export a Cortex Token
* Update the [Local Secret Client](../local-clients/README.md#secrets)  with any secrets required by your Connection(s)
* Update application command in `spark-conf.json` for your project and connections

```bash
# from the parent directory
make clean build create-app-image

docker run -p 4040:4040 --entrypoint="python" -e CORTEX_TOKEN="${CORTEX_TOKEN}" \
  -v $(pwd)/join-connections/src/main/resources/conf:/app/conf \
  -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
  -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
profiles-example submit_job.py "{ \"payload\" : { \"config\" : \"/app/conf/spark-conf.json\" } }"
```

## Running as a Skill

TODO