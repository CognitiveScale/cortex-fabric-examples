# Join Connections

This example is a CLI application for Joining two Cortex Connections and saving the resulting dataset to
another Connection. This builds off the [Local Clients](../local-clients/README.md) example for its setup 
(see [connections](../local-clients/README.md#connections)).

See [JoinConnections.java](./src/main/java/com/c12e/cortex/examples/joinconn/JoinConnections.java) for the full source.

## Running Locally

To run this example with locally with local Cortex clients:
```bash
# from the parent directory
make build
./gradlew main-app:run --args="join-connections -p local -l member-base-file -r member-feedback-file -w member-joined-file -c member_id"
```

This will merge the following connections defined in the Local Catalog, and will populate `member-joined-file` connection:
- member-base-file
- member-feedback-file
- member-flu-risk-file

(To see the joined file, see: `main-app/build/tmp/test-data/joined_v14.csv`).

## Running in a Docker container with spark-submit

To run this example with spark-submit/docker container and local Cortex clients:
```bash
# from the parent directory
make clean build create-app-image

docker run -p 4040:4040 --entrypoint="python" -e CORTEX_TOKEN="xxx" \
  -v $(pwd)/join-connections/src/main/resources/conf:/app/conf \
  -v $(pwd)/main-app/src:/opt/spark/work-dir/src \
  -v $(pwd)/main-app/build:/opt/spark/work-dir/build \
profiles-example submit_job.py "{ \"payload\" : { \"config\" : \"/app/conf/spark-conf.json\" } }"
```

Spark submit launches the task in the docker container, but local Cortex clients will be used.

Notes:
* The `CORTEX_TOKEN` environment variable is required by the Spark Submit wrapper
* Port 4040 is forwarded from the container to expose the Spark UI (for debugging)
* The 1st volume mount is sharing the options
* The 2cd volume mount shares the LocalCatalog contents with the container, and the Spark-submit python script
* The 3rd volume mount is the output of the joined connection

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