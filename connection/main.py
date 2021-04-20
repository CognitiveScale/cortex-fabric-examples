from fastapi import FastAPI
from logzero import logger
from pymongo import MongoClient
from cortex import Cortex

app = FastAPI()

connection_pool = {}


@app.post('/invoke')
def run(req: dict):
    payload = req['payload']
    try:
        client = Cortex.client(api_endpoint=req["apiEndpoint"], project=req["projectId"], token=req["token"])
        # init - get or create connection
        connection_name = payload["connection_name"]
        mongo = connection_pool.get(connection_name)
        if not mongo:
            connection = client.get_connection(connection_name)
            params = connection['params']
            mongo = MongoClient(params["uri"])
            connection_pool[connection_name] = mongo

        # use connection
        database = params.get("database")
        collection = params.get("collection")
        query = payload.get("query")

        if database and collection:
            result = list(mongo[database][collection].find(query))
        else:
            result = {
                "error": "collection, database and query must be provided"
            }
    except Exception as e:
        result = {
            "error": str(e)
        }
        logger.exception(e)
    return {'payload': result}
