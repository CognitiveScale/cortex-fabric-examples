### Spark Stream Ingest Job (Skill)

Cortex skill does a spark stream ingest

### Prerequisites

- Python 3.x
- Docker client
- Cortex CLI 
- URL/Credentials for a cortex instance

### Project Structure
Job source code location `src/main/python/main.py`
Job submit code location `submit_job.py`
spark config location `config.json`
Dockerfile for skill that does the spark submit `Dockerfile`
Dockerfile to build Driver /executor images `spark-k8s-container-image/Dockerfile`
Script to build the spark base image `spark-base`

#### Steps

Update `config.json`, this is the spark config and get's packaged with the spark-submit code. Update the `spark.kubernetes.driver.container.image` and `spark.kubernetes.executor.container.image` values to point to the image tags (if changed, by default it points to the latest tag)

> Note: `config.json` is packaged with the docker image, we can update the submit code to use this `config.json` as a default or pickup a config.json from somewhere else (managed content maybe)

Builds & Pushes all the images and saves the types, action, skill definition and you can skip the below 5 steps upto save types:

        make deploy.all
        
Setup the skill in an agent and invoke or use `make tests` after updating payload.json in [tests](./tests)
        
        ```
        { 
            "uri": "s3a://dci-perf-managed-content-1e891c002ba4dacaca44/perf/CVS/150/stream_parquet/member_feedback_v16_1.parquet",
            "stream_read_dir": "s3a://dci-perf-managed-content-1e891c002ba4dacaca44/perf/CVS/150/stream_parquet", 
            "publicKey":"", 
            "secretKey":"",
            "s3Endpoint":"http://s3.amazonaws.com",
            "maxFilesPerTrigger": 1, 
            "pollInterval":60,
            "type" : "parquet",
            "storage_protocol": "s3a://",
            "project_id": "bptest",
            "source_name": "stream",
            "isTriggered": false,
            "primary_key": "member_id"
        }
        ```

### About

This example shows how to run a custom spark-submit job. In this case, we show how to run a stream-ingest using a python script that uses a bootstrap uri (`uri` in the input params) to infer the input data schema and upsert the data into a delta-table for a particular primary-key. The example assumes the stream_ingest job in this case is for parquet files and the trigger is set for a processingTime (runs forever with `pollinterval` seconds of polling interval).



[https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html](https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html) <br>
[https://docs.delta.io/latest/api/python/index.html](https://docs.delta.io/latest/api/python/index.html)

For more details about how to build skills go to [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/build-skills/define-skills)
