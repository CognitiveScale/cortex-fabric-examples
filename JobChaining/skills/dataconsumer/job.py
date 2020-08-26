"""
Copyright (c) 2020. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""
from cortex import Cortex
from cortex.content import ManagedContentClient
import pandas as pd
import sys
import json

def process(params):
    # create a Cortex client instance from the job's parameters
    client = Cortex.client(api_endpoint=params['apiEndpoint'], token=params['token'])
    # get he agent payload
    payload = params['payload']
    # You can print logs to the console these are collected by docker/k8s
    print(f'Got payload: {payload}')
    if 'activationId' in params:
        content_key = f'jobchain-data-{params["activationId"]}'
    else:
        if 'datafileKey' not in payload:
            raise Exception("'datafileKey' is required in the payload")
        content_key = payload['datafileKey']
    print(f'Fetching datafile from managed content: {content_key}')
    # use the `client` instance to use Cortex client libraries
    content_client = ManagedContentClient(client);
    # This is streaming the records to Cortex's managed content
    # if this was called as part of an agent
    content = content_client.download(content_key)
    df = pd.read_json(content, lines=True)
    counts = df['color'].value_counts()
    print(f'{counts.to_json()}')

if __name__ == "__main__":
    if len(sys.argv)<2:
        print("Message/payload commandline is required")
        exit(1)
    # The last argument in sys.argv is the payload from cortex
    process(json.loads(sys.argv[-1]))
