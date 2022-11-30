"""
Copyright (c) 2022. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Template/Example Code [License](https://github.com/CognitiveScale/cortex-code-templates/blob/main/LICENSE.md)
"""
import json
from gql import Client, gql
from cortex import Cortex
from cortex.content import ManagedContentClient
from gql.transport.aiohttp import AIOHTTPTransport
import pandas as pd
import sys
import io


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
        raise Exception("\n Error in execute_query : {}".format(e))


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
        raise Exception("\n Error in get_gql_client : {}".format(e))


def get_profiles(gql_client, project, profile_schema, profiles_key, filter_cohort):
    try:
        if filter_cohort:
            query = f"""{{
                            profiles(project:"{project}",profileSchema:"{profile_schema}",filter:"{filter_cohort}",limit:0) {{
                            attributes {{
                                key
                                source
                                type
                                value
                                __typename
                                }}
                            profileID
                            }}
                        }}
                        """
        else:
            query = f"""{{
                            profiles(project:"{project}",profileSchema:"{profile_schema}",limit:0) {{
                            attributes {{
                                key
                                source
                                type
                                value
                                __typename
                                }}
                            profileID
                            }}
                        }}
                        """
        profiles = execute_query(query, gql_client)["profiles"]
        # figure out the columns
        profile_data = []
        for profile in profiles:
            profile_dict = {}
            profile_id = profile["profileID"]
            for each in profile["attributes"]:
                profile_dict["profileId"] = profile_id
                profile_dict[each["key"]] = each["value"]
            profile_data.append(profile_dict)
        df = pd.DataFrame(profile_data)
        df.to_parquet(profiles_key, index=False)
        f_obj = open(profiles_key, mode="rb")
        f_obj.close()
    except Exception as e:
        raise Exception("\n Error in get_profiles : {}".format(e))
    
def run(params):
    """
    Function to uses the skill params to extract profiles and dump to Managed Content

    @param payload: dictionary containing
        @param profileScehma: ProfileSchema to perform training
    @param token: Cortex token
    @param apiEndpoint: Cortex apiendpoint
    @param projectId: Cortex projectId
    @return: True/False
    """
    token = params["token"]
    apiendpoint = params["apiEndpoint"]
    project = params["projectId"]
    payload = params["payload"]
    profile_schema = payload["profileSchema"]
    filter_cohort = payload.get("filter", "")

    gql_endpoint = apiendpoint + "/fabric/v4/graphql"
    gql_client = get_gql_client(gql_endpoint, token)
    cortex_client = Cortex.client(
        api_endpoint=apiendpoint, verify_ssl_cert=True, token=token, project=project)
    managed_content = ManagedContentClient(cortex_client)
    get_profiles(gql_client, project, profile_schema, profile_schema + ".parquet", filter_cohort)

    f_obj = open(profile_schema + ".parquet", mode="rb")

    managed_content.upload_streaming(key=profile_schema+".parquet", project=project, stream=f_obj, content_type="application/octet-stream")
    f_obj.close()
    return True


if __name__ == '__main__':
    import sys
    params = sys.argv[1]
    # params = {
    #     "token": "eyJhbGciOiJFZERTQSIsImtpZCI6Im5WalJOdWhPQzc5ZFpPYVMwaGt4U09Bek14Zm1mTWl0SUpLY05fdWQwTGcifQ.eyJzdWIiOiIyNmU5NmU1OC1kYjhjLTQ5NWQtODI3OS1jMjQ1YzNlMjMzMGUiLCJhdWQiOiJjb3J0ZXgiLCJpc3MiOiJjb2duaXRpdmVzY2FsZS5jb20iLCJpYXQiOjE2Njk4MjMzNzgsImV4cCI6MTY2OTkwOTc3OH0.5kmaTBBGcn90-yKDilNhgKmMkrhw2fQT6kxVBY95UoFfgLNjYbK6w1nXXr68vIQ81HdKx-yR9-Vv_xzQJTucAQ",
    #     "apiEndpoint": "https://api.dci-dev.dev-eks.insights.ai",
    #     "projectId": "bptest",
    #     "payload": {
    #         "profileSchema": "german-credit-0c3d3",
    #         "experimentName": "german_credit",
    #         "targetColumn": "outcome",
    #         "primaryColumn": "id"
    #     }
    # }
    params = json.loads(params)
    print(f'Received: {params["payload"]}')
    run(params)
