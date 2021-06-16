import sys
import os
import json
import subprocess
import logging
from cortex import Cortex
from cortex.run import Run


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
    experiment_name = input_params["properties"]["experiment-name"]
    run_id = input_params["properties"]["run-id"]
    client = Cortex.client(api_endpoint=url, token=token, project=project)
    experiment = client.experiment(experiment_name)
    run = Run.from_json(experiment.get_run(run_id), experiment)
    spark_config = run.get_param('config')
    logging.info("Spark Config: {}".format(str(spark_config)))
    run_args = get_runtime_args(spark_config)
    run_args.append("src/main/python/main.py")
    run_args.append(json.dumps(input_params))
    subprocess.call(run_args)
