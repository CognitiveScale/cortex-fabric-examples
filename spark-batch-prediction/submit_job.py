"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""
import sys
import os
import json
import subprocess
import logging
from sensa import Sensa
from sensa.utils import log_message, get_logger
from sensa.experiment import Experiment


def get_runtime_args(config):
    pyspark_args = config["pyspark"]
    options = pyspark_args["options"]
    args = [os.environ['SPARK_HOME'] + "/" + pyspark_args["pyspark_bin"]]
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


if __name__ == '__main__':
    input_params = json.loads(sys.argv[1])
    url = input_params["apiEndpoint"]
    token = input_params["token"]
    project = input_params["projectId"]
    skill_name = input_params["skillName"]
    experiment_name = input_params["properties"]["experiment-name"]
    run_id = input_params["properties"]["run-id"]
    client = Sensa.client(api_endpoint=url, token=token, project=project)
    result = client.experiments.get_experiment(experiment_name)
    experiment = Experiment(result, client.experiments)
    run = experiment.get_run(run_id)
    conn_params = {}
    connection = client.connections.get_connection(input_params["properties"]["connection-name"])
    for p in connection['params']:
        conn_params.update({p['name']: p['value']})
    spark_config = run.get_artifact('spark-config')
    log_message(msg=f"Spark Config: {str(spark_config)}", log=get_logger(skill_name), level=logging.INFO)
    run_args = get_runtime_args(spark_config)
    run_args.append("local:///opt/spark/work-dir/src/main/python/main.py")
    run_args.append(json.dumps(input_params))
    subprocess.call(run_args)
