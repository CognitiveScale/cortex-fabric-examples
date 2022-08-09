# Profiles Examples Main Application

Refer to top level [README.md](../README.md) for instructions to build and run the Profile examples.
This main application is a CLI with all other examples included as subcommands.

In addition, all (local) resources and data used by the examples are defined in this module.

## Main App Layout
* The logging configuration for this project is controlled by the [logback.xml](./src/main/resources/logback.xml) file.
* The Local Catalog contents are defined in the [spec/](src/main/resources/spec) folder.
* Datasets for Connections are in the [data/](src/main/resources/data) folder.
* The `python/` directory contains the Spark-submit wrapper used for running our application in a Docker container.
