# Datasource Refresh

This example is contains a CLI application for refreshing a DataSource by reading its Cortex Connection and writing the
dataset to the DataSource. This builds off the [Local Clients](../local-clients/README.md) example for its setup 
(see [Data Sources](../local-clients/README.md#data-sources)).

See [DataSourcesRW.java](./src/main/java/com/c12e/cortex/examples/datasource/DataSourceRW.java) for the full source.

## Running Locally

To run this example with locally with local Cortex clients:
```bash
./gradlew main-app:run --args="datasource-refresh -p local -d member-base-ds"
```

This will write the `member-base-ds` DataSource to a local file. **NOTE:** Because this is running with a Catalog instance
and local filesystem as the Cortex backend, the resulting for the DataSource will be written to a local file and does
not exist prior to running the above.

(The resulting DataSource is saved as a DeltaTable in `build/test-data/cortex-profiles/sources/local/member-base-ds-delta`)

## Running in a Docker container with spark-submit

Run this example in a docker container with local clients, from the parent directory:
```bash
make clean build create-app-image

docker run -p 4040:4040 --entrypoint="python" -e CORTEX_TOKEN="xxx" \
  -v $(pwd)/datasource-refresh/src/main/resources/conf:/app/conf \
  -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
  -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
profiles-example submit_job.py "{ \"payload\" : { \"config\" : \"/app/conf/spark-conf.json\" } }"
```

Notes:
* The `CORTEX_TOKEN` environment variable is required by the Spark Submit wrapper
* Port 4040 is forwarded from the container to expose the Spark UI
* The 1st volume mount is sharing the options
* The 2cd volume mount shares the LocalCatalog contents with the container, and the Spark-submit python script
* The 3rd volume mount is the output of the joined connection

## Running locally against a Cortex Cluster

TODO

## Running as a Skill

TODO