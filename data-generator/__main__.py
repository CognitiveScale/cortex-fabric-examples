import json
import sys
#from cortex import Cortex,Message
from ds_discovery import Controller
import os
import warnings
import requests

warnings.simplefilter(action='ignore', category=FutureWarning)
warnings.simplefilter(action='ignore', category=DeprecationWarning)

__author__ = 'Darryl Oatridge'


def domain_controller():
    try:
        job_data = json.loads(os.environ["CORTEX_PAYLOAD"])
        
        payload = job_data["payload"]
        properties = job_data["properties"]
        aws_access_key_id = properties["awspublickey"]
        aws_secret_access_key = properties["awssecretkey"]
        n_fold = payload['n_fold']

        for key in os.environ.keys():
            if key.startswith('HADRON'):
                del os.environ[key]
        if type(payload)==str:
            payload = payload.replace("'",'"')
            payload = json.loads(payload)
        if "response" not in payload.keys():
            uri_pm_repo = payload['domain_contract_repo']
            hadron_kwargs = payload['hadron_kwargs']
        else:
            uri_pm_repo = payload["response"]['domain_contract_repo']
            hadron_kwargs = payload["response"]['hadron_kwargs']

        if not isinstance(uri_pm_repo, str):
            raise KeyError("The message parameters passed do not have the mandatory 'domain_contract_repo' payload key")

        for key in hadron_kwargs.copy().keys():
            if str(key).isupper():
                os.environ[key] = hadron_kwargs.pop(key)


        os.environ["AWS_SECRET_ACCESS_KEY"] = aws_secret_access_key
        os.environ["AWS_ACCESS_KEY_ID"] = aws_access_key_id

        controller = Controller.from_env(uri_pm_repo=uri_pm_repo, default_save=False, has_contract=True)
        controller.remove_run_book()
        roadmap = [
            controller.runbook2dict(task='sor_sim',source=100000*n_fold, persist=True),
            controller.runbook2dict(task='members_gen', source='@', persist=True),
            controller.runbook2dict(task='flu_risk_members_gen', source='@', persist=True),
            controller.runbook2dict(task='feedback_gen', source='@', persist=True),
        ]
        controller.run_controller(run_book=roadmap)

    except Exception as e:
        print(e)
        raise


if __name__ == '__main__':
    """
    if len(sys.argv) < 2:
        print("Message/payload commandline is required")
        exit(1)
    domain_controller(json.loads(sys.argv[-1]))
    """
    domain_controller()