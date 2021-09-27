import logging
import json
import flask

webserver_app = flask.Flask(__name__)

@webserver_app.route("/invoke", methods=["POST"])
def run():

    try:
        payload = flask.request.json['payload']
        profileId = payload["profileId"]
        intervention = payload["intervention_id"]
        campaign_name = payload["campaign"]
        mission_name = payload["mission"]
        #Add logic for action here
        message = "Action being invoked for Campaign "+str(campaign_name)+" and Mission: "+str(mission_name)+"\n"
        message += " Successfully invoked action for given profile ID "+str(profileId)+" for intervention "+str(intervention)
        return flask.Response(response=json.dumps({"payload":{"message":message}}), status=200, mimetype='application/json')

    except Exception as e:
        return flask.Response(response=json.dumps({"payload":{"message":"Exception occurred " + str(e)}}), status=500, mimetype='application/json')


if __name__ == '__main__':
    webserver_app.run(host='0.0.0.0', debug=True, port=5000, use_reloader=False)



