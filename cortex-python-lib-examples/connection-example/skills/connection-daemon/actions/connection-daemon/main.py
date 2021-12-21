"""
Copyright (c) 2020. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""

from fastapi import FastAPI
from logzero import logger
from pymongo import MongoClient
from cortex import Cortex

app = FastAPI()


@app.post('/invoke')
def run(request_body: dict):
    # Get agent/skill activation request body
    api_endpoint = request_body["apiEndpoint"]
    project = request_body["projectId"]
    token = request_body["token"]
    connection_name = request_body["payload"]["connection_name"]
    query = request_body["payload"]["query"]

    try:
        # Create Cortex client
        client = Cortex.client(api_endpoint=api_endpoint, project=project, token=token)

        # Get connection and create mongo client
        connection = client.get_connection(connection_name)
        params = dict(map(lambda l: (l['name'], l['value']), connection['params']))
        mongo = MongoClient(params["uri"])

        # Use connection
        database = params.get("database")
        collection = params.get("collection")

        if database and collection:
            result = list(mongo[database][collection].find(query))
        else:
            result = {"error": "collection, database and query must be provided"}
    except Exception as e:
        result = {"error": str(e)}
        logger.exception(e)

    # Return result
    return {'payload': result}
