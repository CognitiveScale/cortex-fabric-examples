"""
Copyright (c) 2022. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Template/Example Code [License](https://github.com/CognitiveScale/cortex-code-templates/blob/main/LICENSE.md)
"""

import datetime
import json
import logging
import os
import sys
import time
import traceback

from cortex.utils import get_logger, log_message
from gql import Client, gql
from gql.transport.aiohttp import AIOHTTPTransport


def execute_query(query, client):
    try:
        """
        Execute GQL Query
        :param query: GQL query
        :param client: GQL Client
        :return: Query response
        """
        query = gql(query)
        return client.execute(query)
    except Exception as e:
        log_message(msg=f"Error in execute_query {str(e)}", log=get_logger(
            'feedback-aggregator'), level=logging.FATAL)
        traceback.print_exc()
        sys.exit(1)


def get_gql_client(endpoint, token):
    try:
        """
        Send GQL Request using GQL client
        :param endpoint: GQL Endpoint
        :param token: JWT
        :return: GQL Client
        """
        try:
            if token is not None:
                headers = {"Authorization": "Bearer " + token}
                client = Client(transport=AIOHTTPTransport(url=endpoint, headers=headers),
                                fetch_schema_from_transport=True,
                                execute_timeout=300000)
            else:
                client = Client(transport=AIOHTTPTransport(url=endpoint), fetch_schema_from_transport=True,
                                execute_timeout=300000)
            return client
        except Exception as e:
            raise Exception("Failed to get a connection to GQL client: {}", e)
    except Exception as e:
        log_message(msg=f"Error in get_gql_client {str(e)}", log=get_logger(
            'feedback-aggregator'), level=logging.FATAL)
        traceback.print_exc()
        sys.exit(1)


def get_connections(projectId, gql_client, connection_name):
    try:
        query = ["""
        {
            connections(project : "%s")
            {
                name
            }
        }
        """ % (projectId)]
        connections_list = execute_query(query[0], gql_client)['connections']
        for each in connections_list:
            if each["name"] == connection_name:
                return True
        return False

    except Exception as e:
        log_message(msg=f"Error in get_connections {str(e)}", log=get_logger(
            'feedback-aggregator'), level=logging.FATAL)
        traceback.print_exc()
        sys.exit(1)


def create_connections(projectId, bootstrap_uri, stream_read_dir, gql_client, connection_name, bucket_name, bucket_protocol, connection_type, bucket_api_endpoint):
    try:
        print("Bucket name: "+str(bucket_name))
        conn_uri = f"{bucket_protocol}{bucket_name}//{projectId}//{bootstrap_uri}"
        stream_read_dir = f"{bucket_protocol}{bucket_name}//{projectId}//{stream_read_dir}"
        query = ["""
                    mutation{
                          createConnection(input:{
                            project:"%s"
                            allowRead:true
                            allowWrite:true
                            connectionType:%s
                            contentType:parquet
                            name:"%s"
                            title:"%s"
                            description:"%s"
                            params:[
                                    {
                                      name: "%s", 
                                      value: "http://managed"
                                    },
                                    {
                                      name: "uri",
                                      value: "%s"

                                    },
                                    {
                                      name: "stream_read_dir",
                                      value: "%s"
                                    },
                                    {
                                        name: "pollInterval",
                                        value: "10"
                                    }
                                  ]
                            }
                          )
                          {
                            name
                          }
                        }
                """ % (projectId, connection_type, connection_name, connection_name, "", bucket_api_endpoint, conn_uri, stream_read_dir)]
        execute_query(query[0], gql_client)
    except Exception as e:
        log_message(msg=f"Error in create_connections {str(e)}", log=get_logger(
            'feedback-aggregator'), level=logging.ERROR)
        traceback.print_exc()
        sys.exit(1)


def create_datasource(gql_client, data_source, projectId, conn_name):
    try:
        query = [
            """
            mutation {
                  createDataSource(input:{
                    name:"%s",
                    title:"%s",
                    project:"%s",
                    kind:streaming,
                    connection:{
                      name:"%s"
                    },
                    attributes:[
                    "profileId",
                    "prediction"
                    ],
                    primaryKey:"profileId"}){
                    name
                    __typename
                  }
            }  
            """ % (data_source, data_source, projectId, conn_name)]
        execute_query(query[0], gql_client)
    except Exception as e:
        log_message(msg=f"Error in create_datasource {str(e)}", log=get_logger(
            'feedback-aggregator'), level=logging.ERROR)
        traceback.print_exc()
        sys.exit(1)


def ingest_datasource(projectId, data_source, gql_client):
    try:
        query = ["""
            mutation {
              ingestSource(project:"%s", source:"%s"){
                jobId
              }
            }
        """ % (projectId, data_source)]
        execute_query(query[0], gql_client)
    except Exception as e:
        log_message(msg=f"Error in ingest_datasource {str(e)}", log=get_logger(
            'feedback-aggregator'), level=logging.ERROR)
        traceback.print_exc()
        sys.exit(1)


def get_datasource_status(projectId, data_source, gql_client):
    try:
        query = ["""
        {
            dataStreamStatus(project: "%s", resourceType:DATA_SOURCE, resourceName:"%s") {
                lastUpdated
                lastProcessedSource
                status
              }
        }
        """ % (projectId, data_source)]
        results = execute_query(query[0], gql_client)["dataStreamStatus"]
        lastProcessedSource = results["lastProcessedSource"]
        print("lastProcessedSource: "+str(lastProcessedSource))
        status = results["status"]
        if status == "AWAITING":
            return True
        lastUpdated = datetime.datetime.strptime(
            results["lastUpdated"], '%Y-%m-%dT%H:%M:%S.%fZ')
        currentime = datetime.datetime.now().utcnow()
        difference = ((currentime - lastUpdated).seconds)/60
        if int(difference) > 10:
            return False
        else:
            time.sleep(20)
            print("Waiting for Datasource to process")
            return get_datasource_status(projectId, data_source, gql_client)

    except Exception as e:
        log_message(msg=f"Error in get_datasource_status {str(e)}", log=get_logger(
            'feedback-aggregator'), level=logging.ERROR)
        traceback.print_exc()
        sys.exit(1)


def get_profile_schema(gql_client, profile_schema, projectId):
    try:
        query = f"""
                {{
                    profileSchemaByName(
                        project: "{projectId}"
                        name: "{profile_schema}"
                    ){{
                        primarySource {{
                            attributes
                            profileKey
                            name
                        }}
                     }}
                }}
                """ 
        primary_source = execute_query(query, gql_client)[
            "profileSchemaByName"]["primarySource"]
        return [feature for feature in primary_source["attributes"]], primary_source['name'], primary_source["profileKey"]
    except Exception as e:
        log_message(msg=f"Error in get_datasources {str(e)}", log=get_logger(
            'feedback-aggregator'), level=logging.ERROR)
        traceback.print_exc()
        sys.exit(1)


def get_datasources(projectId, mission_name, gql_client):
    try:
        query = ["""
                {
                    dataSources(project : "%s") {
                        name
                      }
                }
                """ % (projectId)]
        datasources_list = execute_query(query[0], gql_client)["dataSources"]
        for each in datasources_list:
            if each["name"] == mission_name:
                return True
        return False
    except Exception as e:
        log_message(msg=f"Error in get_datasources {str(e)}", log=get_logger(
            'feedback-aggregator'), level=logging.ERROR)
        traceback.print_exc()
        sys.exit(1)


def update_profile_schema(gql_client, projectId, profile_schema, data_source, feature_list, primary_source_name, primary_key):
    try:
        query = f"""mutation {{
              updateProfileSchema(input: {{
                  project: "{projectId}",
                  name: "{profile_schema}",
                  names: {{
                    categories: [],
                    title: "{profile_schema}",
                    plural: "",
                    singular: ""
                  }},
                  title: "{profile_schema}",
                  joins: [
                        {{
                            name: "{data_source}",
                            join: {{
                                primarySourceColumn: "{primary_key}",
                                joinSourceColumn: "profileId"
                            }},
                            attributes: [
                                "prediction"
                            ],
                            profileGroup: "predictions",
                            timestamp: {{
                                auto: true
                            }}
                        }}
                    ],
                  primarySource: {{
                    name: "{primary_source_name}",
                    profileKey: "{primary_key}",
                    profileGroup: "default",
                    timestamp: {{
                      auto: true
                    }},
                    attributes: [
                      {','.join([f'"{s}"' for s in feature_list])}
                    ]
                  }}
               }}){{
                    __typename
                }}
            }}"""
        execute_query(query, gql_client)

    except Exception as e:
        log_message(msg=f"Error in create_profiles {str(e)}", log=get_logger(
            'feedback-aggregator'), level=logging.ERROR)
        traceback.print_exc()
        sys.exit(1)


def buildProfile(projectId, profile_schema, gql_client):
    try:
        query = ["""
                mutation {
                    buildProfile(
                        project:"%s",
                        profileSchema:"%s"
                      )
                      {
                        jobId,isActive,isComplete, isError
                      }
                }
                """ % (projectId, profile_schema)]
        profiles = execute_query(query[0], gql_client)
        jobId = profiles['buildProfile']['jobId']
        return jobId
    except Exception as e:
        log_message(msg=f"Error in buildProfile {str(e)}", log=get_logger(
            'feedback-aggregator'), level=logging.ERROR)
        traceback.print_exc()
        sys.exit(1)


def getJobResources(projectId, resourceName, resourceType, gql_client, invoked_time):
    """
    Checks if a job for build-profiles on the given profile is already running.
    If it is already running, code will pause for 30 seconds and try again. We wait for the job to complete for a maximum of 120 seconds.
    """
    try:
        query = ["""
            {
          jobsForResource(project: "%s",resourceType: "%s",resourceName: "%s"){
            startTime,endTime,isActive,isComplete, isError,jobId,jobType
          }
        }
        """ % (projectId, resourceType, resourceName)]
        results = execute_query(query[0], gql_client)["jobsForResource"]
        if results:
            try:
                first_starttime = datetime.datetime.strptime(
                    results[0]["startTime"], '%Y-%m-%dT%H:%M:%SZ')
            except TypeError:
                # jobsForResource did not return a correct response
                log_message(msg=f"jobsForResource did not return a correct response {str(results)}", log=get_logger(
                    'feedback-aggregator'), level=logging.ERROR)
                return False
            latest_index = 0
        else:
            return True
        if len(results) > 1:
            for each in range(1, len(results)):
                try:
                    current_timestamp = datetime.datetime.strptime(
                        results[each]["startTime"], '%Y-%m-%dT%H:%M:%SZ')
                except TypeError:
                    log_message(msg=f"jobsForResource did not return a correct response {str(results)}", log=get_logger(
                        'feedback-aggregator'), level=logging.ERROR)
                    return False
                if current_timestamp > first_starttime:
                    latest_index = each
                    first_starttime = current_timestamp
        results = results[latest_index]
        log_message(msg="Latest build-profile results: "+str(results),
                    log=get_logger('feedback-aggregator'), level=logging.INFO)
        if results["isComplete"] == False:
            if results["isComplete"] == False and results["isError"] == True:
                return False
            try:
                startTime = datetime.datetime.strptime(
                    results["startTime"], '%Y-%m-%dT%H:%M:%SZ')
            except TypeError:
                log_message(msg=f"jobsForResource did not return a correct response {str(results)}", log=get_logger(
                    'feedback-aggregator'), level=logging.ERROR)
                return False
            currentime = datetime.datetime.now().utcnow()
            difference = ((currentime - startTime).seconds) / 60
            if int(difference) > 10:
                return False
            else:
                time.sleep(20)
                return getJobResources(projectId, resourceName, resourceType, gql_client, invoked_time)
        else:
            return True

    except Exception as e:
        print("Error in getJobResources"+str(e))
        raise


def run(params):
    """
    Function to uses the skill params to extract profiles and dump to Managed Content

    @param payload: dictionary containing
        @param profileScehma: PrfoileSchema to update the predictions to
        @param dataSource: datasource for the predicted data
        @param bootstrap_uri: Bootstrap URI for the streaming connection
        @param stream_read_dir: Directory to stream data from
    @param token: Cortex token
    @param apiEndpoint: Cortex apiendpoint
    @param projectId: Cortex projectId
    @return: True/False
    
    """
    try:
        token = params["token"]
        apiendpoint = params["apiEndpoint"]
        projectId = params["projectId"]
        payload = params["payload"]
        profile_schema = payload["profileSchema"]
        connection_name = profile_schema+"pred"
        data_source = profile_schema+"pred"
       

        properties = params["properties"]
        bucket_name = str(properties["bucket_name"])
        bucket_protocol = str(properties["bucket_protocol"])
        connection_type = str(properties["connection_type"])
        bucket_api_endpoint = str(properties["bucket_api_endpoint"])

        gql_endpoint = apiendpoint + "/fabric/v4/graphql"
        gql_client = get_gql_client(gql_endpoint, token)
        bootstrap_uri = profile_schema + "_predictions/pred.parquet" 
        stream_read_dir = profile_schema + "_predictions"

        if (get_connections(projectId, gql_client, connection_name) != True):
            log_message(msg="Connection does not exist", log=get_logger("feedback-aggregator"), level=logging.INFO)
            create_connections(projectId, bootstrap_uri, stream_read_dir, gql_client, connection_name,
                               bucket_name, bucket_protocol, connection_type, bucket_api_endpoint)

        if (get_datasources(projectId, data_source, gql_client) != True):
            log_message(msg="Datasource does not exist", log=get_logger("feedback-aggregator"), level=logging.INFO)
            create_datasource(gql_client, data_source, projectId, connection_name)
            feature_list, primary_source_name, primary_key = get_profile_schema(gql_client, profile_schema, projectId)
            update_profile_schema(gql_client, projectId, profile_schema, data_source, feature_list, primary_source_name, primary_key)
        
        ingest_datasource(projectId, data_source, gql_client)

        if (get_datasource_status(projectId, data_source, gql_client)):
            invoked_time = datetime.datetime.utcnow()
            if (getJobResources(projectId, profile_schema, "ProfileSchema", gql_client, invoked_time)):
                buildProfile(projectId, profile_schema, gql_client)
                log_message(msg="Profile successfully built", log=get_logger("feedback-aggregator"), level=logging.INFO)
    except Exception as e:
        log_message(msg=f"{str(e)}", log=get_logger(
            'feedback-aggregator'), level=logging.ERROR)
        traceback.print_exc()

def extract_json_objects(text):
    """
    Find JSON objects in text, and yield the decoded JSON data

    Does not attempt to look for JSON arrays, text, or other JSON types outside
    of a parent JSON object.
    """
    MATCH = 'Received: {'
    match_index = text.find(MATCH)
    new_line = text.find('\n', match_index)
    result = eval(text[match_index + len(MATCH)-1:new_line])
    return result


if __name__ == '__main__':
    import sys
    params = sys.argv[1]
    params = json.loads(params)
    # params = {
    #     "token": "eyJhbGciOiJFZERTQSIsImtpZCI6Im5WalJOdWhPQzc5ZFpPYVMwaGt4U09Bek14Zm1mTWl0SUpLY05fdWQwTGcifQ.eyJzdWIiOiIyNmU5NmU1OC1kYjhjLTQ5NWQtODI3OS1jMjQ1YzNlMjMzMGUiLCJhdWQiOiJjb3J0ZXgiLCJpc3MiOiJjb2duaXRpdmVzY2FsZS5jb20iLCJpYXQiOjE2Njk4Mzc5NzMsImV4cCI6MTY2OTkyNDM3M30.bhwsb7CBncNvx77_QxqTp5YXKQKFB3z4cWh64HQpxrDjMxUNv7nlfXTh9w6wlvVrQPrt97mmsvhOxfWySDr7Cg",
    #     "apiEndpoint": "https://api.dci-dev.dev-eks.insights.ai",
    #     "projectId": "bptest",
    #     "payload": {
    #         "profileSchema": "german-credit-0c3d3",
    #     },
    #     "properties": {
    #         "bucket_name": "cortex-content",
    #         "bucket_protocol": "s3a://",
    #         "connection_type": "s3FileStream",
    #         "bucket_api_endpoint": "s3Endpoint"
    #     }
    # }
    print(f'Parsing Payload : --')
    params["payload"] = extract_json_objects(params["payload"])
    print(f'Received: {params["payload"]}')
    run(params)