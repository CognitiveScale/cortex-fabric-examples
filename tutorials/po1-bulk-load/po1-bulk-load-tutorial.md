---
title: "Bulk Load Tutorial"
linkTitle: "Bulk Load Tutorial"
description: >
  This tutorial provides methods for pushing profile attributes in bulk using the Python library and an example of creating a profile building job.
---

This tutorial provides methods for pushing profile attributes in bulk using the Python library and an example of creating a profile building job.

Cortex offers two runtimes for skills, daemons and jobs. Profile-of-One bulk loading is expected to most commonly
occur within jobs. Accordingly, the examples provided showcase various aspects of Profile-of-One bulk loading jobs.

## Overview

### One-time (Setup) Activities

1. Synthesize profile data to bulk load for demonstration / testing purposes.
2. Save Synthesized data to Managed Content.
3. Save a Profile schema be retrieved in Studio or Python libs (either before or after attribute creation)

### Other Activities

1. Download a Managed Content file to build attributes from
2. Extract the appropriate Entity Events to build specific attributes from
3. Configure the secure variables (key-value pairs) required for bulk loading
4. Access profiles after bulk loading

## Set up Jobs

1. Log into the appropriate Cortex environment

  [Configure the CLI](cortex-fabric/docs/docs/getting-started/use-cli.md) to authenticate to the account where you will run the bulk loading job.

2. Synthesize raw data to load.

   Download and run the following `code/bulk_po1__synthesize_data.py` to create a file called `./po1-data.csv` that is loaded to Manged Content.

   [bulk_po1__synthesize_data.py](/tutorials/po1-bulk-load/code/bulk_po1__synthesize_data.py)

   The data generated is similar to [this](/tutorials/bulk_po1_sample_data.md).

3. Load created data file to Managed Content.

  ```
  cortex content upload cortex-docs/examples/data-for-bulk-po1-load.csv ./po1-data.csv
  ```

4. Save secret for Cortex Graph Database that are needed for bulk load.

  ```
  cortex variables save uri.po1-db <URI>
  ```

  <Alert title="NOTE" color="primary">

  Please contact your Cortex DCI Administrator if you are unsure of the proper URI to use.

  </Alert>

## Understand the job's implementation

The packaged implementation of the Cortex job leverages the bulk load Profile-of-One APIs.

The package can be downloaded [here](tutorials/po1-bulk-load/code/po1-bulk-load-job.zip).

The package contains: `bulk-po1-load-job/main.py`, which implements the Cortex job.

When the `bulk-po1-load-job` is run as a Cortex job, it:

- Downloads the data intended for bulk load as Profile-of-One attributes from Managed Content
- Downloads secrets required to connect the appropriate data the bulk load can happen
- Sets up the proper clients that will be utilized for bulk loading
- Declares how a stream of attribute building events can be derived from the data retrieved from Managed Content.
- Invokes the appropriate call to read chunks of the attribute event stream and bulk load them iteratively.
  - `/bulk-po1-load-job/Dockerfile` packages the job so it can run in Cortex.
  - `/bulk-po1-load-job/Makefile` deploys the Job to the configured Cortex environment.

## Deploy the job

After downloading the .zip package in the previous step, run the following to unzip, build, and deploy the code as a Cortex job.

```
unzip po1-bulk-load-job.zip
cd bulk-po1-load-job/
make deploy VERSION=v1
```

<Alert title="NOTE" color="primary">

Remember to increment the version each time you run the job.

</Alert>


## Invoke the job

After deploying the job, run the following command to test the job and ensure it loads profile data in bulk.

```
make test_job_remotely VERSION=v1 # this tests a specific deployed version of the job
```
