# Datasource Refresh

This example is contains a CLI application for refreshing a DataSource by reading its Cortex Connection and writing the
dataset to the DataSource. This builds off the [Local Clients](../local-clients/README.md) example for its setup 
(see [Data Sources](../local-clients/README.md#data-sources)).

See [DataSourcesRW.java](./src/main/java/com/c12e/cortex/examples/datasource/DataSourceRW.java) for the full source.

## Running Locally

To run this example locally with local Cortex clients:
```
$ make clean build

$ ./gradlew main-app:run --args="datasource-refresh -p local -d member-base-ds"
```

This will write the `member-base-ds` DataSource to a local file. **NOTE:** Because this is running with a local Catalog 
and local filesystem as the Cortex backend, the result of the DataSource will be written to a local file and does
not exist prior to running the above.

The resulting DataSource is saved as a DeltaTable in `../main-app/build/test-data/cortex-profiles/sources/local/member-base-ds-delta`

## Running in a Docker container with Spark-Submit

Run this example in a docker container with local clients, from the parent directory:
```bash
make clean build create-app-image

docker run -p 4040:4040 --entrypoint="python" -e CORTEX_TOKEN="xxx" \
  -v $(pwd)/datasource-refresh/src/main/resources/conf:/app/conf \
  -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
  -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
profiles-example submit_job.py "{ \"payload\" : { \"config\" : \"/app/conf/spark-conf.json\" } }"
```



## Running locally against a Cortex Cluster

TODO

## Running as a Skill

TODO