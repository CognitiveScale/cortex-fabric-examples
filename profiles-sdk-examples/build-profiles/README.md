# Build Profiles

This example is a CLI application for building Cortex Profiles. This builds off
the [Local Clients](../local-clients/README.md) example for its setup 
(see [ProfileSchemas](../local-clients/README.md#profile-schemas)).

See [BuildProfile](./src/main/java/com/c12e/cortex/examples/profile/BuildProfile.java).

## Jobs

This example builds Profiles for the `member-profile` [Profile Schema](../local-clients/README.md#profile-schemas) by
using pre-built Job flows for:
- Ingesting a DataSource (`IngestDataSourceJob`)
- Building Profiles (`BuildProfileJob`)

These Jobs flows provide existing functionality, similar to what happens when creating these resources in the Cortex Console.

## Running Locally

To run this example locally with local Cortex Clients:
```bash
make build
./gradlew main-app:run --args="build-profile -p local -ps member-profile"
```

## Running in a Docker Container with Spark-Submit

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