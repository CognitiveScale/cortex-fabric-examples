"""
Copyright (c) 2020. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""
from cortex import Cortex
from cortex.content import ManagedContentClient
import json
import sys
import time
# data generator library...
from faker import Faker

def datagen_stream(count):
    fake = Faker()
    for i in range(count):
        d = { "name": fake.name(), "address": fake.address(), "color": fake.random_elements(elements=('red', 'green', 'blue', 'yellow'), length=1)[0]}
        # TODO why is a sleep required
        time.sleep(.02)
        yield f'{json.dumps(d)}\n'.encode()
    print('DONE GENERATING DATA')

def process(params):
    # create a Cortex client instance from the job's parameters
    client = Cortex.client(api_endpoint=params['apiEndpoint'], token=params['token'])
    # get the agent payload
    payload = params.get('payload',{})
    # You can print logs to the console these are collected by docker/k8s
    print(f'Got payload: {payload}')
    # use the `client` instance to use Cortex client libraries
    content_client = ManagedContentClient(client);
    if 'activationId' in params:
        file_name = f'jobchain-data-{params["activationId"]}'
    else:
        #
        file_name = f'jobchain-data-{int(time.time())}'
    # Read `recordCount` from payload, have a default value of raising an Exception is recommended.
    record_count = payload.get('recordCount', 1000)
    # This is streaming the records to Cortex's managed content
    content_client.upload_streaming(file_name, datagen_stream(record_count), 'application/x-jsonlines')
    print(f'Wrote datafile to managed content key: {file_name}')

if __name__ == "__main__":
    if len(sys.argv)<2:
        print("Message/payload commandline is required")
        exit(1)
    # The last argument in argv[] is the payload from cortex
    process(json.loads(sys.argv[-1]))
