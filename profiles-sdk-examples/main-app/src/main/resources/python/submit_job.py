"""
Copyright (c) 2021. Cognitive Scale Inc. All rights reserved.

"""
import os
import subprocess
import yaml
import sys
import json

def get_runtime_args(config, token):
    pyspark_args = config['pyspark']
    options = pyspark_args['options']
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
    args.append(f"spark.cortex.phoenix.token={token}")
    args.append(pyspark_args['app_location'])
    for x in pyspark_args['app_command']:
        args.append(x)
    return args


def replace_template_variables(template, variables):
    for key in variables.keys():
        template = template.replace(f"${{{key}}}", variables[key])
    return template


def get_config_file(config_file_loc):
    with open(config_file_loc) as json_file:
        return json.load(json_file)

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

        payload = json.loads(sys.argv[1])
        input_params = payload['payload']

        n = len(sys.argv)
        # TODO throw error if wrong amount of args
        config_file_loc = input_params.get("config")
        # get resource files from filesystem
        spark_config = get_config_file(config_file_loc)

        # TODO: Verify app command is a list of strings
        override_app_command = input_params.get("app_command")
        if override_app_command:
            spark_config.get("pyspark", {})["app_command"] = override_app_command

        # TODO: Verify conf overrides is a dict
        config_option_overrides = input_params.get("conf")
        if config_option_overrides:
            spark_config.get("pyspark", {}).get("options", {}).get("--conf", {}).update(config_option_overrides)

        # create spark-submit call
        run_args = get_runtime_args(spark_config, token)

        print(run_args)

        cmd = subprocess.Popen(run_args, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        pod = ''
        container_name = 'spark-kubernetes-driver'
        container_state = ''
        exit_code = '0'
        termination_reason = ''
        log_lines = []

        for line in iter(cmd.stdout.readline, ''):
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
        print(exc, file=sys.stderr)
        sys.exit(1)
