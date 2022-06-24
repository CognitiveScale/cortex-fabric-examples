"""
Copyright (c) 2022. Cognitive Scale Inc. All rights reserved.

"""
import os
import subprocess
import yaml
import sys
import json
import io
import traceback
from cortex.content import ManagedContentClient


def get_runtime_args(config, driver_spec_loc, token):
    pyspark_args = config['pyspark']
    options = pyspark_args['options']
    args = [os.path.join(os.environ['SPARK_HOME'], pyspark_args['pyspark_bin'])]
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


def download_from_managed_content(managed_content_client, key, project):
    try:
        return managed_content_client.download(
            key, retries=3, project=project)
    except Exception as e:
        traceback.print_exc()
        raise Exception(f"Failed to fetch {key} from managed content")


def get_config_from_mc(key, payload):
    managed_content_client = ManagedContentClient(payload['apiEndpoint'], token=os.environ['CORTEX_TOKEN'])
    content = download_from_managed_content(managed_content_client, key, payload['projectId']).read()
    return json.load(io.BytesIO(content))
    

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
        payload = json.loads(os.environ['CORTEX_PAYLOAD'])
        input_params = payload['payload']
        n = len(sys.argv)
        # TODO throw error if wrong amount of args
        config_file_loc = sys.argv[1]
        driver_template_loc = sys.argv[2]

        # get resource files from filesystem
        if not input_params.get("config"):
            spark_config = get_config_file(config_file_loc)
        else:
            spark_config = get_config_from_mc(key=input_params.get("config"), payload=payload)
        
        driver_template = get_driver_template(driver_template_loc)

        # variable replace and write new driver podspec
        # TODO better job of generalizing
        driver_variables = {'APP_COMMAND': str(spark_config['pyspark']['app_command'])}
        driver_spec = replace_template_variables(driver_template, driver_variables)
        driver_spec_loc = './driver.yaml'
        write_driver(driver_spec_loc, driver_spec)

        # create spark-submit call
        run_args = get_runtime_args(spark_config, driver_spec_loc, token)
        
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
