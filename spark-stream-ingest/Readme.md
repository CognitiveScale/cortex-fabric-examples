### Spark Stream Ingest Job (Skill)

Cortex skill does a spark stream ingest

### Prerequisites

- Python 3.x
- Docker client
- Cortex CLI 
- URL/Credentials for a cortex instance

### Project Structure

- Job source code location `src/main/python/main.py`
- Job submit code location `submit_job.py`
- spark default config location `config.json`
- Dockerfile for skill and the driver/executor image `Dockerfile`


### About

This example shows how to run a custom spark-submit job. In this case, we show how to run a stream-ingest using a python script that uses a bootstrap uri (`uri` in the input params) to infer the input data schema and upsert the data into a delta-table for a particular primary-key. The example assumes the stream_ingest job in this case, is for parquet files, and the trigger is set to `triggerOnce=Tue` (everytime the job is invoked, it ingests the available files in then shutsdown).

The source is pointed to by the input params of the skill, `stream_read_dir` and `uri` (bootstrap uri). We assume this is an s3 bucket in this case the creds for which are passed in the input_params as `secretKey` and `publicKey`. The example writes to an S3 bucket creds for which are passed to config.json `spark.kubernetes.driverEnv.AWS_SECRET_KEY`.


#### Steps

Update `config.json`, this is the spark config and get's packaged with the spark-submit code as a default config. Update the `spark.kubernetes.driver.container.image` and `spark.kubernetes.executor.container.image` values to point to the image tags (if changed, by default it points to the latest tag). Alternately we expose a `config` parameter where we can specify the managed content key to point to, for the config.

Builds & Pushes all the images and saves the types, action, skill definition and you can skip the below 5 steps upto save types:

        make all
        
Setup the skill in an agent and invoke or use `make tests` after updating payload.json in [tests](./tests)
        
        ```
        {
            "payload": { 
            "uri": "s3a://dci-perf-managed-content-1e891c002ba4dacaca44/perf/CVS/150/stream_parquet/member_feedback_v16_1.parquet",
            "stream_read_dir": "s3a://dci-perf-managed-content-1e891c002ba4dacaca44/perf/CVS/150/stream_parquet", 
            "publicKey": "", 
            "secretKey": "",
            "s3Endpoint": "http://s3.amazonaws.com",
            "maxFilesPerTrigger": 1, 
            "pollInterval": 60,
            "type" : "parquet",
            "storage_protocol": "s3a://",
            "project_id": "bptest",
            "source_name": "stream",
            "isTriggered": false,
            "primary_key": "member_id",
            "config": "config.json"
        }}
        ```



[https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html](https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html) <br>
[https://docs.delta.io/latest/api/python/index.html](https://docs.delta.io/latest/api/python/index.html)

For more details about how to build skills go to [Cortex Fabric Documentation - Development - Develop Skills](https://cognitivescale.github.io/cortex-fabric/docs/build-skills/define-skills)
