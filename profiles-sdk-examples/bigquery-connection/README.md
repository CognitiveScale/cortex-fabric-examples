# Reading from BigQuery

This example is a CLI application that writes data from a Google BigQuery to the location of a Cortex Connection. This
builds off the [Local Clients](../local-clients/README.md) example for its initial setup.

## Prerequisites

* Download the [BigQuery Spark connector](https://repo1.maven.org/maven2/com/google/cloud/spark/spark-bigquery-with-dependencies_2.12/0.25.0/spark-bigquery-with-dependencies_2.12-0.25.0.jar)
  and save the file in [../main-ap/src/main/resources/lib/](../main-app/src/main/resources/lib/).
* The BigQuery Spark Connector is a required dependency to run this
  example (`com.google.cloud.spark:spark-bigquery-with-dependencies_2.12:0.25.0`).
* Get a GCP Service Account JSON as described in: https://docs.google.com/document/d/1T1u8RMZhDYMIXHk7v3lLF2rzag7xLTr5CLHC-49UiYU/edit#heading=h.756ioo8pxy08.
  Save this file in `profiles-examples/main-app/src/main/resources/credentials` for future use (e.g. `gcs-service-account.json`).

## Running locally

To run this example locally with local Cortex clients (from the parent directory):
```
$ make build

$ export BIGQUERY_CREDS_FILE=$(PWD)/main-app/src/main/resources/credentials/gcs-service-account.json

$ ./gradlew main-app:run --args="cdata -p local -i cdata-csv -o sink"
...

(spark.databricks.delta.schema.autoMerge.enabled,true), (spark.cortex.catalog.local.dir,src/main/resources/spec), (spark.sql.warehouse.dir,file:/Users/laguirre/cortex/cortex-fabric-examples/profiles-sdk-examples/main-app/spark-warehouse)15:25:10.695 [main] INFO  c.g.c.s.b.d.DirectBigQueryRelation - |Querying table bigquery-public-data.samples.shakespeare, parameters sent from Spark:|requiredColumns=[word,word_count,corpus,corpus_date],|filters=[]
15:25:13.052 [main] INFO  c.g.c.s.b.direct.BigQueryRDDFactory - Created read session for table 'bigquery-public-data.samples.shakespeare': projects/fabric-qa/locations/us/sessions/CAISDHVzbzczR0xLdjVLbBoCamQaAmpmGgJpchoCb2oaAmpxGgJuYRoCb3MaAm93GgJqchoCaXcaAmpjGgJwehoCcHkaAmpzGgJweBoCb3YaAmppGgJpYRoCaWMaAnBs
15:25:14.476 [Executor task launch worker for task 0.0 in stage 0.0 (TID 0)] INFO  c.g.c.s.b.r.o.a.a.m.BaseAllocator - Debug mode disabled.
15:25:14.480 [Executor task launch worker for task 0.0 in stage 0.0 (TID 0)] INFO  c.g.c.s.b.r.o.a.a.m.DefaultAllocationManagerOption - allocation manager type not specified, using netty as the default type
15:25:14.481 [Executor task launch worker for task 0.0 in stage 0.0 (TID 0)] INFO  c.g.c.s.b.r.o.a.a.m.CheckAllocator - Using DefaultAllocationManager at memory/DefaultAllocationManagerFactory.class
+----+----------+-------+-----------+
|word|word_count| corpus|corpus_date|
+----+----------+-------+-----------+
|LVII|         1|sonnets|          0|
+----+----------+-------+-----------+
only showing top 1 row

15:25:15.268 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Writing to connection: './build/tmp/test-data/sink-ds/'
15:25:15.315 [main] INFO  c.g.c.s.b.d.DirectBigQueryRelation - |Querying table bigquery-public-data.samples.shakespeare, parameters sent from Spark:|requiredColumns=[word,word_count,corpus,corpus_date],|filters=[]
15:25:16.092 [main] INFO  c.g.c.s.b.direct.BigQueryRDDFactory - Created read session for table 'bigquery-public-data.samples.shakespeare': projects/fabric-qa/locations/us/sessions/CAISDE1sYzBITDJnYnd1MRoCamQaAmpmGgJpchoCb2oaAmpxGgJuYRoCb3MaAm93GgJqchoCaXcaAmpjGgJwehoCcHkaAmpzGgJweBoCb3YaAmppGgJpYRoCaWMaAnBs
15:25:17.820 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@333c8791{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
15:25:17.821 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://c02wq091htdf.attlocal.net:4040

Deprecated Gradle features were used in this build, making it incompatible with Gradle 8.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

See https://docs.gradle.org/7.4/userguide/command_line_interface.html#sec:command_line_warnings

BUILD SUCCESSFUL in 21s
```

## Running locally in a Docker Container with Spark-Submit

To run this example in a docker container with local Cortex clients (from the parent directory):
```
$ make build create-app-image

$ export CORTEX_TOKEN=...

# directory is in docker container
$ export BIGQUERY_CREDS_FILE=/opt/spark/work-dir/src/main/resources/credentials/gcs-service-account.json

$ docker run -p 4040:4040 \
  --entrypoint="python" \
  -e CORTEX_TOKEN="${CORTEX_TOKEN}" \
  -e BIGQUERY_CREDS_FILE="${BIGQUERY_CREDS_FILE}" \
  -v $(pwd)/bigquery-connection/src/main/resources/conf:/app/conf \
  -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
  -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/spark-conf.json\"}}"

19:29:19.134 [main] INFO  c.g.c.s.b.direct.BigQueryRDDFactory - Created read session for table 'bigquery-public-data.samples.shakespeare': projects/fabric-qa/locations/us/sessions/CAISDC1DWTRobUhUWThuTxoCamQaAmpmGgJpchoCb2oaAmpxGgJuYRoCb3MaAm93GgJqchoCaXcaAmpjGgJwehoCcHkaAmpzGgJweBoCb3YaAmppGgJpYRoCaWMaAnBs
19:29:21.220 [Executor task launch worker for task 0.0 in stage 0.0 (TID 0)] INFO  c.g.c.s.b.r.o.a.a.m.BaseAllocator - Debug mode disabled.
19:29:21.224 [Executor task launch worker for task 0.0 in stage 0.0 (TID 0)] INFO  c.g.c.s.b.r.o.a.a.m.DefaultAllocationManagerOption - allocation manager type not specified, using netty as the default type
19:29:21.225 [Executor task launch worker for task 0.0 in stage 0.0 (TID 0)] INFO  c.g.c.s.b.r.o.a.a.m.CheckAllocator - Using DefaultAllocationManager at memory/DefaultAllocationManagerFactory.class
+----+----------+-------+-----------+
|word|word_count| corpus|corpus_date|
+----+----------+-------+-----------+
|LVII|         1|sonnets|          0|
+----+----------+-------+-----------+
only showing top 1 row

19:29:21.946 [main] DEBUG c.c.c.p.m.c.DefaultCortexConnectionWriter - Writing to connection: 'file:///opt/spark/work-dir/build/tmp/test-data/sink-ds/'
19:29:22.001 [main] INFO  c.g.c.s.b.d.DirectBigQueryRelation - |Querying table bigquery-public-data.samples.shakespeare, parameters sent from Spark:|requiredColumns=[word,word_count,corpus,corpus_date],|filters=[]
19:29:22.738 [main] INFO  c.g.c.s.b.direct.BigQueryRDDFactory - Created read session for table 'bigquery-public-data.samples.shakespeare': projects/fabric-qa/locations/us/sessions/CAISDHU1U1BuWS1BbEVOQxoCamQaAmpmGgJpchoCb2oaAmpxGgJuYRoCb3MaAm93GgJqchoCaXcaAmpjGgJwehoCcHkaAmpzGgJweBoCb3YaAmppGgJpYRoCaWMaAnBs
19:29:25.032 [shutdown-hook-0] INFO  o.s.jetty.server.AbstractConnector - Stopped Spark@2b03d52f{HTTP/1.1, (http/1.1)}{0.0.0.0:4040}
19:29:25.035 [shutdown-hook-0] INFO  org.apache.spark.ui.SparkUI - Stopped Spark web UI at http://9784fa1fbb70:4040
Pod Name:
Container State:
Termination Reason:
Exit Code: 0
```

Notes:
* Port 4040 is forwarded from the container to expose the Spark UI (for debugging)
* The `BIGQUERY_CREDS_FILE` environment is the path to service account file in the container
* The 1st volume mount is sharing the [Spark submit config file](./src/main/resources/conf/spark-conf.json)
* The 2cd volume mount shares the LocalCatalog contents and other local application resources
* The 3rd volume mount is the output location of the joined connection
 
The sink file can be found at `./main-app/build/tmp/test-data/sink-ds` after running the command.

**NOTE:** The sink connection is defined in the [cdata-connections.yml](../main-app/src/main/resources/spec/cdata-connections.yml) file, and the above
is using a [BigQuery Sample table](https://cloud.google.com/bigquery/public-data#sample_tables).