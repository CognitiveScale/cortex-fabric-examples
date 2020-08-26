"""
Copyright (c) 2020. Cognitive Scale Inc. All rights reserved.

Licensed under CognitiveScale Example Code [License](https://cognitivescale.github.io/cortex-fabric-examples/LICENSE.md)
"""
from cortex import Message
import json
from flask import Flask, request

app = Flask(__name__)
app.debug = True

@app.route('/v1/hello', methods = ['GET','POST'])
def hello_world():
    if request.method == 'GET':
      return json.dumps({'message': 'Hello from cortex/hellodaemon GET'});
    if request.method == 'POST':
      reqBody = request.get_json(force=True)
      app.logger.info(f'Request: {json.dumps(reqBody)}')
      if reqBody:
        payload = reqBody.get('payload',{})
        text = payload.get('text', '')
        wordcount = len(text.split())
        return json.dumps({"payload":{"message":f'Hello received: \'{text}\', word count: {wordcount}'}});

if __name__ == '__main__':
    app.run(host='0.0.0.0')
