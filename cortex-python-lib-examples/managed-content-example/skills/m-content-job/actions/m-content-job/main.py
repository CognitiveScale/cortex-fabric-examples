"""
Copyright (c) 2020. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""

import json
import sys
from cortex.content import ManagedContentClient


# Download data from managed content.
def download(content_client: ManagedContentClient, project_id: str, key: str):

    data = content_client.download(key=key, project=project_id).data

    return print(f'{data}')


# Uploads string to managed content
def upload(content_client: ManagedContentClient, project_id: str, key: str, content: str):

    # Upload content in payload.
    content_client.upload(key=key, project=project_id, stream_name=key, stream=content,
                          content_type="application/octet-stream")

    return print(f'Managed content ({key}) uploaded: {content}')


# The starting point for the job
if __name__ == '__main__':
    request_body = json.loads(sys.argv[1])
    url = request_body['apiEndpoint']
    token = request_body['token']
    payload = request_body.get('payload', {})

    if payload:
        project_id = payload.get('projectId', request_body['projectId'])

        # Create ManagedContentClient.
        content_client = ManagedContentClient(url=url, token=token, project=project_id)
        key = payload['key']

        if payload['command'] == 'download':
            # Download data from managed content.
            data = content_client.download(key=key, project=project_id).data
            print(f'{data}')

        elif payload['command'] == 'upload':
            # Uploads string to managed content
            content = payload['content']
            content_client.upload(key=key, project=project_id, stream_name=key, stream=content,
                                  content_type="application/octet-stream")

            print(f'Managed content ({key}) uploaded: {content}')
        else:
            print('Invalid command given. Valid commands: `download`, `upload`')
    else:
        print('No payload given')
