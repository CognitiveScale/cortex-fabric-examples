import requests
import uuid
import datetime
import pandas as pd
import logging as log
from common.configs.config import Config
import time

config = Config()


def timeit(method):
    def timed(*args, **kw):
        ts = time.time()
        result = method(*args, **kw)
        te = time.time()
        print(method.__name__, ': %2.2f s' % (te - ts))
        return result

    return timed


@timeit
def send_app_notification(data, params):
    print("notification service.....")
    headers = {'Content-type': 'application/json', 'Authorization': 'Bearer '+ params["token"]}
    r = requests.post(url=params["properties"]
                      ["notification_api"], json=data, headers=headers)
    res = r.text
    return res


@timeit
def save_notifications(data, conn):
    # data should have following these columns
    # ['member_id', 'channel_type', 'message', 'cohort_id', 'intervention_id', 'mission_id', 'project_id']
    data["notification_time"] = [str(datetime.datetime.now()) for i in range(len(data))]
    data["notification_id"] = [str(uuid.uuid4()) for i in range(len(data))]
    data.to_sql("notification", con=conn, index=False, if_exists="append")
    return None


