"""
Copyright (c) 2020. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""
import requests
import sys
import json

def process(params):
    # get the payload
    payload = params.get('payload', {"message": "No payload passed to webhook, are you calling this skill as part of an agent?"})
    props = params.get('properties', {})
    webhook_url = props.get('webhook-URL','')
    if webhook_url == '':
        raise Exception("Skill property 'webhook-URL' must be provided")
    # You can print logs to the console these are collected by docker/k8s
    print(f'Got payload: {payload}')
    res = requests.post(webhook_url, data = payload)
    if res.status_code != 200:
        raise Exception(f'ERROR {res.status_code} invoking webhook: {res.text}')
    print(f'Webhook invoked\n Http status: {res.status_code} \n Response: {res.text}')

if __name__ == "__main__":
    if len(sys.argv)<2:
        print("Message/payload commandline is required")
        exit(1)
    # The last argument in sys.argv is the payload from cortex
    process(json.loads(sys.argv[-1]))
