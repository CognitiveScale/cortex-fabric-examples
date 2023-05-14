import json
import traceback
import os
from ds_discovery import Controller
import warnings

warnings.simplefilter(action='ignore', category=FutureWarning)
warnings.simplefilter(action='ignore', category=DeprecationWarning)


def main(params):
    try:
        os.environ['TOKEN'] = params["token"]
        os.environ['API_ENDPOINT'] = params["apiEndpoint"]
        os.environ['PROJECT'] = params["projectId"]
        payload = params["payload"]
        uri_pm_repo = payload["domain_contract_repo"]
        hadron_kwargs = payload['hadron_kwargs']

        # Reading up HADRON envs and seetting as os vars
        for key in hadron_kwargs.copy().keys():
            if str(key).isupper():
                os.environ[key] = hadron_kwargs.pop(key)

        controller = Controller.from_env(
            uri_pm_repo=uri_pm_repo, default_save=False, has_contract=True)

        controller.run_controller()

        print({"success": True})
    except:
        traceback.print_exc()
        print({"success": False})


if __name__ == '__main__':
    params = json.loads(os.environ["CORTEX_PAYLOAD"])
    # print(f'Received: {params}')
    main(params)
