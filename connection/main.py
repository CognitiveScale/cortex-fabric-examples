from fastapi import FastAPI
from logzero import logger
from pymongo import MongoClient
from cortex import Cortex

app = FastAPI()


@app.post('/invoke')
def run(req: dict):
    payload = req['payload']
    try:
        client = Cortex.client(api_endpoint=req["apiEndpoint"], project=req["projectId"], token=req["token"])
        connection_name = payload["connection_name"]
        connection = client.get_connection(connection_name)
        params = dict(map(lambda l: (l['name'], l['value']), connection['params']))
        mongo = MongoClient(params["uri"])

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
