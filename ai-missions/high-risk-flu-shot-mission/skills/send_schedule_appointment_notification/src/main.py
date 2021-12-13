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
    For given profiles, prepare and send notifications
    :param params:
    :return: {}
    """
    profileId = params["payload"].get("profileId", "")
    try:
        # Fetches notification template
        notification_message = config.get_template(Constant.FLU_SHOT_CAMPAIGN_TYPE,
                                                   Constant.SEND_FLU_SHOT_SYDNEYCARE_APPOINTMENT_NOTIFICATION)
        notification_message.format(profileId)
        message = "Successfully sent notifications to given profile IDs"
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
