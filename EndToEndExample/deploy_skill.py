#!/usr/bin/env python
# coding: utf-8

"""
## steps
1. S3 bucket contains the data(in this case csv file) 
2. We save a connection of the given types -> (demonstrated using cortex-python sdk)
5. Dockerize and push an action to train multiple models and save in expermients (use the connection in step 2 to load the data) -> job
3. Dockerize and push another action to predict using the exprinment name(to specify a particular model) -> daemon
4. Save a skill definition, and route inputs to two different actions -> (demonstrated using cortex-python sdk)
5. Test the skills using cli
"""

# ### Run the following command
# ```make check-env``` to check if all neccassary environment variables are set


import time
import sys
import json
from pprint import pprint

from cortex import Cortex
from cortex.model import Model, ModelClient
from cortex.experiment import Experiment, ExperimentClient
from cortex.connection import ConnectionClient, Connection
from cortex.skill import SkillClient


# Global Configs
PROJECT_ID = ""
API_ENDPOINT = ""
CORTEX_TOKEN = ""

AWS_PUBLIC_KEY = ""
S3_BUCKET = "cortex-fabric-examples"
FILE_NAME = "german_credit_eval.csv"
URI = f"s3a://{S3_BUCKET}/{FILE_NAME}"
S3_ENDPOINT = "http://s3.us-east-1.amazonaws.com"
# use `cortex configure token` to get token


params = {
    "projectId": PROJECT_ID,
    "apiEndpoint": API_ENDPOINT,
    "token": CORTEX_TOKEN
}

if __name__ == "__main__":
    client = Cortex.client(
        api_endpoint=params['apiEndpoint'], project=params['projectId'], token=params['token'])
    cc = ConnectionClient(client)

    conn_params = {
        "name": "exp-connection",
        "title": "S3 Conn for fabric examples - German Credit Data",
        "description": "S3 Conn",
        "connectionType": "s3",
        "allowWrite": False,
        "allowRead": True,
        "contentType": "csv",
        "params": [
            {"name": "s3Endpoint", "value":  S3_ENDPOINT},
            {"name": "bucket", "value": S3_BUCKET},
            {"name": "publicKey", "value": AWS_PUBLIC_KEY},
            {"name": "secretKey", "value": "#SECURE.awssecretadmin"},
            {"name": "uri", "value": URI}
        ]
    }
    # create a secret called awssecretadmin in your project which contains the aws secret key

    # create a connection
    cc.save_connection(project=PROJECT_ID, connection=conn_params)

    # ### run the following command
    # ``make build`` to build the docker images for train(job) and predict(daemon) action 
    # ``make push`` to push the images to docker registry set by the env variable DOCKER_PREGISTRY_URL

    # Deploying the Skills and actions

    skill_object = {}
    with open("skill.json") as f:
        skill_object = json.load(f)

    skill_client = SkillClient(client)

    skill_client.save_skill(skill_object)

    # we can see the skill with name e2e-example has been deployed

    sks = skill_client.get_skill(skill_object['name'])

    pprint(sks)
