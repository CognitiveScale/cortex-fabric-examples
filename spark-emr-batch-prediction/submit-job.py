# this script needs to
"""
1. upload the diver script to s3 bucket
2. format the skill input in a way to properly submit the job
3. submit the job
"""
from botocore.exceptions import ClientError
import boto3
import json
import sys
import os


def get_runtime_args(config):
    pyspark_args = config["emr"]
    options = pyspark_args["options"]
    args = [pyspark_args["pyspark_bin"]]
    for key in options.keys():
        val = options[key]
        if isinstance(val, str):
            args.append(key)
            args.append(val)
        elif isinstance(val, dict):
            for y in val.keys():
                s_val = val[y]
                args.append(key)
                args.append("{}={}".format(y, s_val))
    return args


def setup_bucket(bucket_name, script_file_name, script_key, s3_resource):
    """
    Creates an Amazon S3 bucket and uploads the specified script file to it.

    :param bucket_name: The name of the bucket to create.
    :param script_file_name: The name of the script file to upload.
    :param script_key: The key of the script object in the Amazon S3 bucket.
    :param s3_resource: The Boto3 Amazon S3 resource object.
    :return: The newly created bucket.
    """
    try:
        bucket = s3_resource.create_bucket(
            Bucket=bucket_name
        )
        bucket.wait_until_exists()
        print("Created bucket %s.", bucket_name)
    except ClientError:
        print("Couldn't create bucket %s.", bucket_name)
        raise

    try:
        bucket.upload_file(script_file_name, script_key)
        print(
            "Uploaded script %s to %s.", script_file_name,
            f'{bucket_name}/{script_key}')
    except ClientError:
        print("Couldn't upload %s to %s.", script_file_name, bucket_name)
        raise

    return bucket


def add_step(cluster_id, emr_client, args, script_path, input_params):
    """
    Adds a job step to the specified cluster. This example adds a Spark
    step, which is run by the cluster as soon as it is added.

    :param cluster_id: The ID of the cluster.
    :param emr_client: The Boto3 EMR client object.
    :params args: List of conf arguments passed for spark submit-job.
    :param script_path: The URI where the Python script is stored.
    :param input_params: Extra params to be passed for driver script (in our case model-id etc)
    :return: The ID of the newly added step.
    """
    try:
        # flatten params, EMR behaves weird with nested dictionary 
        params = {**input_params.pop('properties'), **input_params}
        for k, v in input_params.items():
            if isinstance(v, dict):
                params.pop(k)

        response = emr_client.add_job_flow_steps(
            JobFlowId=cluster_id,
            Steps=[{
                'Name': "EMR-Example",
                'ActionOnFailure': 'CONTINUE',
                'HadoopJarStep': {
                    'Jar': 'command-runner.jar',
                    'Args': [
                        *args,
                        script_path,
                        json.dumps(params)
                    ]
                }
            }])
        step_id = response['StepIds'][0]
        print("Started step with ID %s", step_id)
    except ClientError:
        print("Couldn't start step %s with URI %s.",
              "EMR-Example", script_path)
        raise
    else:
        return step_id


if __name__ == '__main__':
    input_params = json.loads(sys.argv[1])
    os.environ['AWS_ACCESS_KEY_ID'] = input_params['properties'].pop('aws-access-id')
    os.environ['AWS_SECRET_ACCESS_KEY'] = input_params['properties'].pop('aws-secret')

    with open('config.json') as f:
        spark_config = json.load(f)
    
    input_params = {**spark_config['params'], **input_params}

    # Set up s3 resources for the skill.
    s3_resource = boto3.resource('s3')
    if input_params['properties'].startswith('s3://'):
        input_params['properties'] = input_params['properties'].strip("s3://")
    bucket_name = input_params['properties'].pop('s3-bucket')
    script_file_name = 'emr-container-image/src/job.py'
    script_key = f'scripts/{script_file_name}'
    setup_bucket(bucket_name, script_file_name, script_key, s3_resource)
    emr_client = boto3.client('emr',
                              region_name='us-east-1'
                              )

    args = get_runtime_args(spark_config)

    add_step(input_params['properties'].pop('cluster-id'), emr_client,
             args, f"s3://{bucket_name}/{script_key}", input_params)
