# Profiles Examples Main Application

For instructions building and running see parent the [README.md](../README.md). This example is structured as CLI
application with all other examples as subcommands. In addition, all (local) resources and data used by the examples are
defined in this module.

## Main App Layout
* The logging configuration for this project is controlled by the [logback.xml](./src/main/resources/logback.xml) file.
* The Local Catalog contents are defined in the [spec/](src/main/resources/spec) folder.
* Datasets for Connections are in the [data/](src/main/resources/data) folder.
* The `python/` directory contains the spark-submit wrapper used for running our application in a docker container (see `Dockerfile`).
