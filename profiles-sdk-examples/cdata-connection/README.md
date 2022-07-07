# CData Connection

This example is a CLI application for reading data from a CData Cortex Connection and writing said data to separate
Connection. This builds off the [Local Clients](../local-clients/README.md) example for its initial setup, but uses a
separate set of connections defined in [cdata-connections.yml](../main-app/src/main/resources/spec/cdata-connections.yml).

See [CData.java](./src/main/java/com/c12e/cortex/examples/cdata/CData.java) for the full source.

## Prerequisites
* Download the CData driver jars from http://cdatabuilds.s3.amazonaws.com/support/JDBC_JARS_21.0.8059.zip
* Add required driver jars and CData Spark SQL jar to [../main-app/src/main/resources/lib/](../main-app/src/main/resources/lib). These jars will be made available to the Spark Driver and Executors.
* (Optional) Update `query`, `url`, `driver` in the CData/JDBC connection definitions [cdata-connections.yml](../main-app/src/main/resources/spec/cdata-connections.yml) to control which subset of the datasets will be used. Refer to [CData documentation](https://cdn.cdata.com/help/RVF/jdbc/pg_JDBCconnectcode.htm) for syntax details.
* **NOTE**: We can't use `cortex-cdata-plugin` to create JDBC connection nor parse Cortex Connections because Spark SQL has specific requirement for JDBC, see https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html

This example utilizes a local Secret Client for manging the connection secrets. Database passwords, SSL certs, service
account JSON file contents, etc. should be set in
the [CData.java](./src/main/java/com/c12e/cortex/examples/cdata/CData.java) file via environment variables.

## Running Locally

To run this example locally with local Cortex clients (from the parent directory):
```
$ make build

$ export CDATA_OEM_KEY=...

$ export CDATA_PRODUCT_CHECKSUM=...

$ ./gradlew main-app:run --args="cdata -p local -i cdata-csv -o sink"
```

This will read the `cdata-csv` Connection and write it to the `sink` connection defined
in the `cdata-connections.yml` file. Both connections require:
- an `oem_key` to be set via `CDATA_OEM_KEY`
- a product checksum to be set via `CDATA_PRODUCT_CHECKSUM`

The sink file can be found at `./main-app/build/tmp/test-data/sink-ds`.

## Running in a Docker container with spark-submit

```
$ make clean build create-app-image

$ docker run -p 4040:4040 --entrypoint="python" \
    -e "CORTEX_TOKEN=xxxxxxxxx" \
    -e "CDATA_OEM_KEY=<CData OEM Key>" \
    -e "CDATA_PRODUCT_CHECKSUM=<CData product checksum>" \
    -v $(pwd)/main-app/src/main/resources/conf:/app/conf \
    -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
    -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
    profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/local.json\"}}"
```

Notes:
* Port 4040 is forwarded from the container to expose the Spark UI (for debugging)
* The 1st volume mount is sharing the [Spark submit config file](./src/main/resources/conf/spark-conf.json)
* The 2cd volume mount shares the LocalCatalog contents and other local application resources
* The 3rd volume mount is the output location of the joined connection

## Running as a Skill

TODO.

<!--
### For CData example
* Get CData driver jars from http://cdatabuilds.s3.amazonaws.com/support/JDBC_JARS_21.0.8059.zip
* Add required driver jars and CData Spark SQL jar to `profiles-examples/main-app/src/main/resources/lib`
* Update `query`, `url`, `driver` in CData/JDBC spec `connectors.yml`. Get these syntax details from CData documentation https://cdn.cdata.com/help/RVF/jdbc/pg_JDBCconnectcode.htm
* For database password, SSL certs, service account JSON file update `CData.java` example and add secrets through env var or file
* Docker command to run in local
```
docker run -p 4040:4040 -e "CORTEX_TOKEN=xxxxxxxxx" -e "CDATA_OEM_KEY=<CData OEM Key>"  -e "CDATA_PRODUCT_CHECKSUM=<CData product checksum>"  --entrypoint="python" -v $(pwd)/main-app/src/main/resources/conf:/app/conf -v $(pwd)/main-app/src:/opt/spark/work-dir/src -v $(pwd)/main-app/build:/opt/spark/work-dir/build profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/local.json\"}}"
```
* We can't use cortex-cdata-plugin to create JDBC connection or parsing cortex connection because Spark SQL has specific requirement for JDBC https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html

#### Notes on CData BigQuery connection
Get GCP Service Account JSON as described in https://docs.google.com/document/d/1T1u8RMZhDYMIXHk7v3lLF2rzag7xLTr5CLHC-49UiYU/edit#heading=h.756ioo8pxy08. and put into `profiles-examples/main-app/src/main/resources/credentials`
```
docker run -p 4040:4040 -e "CORTEX_TOKEN=xxxxxxxxx" -e "CDATA_OEM_KEY=<CData OEM Key>"  -e "CDATA_PRODUCT_CHECKSUM=<CData product checksum>"  --entrypoint="python" -v $(pwd)/main-app/src/main/resources/credentials/:/secure-storage/ -v $(pwd)/main-app/src/main/resources/conf:/app/conf -v $(pwd)/main-app/src:/opt/spark/work-dir/src -v $(pwd)/main-app/build:/opt/spark/work-dir/build profiles-example submit_job.py "{\"payload\" : {\"config\": \"/app/conf/local.json\"}}"
```
Currently, CData BigQuery JDBC connection is failing with
```
 java.sql.SQLException: 'port' is not a valid connection property.
        at XcoreXgooglebigqueryX210X8059.qrc.a(Unknown Source)
        at XcoreXgooglebigqueryX210X8059.qrc.b(Unknown Source)
        at cdata.jdbc.googlebigquery.GoogleBigQueryDriver.connect(Unknown Source)
        at org.apache.spark.sql.execution.datasources.jdbc.connection.BasicConnectionProvider.getConnection(BasicConnectionProvider.scala:49)
        at org.apache.spark.sql.execution.datasources.jdbc.connection.ConnectionProvider$.create(ConnectionProvider.scala:77)
```
Looks like Spark is passing `port` implicitly
 -->