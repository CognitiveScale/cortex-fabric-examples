"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.

"""
import os
import subprocess
import yaml
import sys
import json


def get_runtime_args(config, driver_spec_loc, token):
    pyspark_args = config['pyspark']
    options = pyspark_args['options']
    # os.environ['SPARK_HOME'] = "/Users/bpandey/projects/cortex6/cortex-fabric-examples/spark-stream-ingest/submit/submit/opt/spark"
    # os.environ['HADOOP_HOME'] 
    args = [os.environ['SPARK_HOME'] + "/" + pyspark_args['pyspark_bin']]
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
    args.append('--conf')
    args.append(f"spark.kubernetes.driverEnv.CORTEX_TOKEN={token}")
    args.append('--conf')
    args.append(f"spark.kubernetes.driver.podTemplateFile={driver_spec_loc}")
    args.append(pyspark_args['app_location'])
    return args


def replace_template_variables(template, variables):
    for key in variables.keys():
        template = template.replace(f"${{{key}}}", variables[key])
    return template


def get_config_file(config_file_loc):
    with open(config_file_loc) as json_file:
        return json.load(json_file)


def get_driver_template(driver_file_loc):
    with open(driver_file_loc) as yaml_file:
        return yaml_file.read()


def write_driver(driver_spec_loc, driver_spec):
    with open(driver_spec_loc, 'w') as file:
        file.write(driver_spec)


class LogMessage:
    def __init__(self):
        self.log_header = ''
        self.pod_info = {}
        self.containers = {}

    @staticmethod
    def get_or_empty_string(dict, key):
        if key in dict:
            return dict[key]
        else:
            return ''

    @staticmethod
    def create_from_log_lines(log_parts):
        instance = LogMessage()
        pod_info_lines = []
        container_info_lines = []

        for log_part in log_parts:
            if instance.log_header == '':
                instance.log_header = log_part.strip()
            if log_part.startswith('\t\t'):
                container_info_lines.append(log_part)
            elif log_part.startswith('\t'):
                pod_info_lines.append(log_part)

        for pod_info_line in pod_info_lines:
            pod_info_parts = pod_info_line.strip().split(':', 1)
            if len(pod_info_parts) == 2 and pod_info_parts[1] != '':
                instance.pod_info[pod_info_parts[0].strip()] = pod_info_parts[1].strip()

        current_container = ''

        for container_line in container_info_lines:
            container_parts = container_line.split(':', 1)
            if len(container_parts) == 2:
                if 'container name' in container_parts[0]:
                    current_container = container_parts[1].strip()
                    instance.containers[current_container] = {}
                else:
                    instance.containers[current_container][container_parts[0].strip()] = container_parts[1].strip()
            elif container_parts[0] == '':
                current_container = ''
        return instance


if __name__ == '__main__':
    try:
        # pool values from args
        token = os.environ['CORTEX_TOKEN']
        # token = "eyJhbGciOiJFZERTQSIsImtpZCI6Im5WalJOdWhPQzc5ZFpPYVMwaGt4U09Bek14Zm1mTWl0SUpLY05fdWQwTGcifQ.eyJzdWIiOiIyNmU5NmU1OC1kYjhjLTQ5NWQtODI3OS1jMjQ1YzNlMjMzMGUiLCJhdWQiOiJjb3J0ZXgiLCJpc3MiOiJjb2duaXRpdmVzY2FsZS5jb20iLCJpYXQiOjE2NTMxMzc5MTIsImV4cCI6MTY1MzIyNDMxMn0.yFz2_t4iYWSZpMsOqjvwA9QOkNyIUaVL7fBiERnxfxTyiXMuDLnsPJN4laoFdfnFpqItUmtEEwJlO1AZJJ6ODA"
        n = len(sys.argv)
        # TODO throw error if wrong amount of args
        config_file_loc = sys.argv[1]
        driver_template_loc = sys.argv[2]

        # get resource files from filesystem
        spark_config = get_config_file(config_file_loc)
        driver_template = get_driver_template(driver_template_loc)

        # variable replace and write new driver podspec
        # TODO better job of generalizing
        driver_variables = {'APP_COMMAND': str(spark_config['pyspark']['app_command'])}
        driver_spec = replace_template_variables(driver_template, driver_variables)
        driver_spec_loc = './driver.yaml'
        write_driver(driver_spec_loc, driver_spec)

        # create spark-submit call
        run_args = get_runtime_args(spark_config, driver_spec_loc, token)
        input_params = { 
            "uri": "s3a://dci-perf-managed-content-1e891c002ba4dacaca44/perf/CVS/150/stream_parquet/member_feedback_v16_1.parquet",
            "stream_read_dir": "s3a://dci-perf-managed-content-1e891c002ba4dacaca44/perf/CVS/150/stream_parquet", 
            "publicKey":"AKIAWPZU5FVI7VPBAMHV", 
            "secretKey":"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",
            "s3Endpoint":"http://s3.amazonaws.com",
            "maxFilesPerTrigger": 1, 
            "pollInterval":60,
            "type" : 'parquet',
            "storage_protocol": 's3a://',
            "project_id": "bptest",
            "source_name": "stream",
            "isTriggered": False,
            "primary_key": "member_id"
        }
        run_args.append(json.dumps(input_params))

        cmd = subprocess.Popen(run_args, stderr=subprocess.PIPE, text=True)
        print(run_args)
        pod = ''
        container_name = 'fabric-action'
        container_state = ''
        exit_code = '0'
        termination_reason = ''
        log_lines = []

        for line in iter(cmd.stderr.readline, ''):
            if 'LoggingPodStatusWatcherImpl' in line:
                log_message = LogMessage.create_from_log_lines(log_lines)
                if pod == '':
                    pod = LogMessage.get_or_empty_string(log_message.pod_info, 'pod name')
                if container_name in log_message.containers:
                    container_state = LogMessage.get_or_empty_string(log_message.containers[container_name], 'container state')
                    exit_code = LogMessage.get_or_empty_string(log_message.containers[container_name], 'exit code')
                    termination_reason = LogMessage.get_or_empty_string(log_message.containers[container_name], 'termination reason')
                log_lines.clear()
            log_lines.append(line)
            print(line.rstrip())

        print('Pod Name:', pod)
        print('Container State:', container_state)
        print('Termination Reason:', termination_reason)
        print('Exit Code:', exit_code)
        if exit_code != '0':
            sys.exit(1)
    except Exception as exc:
        raise
        print(exc, file=sys.stderr)
        sys.exit(1)
