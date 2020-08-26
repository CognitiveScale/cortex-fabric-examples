"""
Copyright (c) 2020. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""
import sys
import time
import json

def process(params):
    print('Starting: %s' % time.ctime())
    payload = params.get('payload', {"message": "No payload passed to webhook, are you calling this skill as part of an agent?"})
    # You can print logs to the console these are collected by docker/k8s
    print(f'Got payload: {payload}')
    time.sleep(20)
    # get the payload
    print('Stopped: %s\n' % time.ctime())

if __name__ == "__main__":
    if len(sys.argv)<2:
        print("Message/payload commandline is required")
        exit(1)
    # The last argument in sys.argv is the payload from cortex
    process(json.loads(sys.argv[-1]))
