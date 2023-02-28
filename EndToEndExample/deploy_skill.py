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

from config import PROJECT_ID, API_ENDPOINT, CORTEX_TOKEN, CONN_PARAMS

from cortex import Cortex


params = {
    "projectId": PROJECT_ID,
    "apiEndpoint": API_ENDPOINT,
    "token": CORTEX_TOKEN
}

if __name__ == "__main__":
    client = Cortex.client()
    conn_params = {}
    with open("conn.json") as f:
        conn_params = json.load(f)

    conn_params["params"] = []
    for name, value in CONN_PARAMS.items():
        conn_params["params"].append({"name": name, "value": value})
    
    # create a secret called awssecretadmin in your project which contains the aws secret key

    # create a connection
    client.connections.save_connection(connection=conn_params)

    # ### run the following command
    # ``make build`` to build the docker images for train(job) and predict(daemon) action 
    # ``make push`` to push the images to docker registry set by the env variable DOCKER_PREGISTRY_URL

    # Deploying the Skills and actions

    skill_object = {}
    with open("skill.json") as f:
        skill_object = json.load(f)

    client.skills.save_skill(skill_object)

    # we can see the skill with name e2e-example has been deployed

    sks = client.skills.get_skill(skill_object['name'])

    pprint(sks)
