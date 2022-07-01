# Join Connections

This example is contains a CLI application for Joining two Cortex Connections and saving the resulting dataset to
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

## Running locally against a Cortex Cluster

TODO:
* Link to RemoteCatalog/Config
* Set config options, edit the spark-conf file
* Get a Cortex Token, and export it
 
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