import pandas as pd
import requests
import logging

logger = logging.getLogger(__name__)
from common.helper import send_app_notification
from common.configs.config import Config
from common.configs.constants import Constant
import traceback
import datetime

config = Config()


def main(params):
    """
    Send notification to Virtual care nurse with given profile details
    :param params:
    :return:
    """
    profileId = params["payload"].get("profileId", "")
    try:
        # Fetches notification template
        notification_message = config.get_template(Constant.FLU_SHOT_CAMPAIGN_TYPE,
                                                   Constant.SEND_VIRTUAL_CARE_NURSE_NOTIFICATION)
        notification_message.format(profileId)
            # res = send_app_notification({"payload": data}, params) # API which we use to send notifications
        message = "Successfully sent given profile details to virtual care nurse"
    except Exception as e:
        message = 'Error while sending notifications'
        traceback.print_exc()
        raise e
    print(datetime.datetime.now())
    return {'message': message}


if __name__ == "__main__":
    import json
    import sys

    params = json.loads(sys.argv[1])
    logger.info(main(params))
