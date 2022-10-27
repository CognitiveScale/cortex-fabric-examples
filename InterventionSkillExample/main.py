from fastapi import FastAPI, Response, status

app = FastAPI()


@app.post('/invoke', status_code=200)
def run(params:dict,response: Response):

    try:
        payload = params['payload']
        profileId = payload["profileId"]
        intervention = payload["intervention_id"]
        campaign_name = payload["campaign"]
        mission_name = payload["mission"]
        profile_schema = payload["profileSchema"]
        #Add logic for action here
        message = "Action being invoked for Campaign "+str(campaign_name)+" and Mission: "+str(mission_name)+"\n"
        message += " Successfully invoked action for given profile ID "+str(profileId)+" for intervention "+str(intervention)
        response.status_code = status.HTTP_200_OK
        return {"payload":{"message":message}}

    except Exception as e:
        response.status_code = status.HTTP_500_INTERNAL_SERVER_ERROR
        return {"payload": {"message":"Exception occurred " + str(e)}}




