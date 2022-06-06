# Cortex fabric example repository

This repository contains examples for help Cortex Fabric users.

## Contents
 | Folder | Language | Description |
 | --------| -------- |----------- |
 | [cortex-python-lib-examples/connection-example](./cortex-python-lib-examples/connection-example) | Python | An example daemon-Skill that connects to a Mongo database. Uses cortex-python library to get Connection definition. |
 | [cortex-python-lib-examples/gcs-connection-skill](./cortex-python-lib-examples/gcs-connection-skill) | Python | An example job-Skill that reads data from a Google Cloud Storage (GCS) saved as a Cortex Connection. |
 | [cortex-python-lib-examples/managed-content-example](./cortex-python-lib-examples/managed-content-example) | Python | An example job-Skill that uses the cortex-python library's ManagedContentClient to upload and download a message to Managed Content. |
 | [cortex-python-lib-examples/simple-experiment](./cortex-python-lib-examples/simple-experiment) | Python | A pair of Skills that train (job) and predict (daemon) a Model using sklearn's RandomForestClassifier and the cortex-python library's Experiment class. |
 | [profiles-jar-examples](./profiles-jar-examples) | Java/Kotlin | A set of examples utilizing the Fabric profiles-jar. |
 | [word-count-agent/](./word-count-agent) | Python | An Agent with one daemon-Skill that reads `text` from the payload and responds with a word count.

